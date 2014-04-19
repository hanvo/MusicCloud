import pygame

#intit the player
pygame.mixer.init()

#load the song
pygame.mixer.music.load("C:\Users\QuakeZ\Desktop\Music Folder\Frozen Medley.mp3")

#Play
pygame.mixer.music.play()

#MUST HAVE because you have to pause the script for a few seconds so it gives it time to
#start playing the song
while pygame.mixer.music.get_busy(): 
    pygame.time.Clock().tick(10)
