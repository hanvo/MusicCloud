from mutagen import File



data = File(r'C:\Users\QuakeZ\Desktop\Music Folder\#SELFIE.mp3')
artwork = data.tags['APIC:'].data
print type(artwork)

with open('image.jpg','wb') as img:
    img.write(artwork)
