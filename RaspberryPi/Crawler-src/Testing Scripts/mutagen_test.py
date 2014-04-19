from mutagen.mp3 import MP3
import os



loc = os.path.join(r'C:\Users\QuakeZ\Desktop\Music Folder','Frozen Medley.mp3')
audio = MP3(loc)
print audio.info.length
