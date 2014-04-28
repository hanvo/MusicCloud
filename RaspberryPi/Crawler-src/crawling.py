#File Crawling
#Things that are done:
# -Crawling of music files
# -Inserting and storing in a local Database (Song_list.db)
# -Database: ID, Title, Absolute file path(Storing is maybe wrong. Double Slash),
#                   LengthofSong(Seconds),Artist, album, albumArt, albumArtType(3 - album cover)
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

    c.execute('''CREATE TABLE if not exists music(id real, song text, path text,lengthOfSong real,artist text, album text,art blob,artType text,artCoverID real)''')
    

    #for loop that will recursivly go through the file given
    #Currently just outputs a list of songs within a folder
    x = "root"
    y = "song name"
    songCount = 0
    for root, dirnames, filenames in os.walk(r'.'):
        x = root
        print "Root: ", root
        print "Song List:"
        print '\n'.join(filenames)
        print '\n'       


        for x in range(len(filenames)):
            y =  filenames[x] #getting the indivdual file
            path = os.path.join(root,filenames[x]) #path to file
            
            if not path.endswith(".mp3"): # Skip non-mp3 files
                continue

            print "Checking file: ", path            

            data = File(path)
            if 'APIC:' in data.tags: # Check if we even have an APIC frame available
                if isinstance(data.tags['APIC:'], list): # Check if we have a list of photos

                    print "Found ", length(data.tags['APIC:']), " APIC images" # Debug statement

                    for apic in data.tags['APIC:']:
                        artwork = apic.data
                        artType = apic.mime
                        artCoverID = apic.type
                        if apic.type == 3: # Stop looping if we found media type 3 (Front cover
                            break
                else: # We don't have a list, just access the fields directly
                    artwork = data.tags['APIC:'].data
                    artType = data.tags['APIC:'].mime
                    artCoverID = data.tags['APIC:'].type
            else:
                print "No Album Art work - ", y
                artwork = 'null'
                artType = 'null'
                artCoverID = 'null'

            audioFile = eyed3.load(path) #loading for artist/Album
            artist = audioFile.tag.artist
            album = audioFile.tag.album
            audio = MP3(path) #for audio lenth
            

            print "Inserting values: \"" + str(songCount) + "\" \"" + str(y) + "\" \"" + str(path) + "\" \"" + str(audioLength) + "\" \"" + str(artist) + "\" \"" + str(album) + "\" \"" + str(artType) + "\""# Debug statement

            c.execute('insert into music values (?,?,?,?,?,?,?,?,?)', (songCount,y,path,audioLength,artist,album,artwork,artType,artCoverID,))
            songCount = songCount + 1
                
    print "\n"
    for row in c.execute('SELECT id,song,path,lengthOfSong,artist,album,artType,artCoverID FROM music '):
        print row


    #c.execute('SELECT art FROM music')
    #picture=c.fetchone()[0]
    #with open('test122.jpg','wb') as img:
    #    img.write(picture)

    # Commit the changes to the database
    conn.commit()

    # Flush to the disk
    conn.close()
        

if __name__ == "__main__":
        main()
