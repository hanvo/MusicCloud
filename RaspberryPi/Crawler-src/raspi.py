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

timeout = 100
_Rlock = threading.RLock()
flag_serv_func = 0
_clientID = -1

def play_back_func():
	pass

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
	else:
		pass
	
	try:
		
	except socket.timeout:
		serv_func()
	_serv_sock.


def communicate_func():
	pass

def _get_song_request(_serv_sock,_clientID):
	while True:
		_serv_sock.request("GET","klamath.dnsdynamic.com:5050/speaker/request_update?clientID="+str(_clientID))
		_upcoming_song_resp = _serv_sock.getresponse()
		#Push _upcoming_song_resp the Queue


if __name__ == "__main__":
	#thread1 = Thread(target = play_back_func, args =() )
	thread2 = Thread(target = serv_func, args=() )
	#thread3 = Thread(target = communicate_func, args =() )
	#thread1.start()
	thread2.start()
	#thread3.start()
	#thread1.join()
	thread2.join()
	#thread3.join()
