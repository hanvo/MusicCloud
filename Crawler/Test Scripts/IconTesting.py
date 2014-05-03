import wx

def OnTaskBarRight(event):
             app.ExitMainLoop()
#setup app
app= wx.PySimpleApp()

#setup icon object
icon = wx.Icon(r'C:\Users\QuakeZ\Documents\GitHub\MusicCloud\Crawler\Crawler-Src\icon-120.png', wx.BITMAP_TYPE_ANY)

#setup taskbar icon
tbicon = wx.TaskBarIcon()
tbicon.SetIcon(icon, "I am an Icon")

#add taskbar icon event
wx.EVT_TASKBAR_RIGHT_UP(tbicon, OnTaskBarRight)

app.MainLoop()
