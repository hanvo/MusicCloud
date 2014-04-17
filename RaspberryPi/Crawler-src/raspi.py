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

if __name__ = "__main__":
	thread1 = Thread(target = play_back_func, args = )
	thread2 = Thread(target = serv_func, args = )
	thread3 = Thread(target = communicate_func, args = )
