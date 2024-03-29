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
import httplib
import json


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
      global log
      log = wx.TextCtrl(panel,style = wx.TE_MULTILINE|wx.TE_READONLY|wx.HSCROLL)
      hbox2.Add(log, proportion=1, flag=wx.EXPAND)
      vbox.Add(hbox2,proportion=1,flag=wx.LEFT|wx.RIGHT|wx.EXPAND,border=10)
      vbox.Add((-1,25))


      #adding the 2 buttons for Server Status and start server
      hbox3 = wx.BoxSizer(wx.HORIZONTAL)
      serverStatus = wx.Button(panel, label='Server Status',size=(100,30))
      self.Bind(wx.EVT_BUTTON,self.btnStatus)
      hbox3.Add(serverStatus)
      vbox.Add(hbox3,flag=wx.ALIGN_RIGHT|wx.RIGHT,border=10) 

      #Something Not sure. 
      panel.SetSizer(vbox)


      #Redirection to MessageBox
      redir=RedirectText(log)
      sys.stdout=redir

      self.CreateStatusBar()
      menuBar = wx.MenuBar()
      menu = wx.Menu()
      view = wx.Menu()
      menu.Append(104, "&Open", "Select Directory to Crawl")
      menu.Append(105, "&Clear", "Clear Message Box")
      menu.Append(106, "&Exit", "Exit Program")
      view.Append(100, "&About", "About")
      menuBar.Append(menu, "&File")
      menuBar.Append(view, "&Project")
      self.SetMenuBar(menuBar)


      self.Bind(wx.EVT_MENU, self.opendir, id=104)
      self.Bind(wx.EVT_MENU, self.clearMess, id=105)
      self.Bind(wx.EVT_MENU, self.exitProg, id=106)
      self.Bind(wx.EVT_MENU, self.OnAboutBox, id=100)

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
      print 'Pinging Server Status...'
      try:
        timeout = 50
        http = httplib.HTTPConnection('klamath.dnsdynamic.com',5050, timeout = timeout)
        socket.setdefaulttimeout(timeout)

        server_response = send_request(http, "autenticate",{},{"pin":1234},"POST")
        server_response = json.loads(server_response.read())
        client_id = server_response['id']

        send_request(http,"deauthenticate", {"clientID": client_id},{"id": client_id}, "POST")
        http.close()

        print "Server Online"
      except Exception, e:
        print "Server Offline"

    def clearMess(self,event):
      self.SetStatusText('Message Box Cleared')
      log.Clear()

    def exitProg(self,event):
      self.Close()

    def OnAboutBox(self,event):
      description = """Crawls through a selected file recursively. 
It searches for Mp3 files and if true stores meta data into a local db file. 
Computer Science 2014 Spring 
Professor Li 
April - May """
      licence = """(C) NO RE-USEARINO. BAD CODE. Trololol Don Phan"""
      info = wx.AboutDialogInfo()
      info.SetIcon(wx.Icon('icon-120.png', wx.BITMAP_TYPE_ANY))
      info.SetName('Daddy Yeezy')
      info.SetVersion('1.0')
      info.SetDescription(description)
      info.SetLicence(licence)
      info.AddDeveloper('Don Phan - phand@purdue.edu - Crawler')
      info.AddDeveloper('Jason Rahman - rahmanj@purdue.edu - Server ')
      info.AddDeveloper('Anant Goel - goela@purdue.edu - Raspberry Pi ')
      info.AddDeveloper('Josh Selbo - jselbo@purdue.edu - iOS Dev')


      wx.AboutBox(info)


class MyApp(wx.App):
    def OnInit(self):
        myFrame = MyFrame(None, -1, "Daddy Yeezus")
        myFrame.CenterOnScreen()
        myFrame.Show(True)
        return True



class main():
    app = MyApp(0)
    app.MainLoop()


if __name__ == '__main__':
    main()
