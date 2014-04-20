#File Crawling
#Things that are done:
# -Crawling of music files
# -Inserting and storing in a local Database (Song_list.db)
# -Database: ID, Title, Absolute file path(Storing is wrong. Double Slash),
#                   LengthofSong(Seconds),Artist, album, album Art, album art Type
#
#Still Need:
# -Have a properties file storing:
#       -Where you want to crawl
#       -outPut Name of Database
# -Limit to Only .mp3 (Right now Assumes that all files within folder are .mp3.
#
# Awesome Stuff I Want to get done
#   GUI where it will have a Start Crawl Button
#   Display of errors files with no album art
#   Explorer to select folder
#
#   

#Don Phan
#CS 252 - Lab6

import os 
import sys
import sqlite3
from mutagen.mp3 import MP3
from mutagen import File
import eyed3



def main():
    crawl()

    
def crawl():

    #Pre-Crawl prep for database init
    location = 'song_list.db'
    table_name = 'music'
    conn = sqlite3.connect(location)
    c = conn.cursor()
    conn.text_factory = str
    
    #create the tables
    sqlStatement = 'drop table if exists ' + table_name
    c.execute(sqlStatement)

    c.execute('''CREATE TABLE if not exists music(id real, song text, path text,lengthOfSong real,artist text, album text,art blob,artType text)''')
    

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
            y =  filenames[songCount] #getting the indivdual file
            path = os.path.join(root,filenames[songCount]) #path to file
            data = File(path)
            if 'APIC:' in data.tags:
                artwork = data.tags['APIC:'].data
                artType = data.tags['APIC:'].mime
            else:
                print "No Album Art work - ", y
                artwork = 'null'
                artType = 'null'
            audioFile = eyed3.load(path) #loading for artist/Album
            artist = audioFile.tag.artist
            album = audioFile.tag.album
            audio = MP3(path) #for audio lenth
            audioLength = audio.info.length   
            c.execute('insert into music values (?,?,?,?,?,?,?,?)', (songCount,y,path,audioLength,artist,album,artwork,artType))
            songCount = songCount + 1
                
    print "\n"
    for row in c.execute('SELECT id,song,path,lengthOfSong,artist,album,artType FROM music '):
        print row


    #c.execute('SELECT art FROM music')
    #picture=c.fetchone()[0]
    #with open('test122.jpg','wb') as img:
    #    img.write(picture)
        

if __name__ == "__main__":
        main()
