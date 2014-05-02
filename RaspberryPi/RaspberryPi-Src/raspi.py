#!/usr/bin/python

""" Program to receive HTTP request from server with JSON body which will
contain the data for the song to be played/paused/stopped. The program will
use three threads.

Thread 1 - This thread will be used for playback.
Thread 2 - This thread will be used to keep the connection with the Server open
and when it timesout, the connection will be reopened and communicate with the playback thread.
Thread 3 - This thread will send Thread 1 commands for the song to be played/paused etc and also
communicate with the server.

The threads will use a queue which will be a basic FIFO implementation holding object types
which represent the request for the thread to execute. When the queue dequeues the thread will 
execute that command and keep dequeueing and executing till the queue it empty in which case the
thread will wait on the queue.

@ Author - Anant Goel
@ date - April 2014
@ purpose - CS 252 Lab 6.

"""

#Importing the thread function
from threading import Thread
import threading
import httplib
import json
import socket
import sys
import Queue
import pygame
import os
import fnmatch

timeout = 100
_Rlock = threading.RLock()
flag_serv_func = 0
_clientID = -1
_playingstate = 0
SONG_END = pygame.USEREVENT + 1
_serv_playback_queue = Queue.Queue(0)
_playback_conn_queue = Queue.Queue(0)
_conn_playback_queue = Queue.Queue(0)
_serv_comm_ID_queue = Queue.Queue(0)


def serv_func():
	_serv_sock = httplib.HTTPConnection('klamath.dnsdynamic.com', 5050, timeout = timeout)
	socket.setdefaulttimeout(timeout)
	global flag_serv_func
	global _clientID
	if flag_serv_func == 0:
		flag_serv_func = 1
		params = json.dumps({"pin":1234},encoding = "ASCII")
		headers = {"Content-Type": "application/json"}
		print "Authenticating\n"
		_serv_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/authenticate",params,headers)
		_serv_resp_json = _serv_sock.getresponse()

		if _serv_resp_json.status != 200:
			sys.exit()
		else:
			pass
		print "Authenticated response\n"
		print _serv_resp_json.status, _serv_resp_json.reason
		_serv_resp_message = _serv_resp_json.read()
		_serv_response = json.loads(_serv_resp_message)
		_clientID = _serv_response['id']
		print "Client ID is ="+str(_clientID)
		print "\n"
		_serv_comm_ID_queue.put(_clientID)
	else:
		pass
	
	try:
		while True:
			print "Sending request_update\n"
			print "Client ID in try is ="+str(_clientID)
			print "\n"
			_serv_sock.request("GET","klamath.dnsdynamic.com:5050/speaker/request_update?clientID="+str(_clientID))
			_upcoming_song_resp = _serv_sock.getresponse()
			print "Request Update Response\n"
			print _upcoming_song_resp.status, _upcoming_song_resp.reason, _upcoming_song_resp.getheaders()
			#Push _upcoming_song_resp.read() the Queue it could also be the play_command
			if _upcoming_song_resp.status == 200:
				rresp = _upcoming_song_resp.read()
				print "Message from server \n"+str(rresp)
				print "\n"
				_serv_playback_queue.put(rresp)
			else:
				print "exiting\n"
				sys.exit()
	except socket.timeout:
		serv_func()

	_serv_sock.close()
	# DEAUTHENTICATE
	params1 = json.dumps({"id":str(_clientID)},encoding = "ASCII")
	headers = {"Content-Type":"application/json"}
	_serv_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/deauthenticate",params1,headers)


def play_back_func():
	
	while True:
		pygame.mixer.init() #might have to make global if going to recursively call
		global _playingstate
		_response = json.loads(_serv_playback_queue.get())
		_serv_playback_queue.task_done()
		print "the Response in play_back_func is \n"+str(_response)
		print "\n"
		_message = {"id":"","status":"","position":""}
		_update_type = _response['update_type']
		print "Update type is = "+_update_type
		print "\n"
		if _update_type == "playback_command":
			_values = _response['values']
			_songID = _values['id']
			_command = _values['command']

			if _command == 'Play':
				print "IM PLAYING"
				_playingstate = 1
				pygame.mixer.music.set_endevent(SONG_END)
				pygame.mixer.music.load(str(_songID))
				pygame.mixer.music.play()
				while pygame.mixer.music.get_busy():
					pygame.time.Clock().tick(10)
				_message['id'] = str(_songID)
				_message['status'] = 'Playing'
				_message['position'] = str(pygame.mixer.music.get_pos())
				print "The message in Play is \n"+str(_message)
				_playback_conn_queue.put(_message)
				#There will be an issue here with the loop, find a way to solve it
				#for event in pygame.event.get():
				#	if event.type == SONG_END:
				#		_message['status'] = 'Stopped'
				#		_message['position'] = str(pygame.mixer.music.get_pos())
				#		_playback_conn_queue.put(_message)
				#		send a ready message

			if _command == 'Stop':
				pygame.mixer.music.stop()
				_message['id'] = str(_songID)
				_message['status'] = 'Stopped'
				_message['position'] = str(pygame.mixer.music.get_pos())
				_playback_conn_queue.put(_message)
				_message['status'] = 'Ready'
				_playback_conn_queue.put(_message)
			

		if _update_type == "upcoming_song":
			print "IN UPCOMING SONG\n"
			_flag_ut = 0
			_values = _response['values']
			_songID = _values['id']

			for file in os.listdir('.'):
				if fnmatch.fnmatch(file,str(_songID)):
					_flag_ut = 1
				else:
					pass
			
			if _flag_ut == 0:
				_message['id'] = str(_songID)
				_message['status'] = 'need_song'
				_message['position'] = str(0);
				print "The Message is \n"+str(_message)
				print "\n"
				_playback_conn_queue.put(_message)

def communicate_func():
	while True:
		print "DEBUG: Entered WHILE "
		# Pop the specific request from the Queue, depending on that do the following
		_request_set = _playback_conn_queue.get()
		_playback_conn_queue.task_done()
		print "The Request in communicate_func is \n"+str(_request_set)
		print "\n"
		print _request_set['status']
		_comm_sock = httplib.HTTPConnection('klamath.dnsdynamic.com', 5050, timeout = timeout)
		
		_clientID = _serv_comm_ID_queue.get() # Getting the clientID from the queue
		_serv_comm_ID_queue.task_done()
		print "_clientID in communicate_func = "+str(_clientID)

		if _request_set['status']=='need_song':
			print "IN NEED SONG"
			_songID = _request_set['id']
			print _songID
			_comm_sock.request("GET","klamath.dnsdynamic.com:5050/speaker/request_song?clientID="+str(_clientID)+"&songID="+str(_songID))
			_song_data_resp = _comm_sock.getresponse()
			
			print "Song Data Response"
			print _song_data_resp.status, _song_data_resp.reason

			if _song_data_resp.status == 200:
				_song_data= _song_data_resp.read()
				output_file = open(str(_songID),'w')
				output_file.write(_song_data)
				output_file.close()
				if _playingstate == 0:
					_request_set['status'] = 'Ready'
					print "The Message in playing state is \n"+str(_request_set)

		# NEED TO IMPLEMENT READY, playback position for READY(?)

		if _request_set['status']=='Playing':
			print "IN PLAYING"
			_params_update = json.dumps(_request_set,encoding = "ASCII")
			_headers_update = {"Content-Type":"application/json"}
			_comm_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/status_update?clientID="+str(_clientID),_params_update,_headers_update)
		
		if _request_set['status']=='Stopped':
			print "IN STOPPED"
			_params_update = json.dumps(_request_set,encoding = "ASCII")
			_headers_update = {"Content-Type":"application/json"}
			_comm_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/status_update?clientID="+str(_clientID),_params_update,_headers_update)	

		if _request_set['status']=='Ready':
			print "IN READY"
			_params_update = json.dumps(_request_set,encoding = "ASCII")
			_headers_update = {"Content-Type":"application/json"}
			_comm_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/status_update?clientID="+str(_clientID),_params_update,_headers_update)


if __name__ == "__main__":
	thread1 = Thread(target = play_back_func, args =() )
	thread2 = Thread(target = serv_func, args=() )
	thread3 = Thread(target = communicate_func, args =() )
	thread1.start()
	thread2.start()
	thread3.start()
	thread1.join()
	thread2.join()
	thread3.join()
