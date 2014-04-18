#Basic Crawling of a folder of files

import os 
import sys
import sqlite3

def main():
    crawl()
    

#crawl
#Obtaining a list of songs from a certain folder
#Will place into database(ID TITLE ARTIST ALBUM LENGTHOFSONG(SECONDS) SIZE(BYTES) (ABS PATH)
#To do:
#Make it read from a property file
#Place into a database file "song_list.db"
    
def crawl():

    #Pre-Crawl prep for database init
    location = 'song_list.db'
    table_name = 'music'
    conn = sqlite3.connect(location)
    c = conn.cursor()

    #create the tables
    sqlStatement = 'drop table if exists ' + table_name
    c.execute(sqlStatement)

    #sqlStatement = 'create table if not exists ' + table_name + '(id real)'
    c.execute('''CREATE TABLE if not exists music(id real, song text)''')
    

    #for loop that will recursivly go through the file given
    #Currently just outputs a list of songs within a folder
    x = "root"
    y = "song name"
    songCount = 0
    for root, dirnames, filenames in os.walk(r'C:\Users\QuakeZ\Desktop\Music Folder'):
        x = root
        print "Root: ", root
        print "\n"
        print "Song List: \n"
        print '\n'.join(filenames)
        print '\n'       


        for x in range(len(filenames)):
            y =  filenames[songCount]
            c.execute('insert into music values (?,?)', (songCount,y,))
            songCount = songCount + 1
        

        
        #print "File Paths: \n"
        #for filename in filenames:
            #print "Path: ", os.path.join(root,filename)

    for row in c.execute('SELECT * FROM music'):
        print row

        
    

if __name__ == "__main__":
        main()
