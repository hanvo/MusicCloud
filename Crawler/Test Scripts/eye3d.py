import eyed3

audiofile = eyed3.load("C:\Users\QuakeZ\Desktop\Music Folder\Bubblegum.mp3")
print audiofile.tag.artist
print audiofile.tag.album
