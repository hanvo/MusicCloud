#Basic Crawling of a folder of files
#Need Socket Programming done

import os 
import sys
import sqlite3

from mutagen.mp3 import mp3

def main():
    crawl()
    

#crawl
#Obtaining a list of songs from a certain folder
#Will place into database(ID TITLE ARTIST ALBUM LENGTHOFSONG(SECONDS) SIZE(BYTES) (ABS PATH)
#To do:
#Make it read from a property file
#Place into a database file called 'song.list'
    
def crawl():

    #for loop that will recursivly go through the file given
    #Currently just outputs a list of songs within a folder
    x = "root"
    y = "song name"
    count = 0
    for root, dirnames, filenames in os.walk(r'C:\Users\QuakeZ\Desktop\Music Folder'):
        x = root
        print "Root: ", root
        print "\n"
        print "Song List: \n"
        print '\n'.join(filenames)
        print '\n'       
        y = filenames[count]
        count = count + 1

        
        print "File Paths: \n"
        for filename in filenames:
            print "Path: ", os.path.join(root,filename)

    temp = "%s\%s" %(x,y)
    print "\n"
    print temp
        
    metadata = mp3.Open(temp)    
        



if __name__ == "__main__":
        main()
