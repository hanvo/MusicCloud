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
import time

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)s %(message)s')

timeout = 50	
_Rlock = threading.RLock()
SONG_END = pygame.USEREVENT + 1

keep_running = True

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
		for key in query_vars.keys():
			if count != 0:
				url = url + "&"
			count = count + 1
			url = url + str(key) + "=" + str(query_vars[key])

	return url

#
# Send a request over the given socket
#
def send_request(http_connection, request, query_vars = {}, body_params = {}, method = "GET"):
	headers = {"Content-Type": "application/json"}
	
	url = create_url(request, query_vars)

	logging.info("Sending request to: " + url)

	if body_params.keys() != 0:
		body = json.dumps(body_params, encoding = "ASCII")
	else:
		body = ""

	http_connection.request(method, url, body, headers)
	return http_connection.getresponse()

#
# Format a log message for a HTTP response
#
def create_response_log_message(response):
	log_message = "Response: " + str(response.status)
	log_message = log_message + " " + str(response.reason)
	log_message = log_message + " " + str(response.getheaders())
	return log_message

#
# Check if we need to request a given song
#
def check_for_song(song_id):
	found_song = 0

	for file in os.listdir('.'):
		if fnmatch.fnmatch(file,str(song_id)):
			found_song = 1
		else:
			pass
	return found_song

#
# Handle a play command from the server
#
def handle_play_command(song_id):
	global next_song
	global current_song
	global next_song_state
	global current_song_state

	_message = {"id":"","status":"","position":""}

	if song_id == current_song:

		logging.error('Play command for current song, ignoring')

	elif song_id == next_song and current_song_state != PLAYING and next_song_state == READY:

		logging.info('Playback command received for next song')
				
		# Update current song
		current_song = next_song
		current_song_state = PLAYING

		# Set next song to UNKNOWN since the next song
		# because the current song and we don't know
		# the new next song
		next_song = UNKNOWN
		next_song_state = UNKNOWN

		# Start playback via PyGame
		pygame.mixer.music.load(str(song_id))
		pygame.mixer.music.play()


		logging.debug('Started playback through PyGame')
		logging.debug('Sending Playing status message to communcation thread')

		_message['id'] = str(current_song)
		_message['status'] = 'Playing'
		_message['position'] = str(0)

		playback_connection_queue.put(_message)
					
	elif song_id == next_song and next_song_state == UNKNOWN:
					
		logging.info('Play command for unavailable song, requesting from server')				
	
		# We need to get the next song from the server
		_message['id'] = str(song_id)
		_message['status'] = 'need_song'
		_message['position'] = str(0);

		playback_connection_queue.put(_message)
	elif song_id == next_song and next_song_state == DOWNLOADING:
		logging.debug("Waiting for download to finish")
		# TODO Have the comm thread auto start playback now


#
# Handle incoming stop requests
#
def handle_stop_command(song_id):
	global next_song
	global current_song
	global next_song_state
	global current_song_state

	_message = {"id":"","status":"","position":""}

	# Sanity check for command
	if song_id == current_song:

		# Check the current state of the song
		if current_song_state == PLAYING:
			pygame.mixer.music.stop()

			_message['id'] = str(song_id)
			_message['status'] = 'Stopped'
			_message['position'] = str(pygame.mixer.music.get_pos())

			current_song_state = STOPPED

			playback_connection_queue.put(_message)
			# TODO Look at _next_song
			# and send requestSong/ready or wait until
			# previous ready call succeeds
		else:
			pass # TODO decide how to handle this
	
	else:
		# TODO Error, how to handle this
		logging.error("Stop command from server for wrong song")


#
# Logic to handle upcoming song updates
#
def handle_upcoming_song_update(song_id):

	global next_song
	global current_song
	global next_song_state
	global current_song_state

	_message = {"id":"","status":"","position":""}

	# Set next song
	if song_id != current_song:

		fetch_song = False

		# Check if this is a duplicate update message
		if song_id != next_song and next_song != UNKNOWN:

			next_song = song_id
			next_song_state = UNKNOWN

			fetch_song = True
		elif next_song_state == UNKNOWN and next_song != UNKNOWN:
			fetch_song = True
			

		# Fetch the next song if we've been given a new song
		# Or we have a next song but we don't have it yet
		if fetch_song:

			found_song = check_for_song(song_id)
			
			if found_song == 0: # We need the file, so request first
				
				logging.debug('Could not find song, asking from server')

				_message['id'] = str(song_id)
				_message['status'] = 'need_song'
				_message['position'] = str(0);

				playback_connection_queue.put(_message)
			else:
				logging.debug('Song ready for playback')
				next_song_state = READY

				# Check on the current song to make decision about next move
				if current_song_state != PLAYING:
					# We aren't playing anything right now
					# So send ready immediately since we have the file
				
					_message['id'] = str(song_id)
					_message['status'] = 'Ready'
					_message['position'] = '0'
					playback_connection_queue.put(_message)
				else:
					logging.debug("Song ready for playback, waiting for previous song to end")

		elif next_song_state == DOWNLOADING:
			logging.debug("Now fetching song, download in progress")
												
	elif song_id == next_song:
			logging.debug("Duplicate upcoming song update")

#
# Handle a need song request
#
def handle_need_song(song_id, comm_sock):
	global client_id

	global next_song
	global current_song
	global next_song_state
	global current_song_state

	playback_message = {"id": 0, "position": 0, "status": "ready"}

	logging.debug("IN NEED SONG with song_id = " + str(song_id))

	# Mark as downloading currently
	next_song_state = DOWNLOADING

	song_response = send_request(comm_sock, "request_song", {"clientID": client_id, "songID": song_id})
			
	log_message = create_response_log_message(song_response)
	logging.debug("Song Data Response: " + log_message)

	if song_response.status == 200:
		logging.debug("Song song data from server, saving to file")

		song_data = song_response.read()

		output_file = open(str(song_id),'w')
		output_file.write(song_data)
		output_file.close()
	
		next_song_state = READY

		if current_song_state != PLAYING:
			
			# current song finished, send ready message to the server
			playback_message['status'] = 'Ready'
			playback_message['id'] = song_id
			logging.debug("The Message in playing state is "+str(playback_message))
			send_request(comm_sock, "status_update", {"clientID": client_id}, playback_message, "POST")

		else:	
			# Update status in the background and when PyGame finishes playing,
			# it will see the next_song_state and send "ready"
			logging.debug("Song %d retrieved, waiting for previous song to finish")

	else:
		# Failed to download song, set state UNKNOWN (Maybe add error state later)
		next_song_state = UNKNOWN
		logging.error("Failed to get song data from server")
		# TODO How to handle this error??
			


#
# Main function for requesting updates from the server
#
def update_func():
	global client_id
	global server_url

	global server_url
	global server_port

	global keep_running

	http_connection = httplib.HTTPConnection(server_url, server_port, timeout = timeout)
	socket.setdefaulttimeout(timeout)
	
	# Perform authentication
	try:

		logging.info("Authenticating")
		server_response = send_request(http_connection, "authenticate", {}, {"pin":1234}, "POST")

		if server_response.status != 200:
			sys.exit()

		log_message = create_response_log_message(server_response)
		logging.info("Authenticated response " + log_message)

		server_response = json.loads(server_response.read())

		client_id = server_response['id']
		logging.debug("Authenticated with client ID " + str(client_id))
		server_communication_ID_queue.put(client_id)
	except Exception as e:
		logging.error("Authentication failed from exception: " + str(e))
		http_connection.close()
		keep_running = False
	
	while keep_running:
		try:
			http_connection = httplib.HTTPConnection(server_url, server_port, timeout = timeout)
			socket.setdefaulttimeout(timeout)

			logging.info("Sending request_update")

			update_response = send_request(http_connection, "request_update", {"clientID": client_id})

			logging.info("Request Update Response")
			log_message = create_response_log_message(update_response)
			logging.info(log_message)

			if update_response.status == 200:

				rresp = update_response.read()
				logging.debug("Message from server: "+str(rresp))
				server_playback_queue.put(rresp)
			else:
				log_message = create_response_log_message(update_response)
				logging.warning("Failed to get update from server: " + log_message)
				
			http_connection.close()
		except socket.timeout:
			logging.debug("Timeout for connection, resending")
		except Exception as e:
			logging.exception("Critical exception thrown " + str(e))
			sys.exit()

	# DEAUTHENTICATE
	send_request(http_connection, "deauthenticate", {"clientID": client_id}, {"id": client_id}, "POST")
	http_connection.close()
	
	logging.info("Update thread exiting")


#
# Main function for playback control thread
#
def playback_func():
	global next_song
	global current_song
	global next_song_state
	global current_song_state

	global keep_running
		
	try:
		# Initialize the mixer for PyGame
		pygame.mixer.init()
		pygame.mixer.music.set_endevent(SONG_END)

		# Some platforms might need to init the display for some parts of pygame.
		os.environ["SDL_VIDEODRIVER"] = "dummy"
		pygame.display.init()
		pygame.display.set_mode((1,1))
	except Exception as e:
		logging.error("Failed to initialized PyGame: " + str(e))
		keep_running = False

	_message = {"id":"","status":"","position":""}

	while keep_running:
		
		try:
	
			# Get the update from the queue, but use timeout
			# to multiplex this with the Pygame event loop
			update_body = server_playback_queue.get(True, 0.1)

			response = json.loads(update_body)
			server_playback_queue.task_done()
		
			update_type = response['update_type']

			logging.debug("Update type is = " + update_type)

			if update_type == "playback_command":
				values = response['values']
				song_id = values['id']
				command = values['command']

				if command == 'Play':

					logging.info("Play command received")

					handle_play_command(song_id)
				elif command == 'Stop':

					logging.info("Stop command received")

					handle_stop_command(song_id)
				else:
					logging.warn("Unknown playback command received: " + command)
			
			elif update_type == "upcoming_song":

				values = response['values']
				song_id = values['id']

				logging.info("Processing upcoming_song update for ID " + str(song_id))

				handle_upcoming_song_update(song_id)
			else:
				logging.warning("Unknown update received: " + update_type)

		except Queue.Empty:

			# If the queue is empty, extract an event from PyGame
			# But only if current_song_state is playing
			# which means we're expecting a stop event
			if current_song_state == PLAYING:
				event = pygame.event.poll()
				if event.type == SONG_END:
					current_song_state = STOPPED

					# Send stopped message to the server
					_message['id'] = current_song
					_message['status'] = 'Stopped'
					_message['position'] = str(pygame.mixer.music.get_pos())
					playback_connection_queue.put(_message)
		except Exception as e:
			logging.error("Exception in playback thread: " + str(e))

	logging.info("Playback thread exiting")	

		

#
# Main function for server communication thread
#
def communicate_func():

	global client_id

	global server_url
	global server_port

	global next_song
	global current_song
	global next_song_state
	global current_song_state

	global keep_running

	while keep_running:	

		# Pop the specific request from the Queue, depending on that do the following
		# Use a timeout to remain responsive to updates to keep_running
		try:
			playback_message = playback_connection_queue.get(True, 0.5)
		except Queue.Empty:
			continue

		playback_connection_queue.task_done()

		logging.info("Message in communication thread: " + str(playback_message))

		logging.debug("communicate_func received request: " + str(playback_message))
		comm_sock = httplib.HTTPConnection(server_url, server_port, timeout = timeout)
		

		if client_id == UNKNOWN:

			logging.debug("Getting client ID from queue")

			client_id = server_communication_ID_queue.get() # Getting the clientID from the queue
			server_communication_ID_queue.task_done()
		
			logging.info("client_id in communicate_func is " + str(client_id))

		if playback_message['status'] == 'need_song':
			
			# Request song data from server
			song_id = playback_message['id']

			handle_need_song(song_id, comm_sock)
		else:
	
			logging.debug("Sending " + str(playback_message['status']) + " status update")
			response = send_request(comm_sock, "status_update", {"clientID": client_id}, playback_message, "POST")

			# TODO Check response status from the server (Need 200)

			# If we stopped, we need to check if we can start the next song
			if playback_message['status'] == 'Stopped' and next_song != UNKNOWN:
				
				# Check if we have the song
				found_song = check_for_song(next_song)
				
				if next_song_state == READY:

					# Tell the server we are ready to play the next song
					playback_message['id'] = next_song
					playback_message['status'] = 'Ready'
					playback_message['position'] = '0'
					response = send_request(comm_sock, "status_update", {"clientID": client_id}, playback_message, "POST")		
					# TODO Check response status
				elif next_song_state == UNKNOWN:
				
					# Ask for the next song
					playback_message['id'] = next_song
					playback_message['status'] = 'need_song'
					playback_message['position'] = '0'	
					playback_connection_queue.put(playback_message)
				else:
				
					# Still downloading
					logging.debug("Waiting for download to finish")

	logging.info("Communication thread exiting")

def main():
	playback_thread = Thread(target = playback_func, args =())
	update_thread = Thread(target = update_func, args=())
	communicate_thread = Thread(target = communicate_func, args =())

	#playback_thread.daemon = True
	#update_thread.daemon = True
	#communicate_thread.daemon = True

	playback_thread.start()
	update_thread.start()
	communicate_thread.start()

	while threading.active_count() > 0:
		time.sleep(0.1)

if __name__ == "__main__":
	
	# This is to fix our control-c woes
	try:
		main()
	except KeyboardInterrupt:
		logging.info("Interrupt received, exiting...")
		keep_running = False

