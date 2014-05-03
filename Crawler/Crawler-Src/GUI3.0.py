# Crawler GUI
# Features:
# -Selecting a folder to crawl
# -Outputs into the GUI window

# Need:
# -Clear Button
# -Run the Java Code
# -About Section

import wx
import os
import sys
from crawling import crawl
import socket


class RedirectText(object):
    def __init__(self,aWxTextCtrl):
        self.out=aWxTextCtrl

    def write(self,string):
        self.out.WriteText(string)

class MyFrame(wx.Frame):
    def __init__(self, parent, id, title):
      wx.Frame.__init__(self, parent, id, title)

      #Application Icon
      self.icon = wx.Icon('icon-120.png',wx.BITMAP_TYPE_ANY)
      self.SetIcon(self.icon)

      #making dat panel
      panel = wx.Panel(self)

      #making a layout just for output
      vbox = wx.BoxSizer(wx.VERTICAL)

      #adding the MessageBox Label
      hbox1 = wx.BoxSizer(wx.HORIZONTAL)
      st1 = wx.StaticText(panel, label="Message Box")
      hbox1.Add(st1)
      vbox.Add(hbox1, flag=wx.LEFT | wx.TOP , border=10)
      vbox.Add((-1,10))

      #adding the textbox output
      hbox2 = wx.BoxSizer(wx.HORIZONTAL)
      log = wx.TextCtrl(panel,style = wx.TE_MULTILINE|wx.TE_READONLY|wx.HSCROLL)
      hbox2.Add(log, proportion=1, flag=wx.EXPAND)
      vbox.Add(hbox2,proportion=1,flag=wx.LEFT|wx.RIGHT|wx.EXPAND,border=10)
      vbox.Add((-1,25))


      #adding the 2 buttons for Server Status and start server
      hbox3 = wx.BoxSizer(wx.HORIZONTAL)
      serverStatus = wx.Button(panel, label='Server Status',size=(100,30))
      self.Bind(wx.EVT_BUTTON,self.btnStatus)
      hbox3.Add(serverStatus)
      startServer = wx.Button(panel, label='Start Server', size=(100,30))
      hbox3.Add(startServer, flag=wx.RIGHT|wx.BOTTOM, border=5)
      vbox.Add(hbox3,flag=wx.ALIGN_RIGHT|wx.RIGHT,border=10) 

      #Something Not sure. 
      panel.SetSizer(vbox)


      #Redirection to MessageBox
      redir=RedirectText(log)
      sys.stdout=redir

      self.CreateStatusBar()
      menuBar = wx.MenuBar()
      menu = wx.Menu()
      menu.Append(104, "&Open", "Select Directory to Crawl")
      menuBar.Append(menu, "&File")
      self.SetMenuBar(menuBar)

      self.Bind(wx.EVT_MENU, self.opendir, id=104)

    def OnClose(self,event):
      self.Close(True)

    def opendir(self,event):
        dlg = wx.DirDialog(self, "Choose a directory:", "~/", 0, (10,10), wx.Size(400,400))
        if dlg.ShowModal() == wx.ID_OK:
            self.SetStatusText('You selected: %s\n' % dlg.GetPath())
            crawl(dlg.GetPath())
        else:
            self.SetStatusText('Cancel')
        dlg.Destroy()

    def btnStatus(self,event):
      print 'Pinging Server Status:'
      


class MyApp(wx.App):
    def OnInit(self):
        myFrame = MyFrame(None, -1, "Pimp Daddy Yeezus")
        myFrame.CenterOnScreen()
        myFrame.Show(True)
        return True



class main():
    app = MyApp(0)
    app.MainLoop()


if __name__ == '__main__':
    main()
