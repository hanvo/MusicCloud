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
SONG_END = pygame.USEREVENT + 1
_serv_playback_queue = Queue.Queue(0)
_playback_conn_queue = Queue.Queue(0)
_conn_playback_queue = Queue.Queue(0)
_serv_comm_ID_queue = Queue.Queue(0)


def serv_func():
	_serv_sock = httplib.HTTPConnection('klamath.dnsdynamic.com', 5050, timeout = timeout)
	socket.setdefaulttimeout(timeout)
	if flag_serv_func == 0:
		flag_serv_func = flag_serv_func + 1
		params = json.dumps({"pin":1234},encoding = "ASCII")
		headers = {"Content-Type": "application/json"}
		_serv_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/authenticate",params,headers)
		_serv_resp_json = _serv_sock.getresponse()

		if _serv_resp_json.status != 200:
			sys.exit(-1)
		else:
			pass
		_serv_response = json.load(_serv_resp_json.read())
		_clientID = _serv_response['id']
		_serv_comm_ID_queue.put(_clientID)
	else:
		pass
	
	try:
		while True:
		_serv_sock.request("GET","klamath.dnsdynamic.com:5050/speaker/request_update?clientID="+str(_clientID))
		_upcoming_song_resp = _serv_sock.getresponse()
		#Push _upcoming_song_resp.read() the Queue it could also be the play_command
		_serv_playback_queue.put(_upcoming_song_resp.read())
	except socket.timeout:
		serv_func()
	_serv_sock.close()


def play_back_func():
	# playback command, possibly from REQUEST_SPEAKER_UPDATE, implemented in play back thread
	pygame.mixer.init()
	_response = json.load(_serv_playback_queue.get())
	_serv_playback_queue.task_done()
	_message = {"id":"","status":"","position":""}
	_update_type = _response['update_type']
	if _update_type == "playback_command":
		_values = _response['values']
		_songID = _values['id']
		_command = _values['command']
		if _command == 'Play':
			pygame.mixer.music.set_endevent(SONG_END)
			pygame.mixer.music.load(_songID)
			pygame.mixer.music.play()
			while pygame.mixer.music.get_busy():
				pygame.time.Clock().tick(10)
			_message['id'] = str(_songID)
			_message['status'] = 'Playing'
			_message['position'] = str(pygame.mixer.music.get_pos())
			
		if _command == 'Stop':
			pygame.mixer.music.stop()
	if _update_type == "upcoming_song":
		_flag_ut = 0
		_values = _response['values']
		_songID = _values['id']

		for file in os.listdir('.'):
			if fnmatch.fnmatch(file,_songID):
				_flag_ut = 1
	pass

def communicate_func():
	# Pop the specific request from the Queue, depending on that do the following
	_comm_sock = httplib.HTTPConnection('klamath.dnsdynamic.com', 5050, timeout = timeout)
	_clientID = _serv_comm_ID_queue.get() # Getting the clientID from the queue
	# REQUEST_SONG
	# the value of song ID is in the queue
	_upcoming_resp = json.load(_playback_conn_queue.get())
	_playback_conn_queue.task_done()
	_values = _upcoming_resp['values']
	_songID = _values['id']
	_comm_sock.request("GET","klamath.dnsdynamic.com:5050/request_song?clientID="+str(_clientID)+"&songID"+str(_songID))
	_song_data_json = _comm_sock.getresponse().read()
	_song_data = json.load(_song_data_json)

	# Status update depeding on the playback thread
	_params_update = json.dumps({"id":_songID,"status":"playing","position":position(?)},encoding = "ASCII")
	_headers_update = {"Content-Type":"application/json"}
	_comm_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/status_update?clientID="+str(_clientID),_params_update,_headers_update)
	
if __name__ == "__main__":
	#thread1 = Thread(target = play_back_func, args =() )
	thread2 = Thread(target = serv_func, args=() )
	thread3 = Thread(target = communicate_func, args =() )
	#thread1.start()
	thread2.start()
	thread3.start()
	#thread1.join()
	thread2.join()
	thread3.join()
