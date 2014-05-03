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

#
# NOTES:
#
# Author: Jason P. Rahman
#
# Added code to handle:
#	The case when a song file is already present
#	Record the next song ID as a state machine
#	Record the currently playing song ID
#	Added a sanity check for the "Stopped" code
#		More places similar checks can be made
#	Added a few other notes
#
#	Next Steps:
#		Update stopped code to send Ready/Send song request/wait for previous song request
#		Continue adding more sanity checked
#		Continue tracking _current_song _next_song state variables
#

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

import logging

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)s %(message)s')

timeout = 50	
_Rlock = threading.RLock()
flag_serv_func = 0
SONG_END = pygame.USEREVENT + 1


server_playback_queue = Queue.Queue(0)
playback_connection_queue = Queue.Queue(0)
connection_playback_queue = Queue.Queue(0)
server_communication_ID_queue = Queue.Queue(0)

# Define "Constants"
UNKNOWN = -1
PLAYING = 1
STOPPED = 2
DOWNLOADING = 3
READY = 4

client_id = UNKNOWN

# Initialize state to default values
current_song = UNKNOWN
current_song_state = UNKNOWN
next_song = UNKNOWN
next_song_state = UNKNOWN

server_url = "klamath.dnsdynamic.com"
server_port = "5050"

#
# query_vars is a dictionary of query variables
#
def create_url(request, query_vars = {}):
	global server_url

	url = server_url + ":" + server_port + "/speaker/" + request

	# Add query vars iff we have query vars to add
	if len(query_vars) > 0:
		url = url + "?"
		count = 0
		for key in query_vars:
			if count != 0:
				url = url + "&"
			url = url + str(key) + "=" + str(query_vars[key])

	return url
			
#
# Send a request over the given socket
#
def send_request(http_socket, request, query_vars = {}, body_params = {}, method = "GET"):
	headers = {"Content-Type": "application/json"}
	
	url = create_url(request, query_vars)

	logging.info("Sending request to: " + url)

	if body_params.keys() != 0:
		body = json.dumps(body_params, encoding = "ASCII")
	else:
		body = ""

	http_socket.request(method, url, body, headers)
	return http_socket.getresponse()

#
# Main function for the server thread
#
def serv_func():
	global client_id
	global server_url

	global server_url
	global server_port

	_serv_sock = httplib.HTTPConnection(server_url, server_port, timeout = timeout)
	socket.setdefaulttimeout(timeout)
	
	if client_id == UNKNOWN:

		logging.info("Authenticating")
		_serv_resp_json = send_request(_serv_sock, "authenticate", {}, {"pin":1234}, "POST")

		if _serv_resp_json.status != 200:
			sys.exit()

		logging.info("Authenticated response" + str(_serv_resp_json.status) + " " + _serv_resp_json.reason)

		_serv_resp_message = _serv_resp_json.read()
		_serv_response = json.loads(_serv_resp_message)

		client_id = _serv_response['id']
		logging.debug("Client ID is " + str(client_id))
		server_communication_ID_queue.put(client_id)
	
	
	try:
		while True:
			logging.info("Sending request_update")
			logging.info("Client ID in try is ="+str(client_id))

			_upcoming_song_resp = send_request(_serv_sock, "request_update", {"clientID": client_id})

			logging.info("Request Update Response")
			logging.info(str(_upcoming_song_resp.status) + " " + str(_upcoming_song_resp.reason) + " " + str(_upcoming_song_resp.getheaders()))
			#Push _upcoming_song_resp.read() the Queue it could also be the playcommand

			if _upcoming_song_resp.status == 200:

				rresp = _upcoming_song_resp.read()
				logging.debug("Message from server: "+str(rresp))
				server_playback_queue.put(rresp)
			else:
				logging.warning("Exiting")
				sys.exit()

	except socket.timeout:
		logging.debug("Timeout for connection")
		
		# NOTE: This will lead to stack overflow
		serv_func()

	_serv_sock.close()
	# DEAUTHENTICATE
	params1 = json.dumps({"id":str(client_id)},encoding = "ASCII")
	headers = {"Content-Type":"application/json"}
	_serv_sock.request("POST","klamath.dnsdynamic.com:5050/speaker/deauthenticate",params1,headers)


#
# Main function for playback control thread
#
def playback_func():
	global next_song
	global current_song
	global next_song_state
	global current_song_state
		
	pygame.mixer.init() #might have to make global if going to recursively call

	_message = {"id":"","status":"","position":""}

	while True:
		
		try:
			update_body = server_playback_queue.get(True, 0.1)

			_response = json.loads(update_body)
			server_playback_queue.task_done()

			logging.info("Response in playback_func is " + str(_response))
		
			update_type = _response['update_type']

			logging.debug("Update type is = " + update_type)

			if update_type == "playbackcommand":
				values = _response['values']
				song_id = values['id']
				command = values['command']

				if command == 'Play':

					logging.info("Play command recieved")

					if song_id == current_song:

						logging.error('Play command for current song, ignoring')

					elif current_song_state != PLAYING and song_id == next_song and next_song_state == READY:

						logging.info('Playback command received for next song')
				
						# Set next song to UNKNOWN since the next song
						# because the current song and we don't know
						# the new next song
						next_song = UNKNOWN
						next_song_state = UNKNOWN

						# Update current song
						current_song = next_song
						current_song_state = PLAYING

						# Start playback via PyGame
						pygame.mixer.music.set_endevent(SONG_END)
						#pygame.mixer.music.load(str(song_id))
						#pygame.mixer.music.play()

						#while pygame.mixer.music.get_busy():
						#	pygame.time.Clock().tick(10)

						logging.debug('Started playback through PyGame')
						logging.debug('Sending Playing status message to communcation thread')

						_message['id'] = str()
						_message['status'] = 'Playing'
						_message['position'] = str(0)

						logging.info("The message in Play is " + str(_message))

						playback_connection_queue.put(_message)
					
					elif song_id == next_song and next_song_state == UNKNOWN:
					
						logging.info('Play command for unavailable song, requesting from server')				
	
						# We need to get the next song from the server
						_message['id'] = str(song_id)
						_message['status'] = 'need_song'
						_message['position'] = str(0);

						playback_connection_queue.put(_message)

				if command == 'Stop':

					# Sanity check for command
					if song_id == current_song:

						# Check the current state of the song
						if current_song_state == PLAYING:
							pygame.mixer.music.stop()

							_message['id'] = str(song_id)
							_message['status'] = 'Stopped'
							_message['position'] = str(pygame.mixer.music.get_pos())

							playback_connection_queue.put(_message)
							# TODO Look at _next_song
							# and send requestSong/ready or wait until
							# previous ready call succeeds
						else:
							pass # TODO decide how to handle this
	
					else:
						# TODO Error, how to handle this
						logging.error("Stop command from server for wrong song")
			

			if update_type == "upcoming_song":

				logging.info("Processing upcoming_song update")

				values = _response['values']
				song_id = values['id']

				logging.debug("Setting next_song to " + str(song_id))

				# Set next song
				next_song = song_id
				next_song_state = UNKNOWN

				found_song = 0

				for file in os.listdir('.'):
					if fnmatch.fnmatch(file,str(song_id)):
						found_song = 1
					else:
						pass
			
				if found_song == 0: # We need the file, so request first
				
					logging.debug('Could not find song, asking next')

					_message['id'] = str(song_id)
					_message['status'] = 'need_song'
					_message['position'] = str(0);

					playback_connection_queue.put(_message)
				else:
					logging.debug('Song ready for playback')
					next_song_state = READY

					if current_song_state != PLAYING:
						# We aren't playing anything right now
						# So send ready immediately since we have the file
				
						_message['id'] = str(song_id)
						_message['status'] = 'Ready'
						_message['position'] = '0'
						playback_connection_queue.put(_message)

		except Queue.Empty:

			# If the queue is empty, extract an event from PyGame
			if current_song_state == PLAYING:
				event = pygame.event.poll()
				if event.type == SONG_END:
					current_song_state = STOPPED

					_message['status'] = 'Stopped'
					_message['position'] = str(pygame.mixer.music.get_pos())
					playback_connection_queue.put(_message)
		

		

#
# Main function for server communication thread
#
def communicate_func():
	while True:
		logging.debug("Entered WHILE")
		global client_id

		global server_url
		global server_port

		global next_song
		global current_song
		global next_song_state
		global current_song_state		

		# Pop the specific request from the Queue, depending on that do the following
		_request_set = playback_connection_queue.get()
		playback_connection_queue.task_done()

		logging.debug("communicate_func received request: \n"+str(_request_set))
		_comm_sock = httplib.HTTPConnection(server_url, server_port, timeout = timeout)
		

		if client_id == UNKNOWN:
			client_id = server_communication_ID_queue.get() # Getting the clientID from the queue
			server_communication_ID_queue.task_done()
		
		logging.info("client_id in communicate_func is " + str(client_id))

		if _request_set['status']=='need_song':
			
			logging.debug("IN NEED SONG with song_id = " + str(song_id))

			# Request song data from server
			song_id = _request_set['id']
			_song_data_resp = send_request(_comm_sock, "request_song", {"clientID": client_id, "songID": song_id})
			
			logging.debug("Song Data Response: " + str(_song_data_resp.status) + " " + str(_song_data_resp.reason))

			if _song_data_resp.status == 200:
				logging.debug("Song song data from server, saving to file")

				_song_data= _song_data_resp.read()

				output_file = open(str(song_id),'w')
				output_file.write(_song_data)
				output_file.close()

				if current_song_state != PLAYING:
					# Send ready message to the server

					_request_set['status'] = 'Ready'
					logging.debug("The Message in playing state is "+str(_request_set))
					send_request(_comm_sock, "status_update", {"clientID": client_id}, _request_set, "POST")

				else:
					
					# Update status in the background
					# When PyGame finishes playing, it will see the status
					next_song_state = READY

			else:
			
				logging.error("Failed to get song data from server")
				# TODO How to handle this error??

		# NEED TO IMPLEMENT READY, playback position for READY(?)

		else:
			if _request_set['status']== 'Playing':
				logging.debug("IN PLAYING")
		
			if _request_set['status']== 'Stopped':
				logging.debug("IN STOPPED")

			if _request_set['status']== 'Ready':
				logging.debug("IN READY")

			logging.debug("Sending " + str(_request_set['status']) + " status update")
			response = send_request(_comm_sock, "status_update", {"clientID": client_id}, _request_set, "POST")


if __name__ == "__main__":
	thread1 = Thread(target = playback_func, args =() )
	thread2 = Thread(target = serv_func, args=() )
	thread3 = Thread(target = communicate_func, args =() )
	thread1.start()
	thread2.start()
	thread3.start()
	thread1.join()
	thread2.join()
	thread3.join()
