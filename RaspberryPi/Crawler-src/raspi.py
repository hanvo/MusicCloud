#!/usr/bin/python

""" Program to receive HTTP request from server with JSON body which will
contain the data for the song to be played/paused/stopped. The program will
use three threads.

Thread 1 - This thread will be used for playback.
Thread 2 - This thread will be used to keep the connection with the Server open
and when it timesout, the connection will be reopened.
Thread 3 - This thread will send Thread 1 commands for the song to be played/paused etc.

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

_Rlock = thread.RLock()
_playbackstate = 0 # 1 - Play the song , 2 - Pause the song , 3.. other play back options




if __name__ = "__main__":
	thread1 = Thread(target = play_back_func, args =() )
	thread2 = Thread(target = serv_func, args =() )
	thread3 = Thread(target = communicate_func, args =() )
	thread1.start()
	thread2.start()
	thread3.start()
	thread1.join()
	thread2.join()
	thread3.join()
