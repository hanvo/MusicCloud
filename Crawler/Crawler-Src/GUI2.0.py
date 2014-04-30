# Crawler GUI
# Features:
# -Selecting a folder to crawl
# -Outputs into the GUI window

# Need:
# -Clear Button
# -Run the Java Code
# -Load bar?
# -About Section

import wx
import os
import sys
from crawling import crawl

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
      panel = wx.Panel(self, wx.ID_ANY)
      log = wx.TextCtrl(panel,wx.ID_ANY, size=(150,100),  style = wx.TE_MULTILINE|wx.TE_READONLY|wx.HSCROLL)

      #widgets to the panel
      sizer = wx.BoxSizer(wx.VERTICAL)
      sizer.Add(log, 1, wx.ALL|wx.EXPAND, 5)
      panel.SetSizer(sizer)
      redir=RedirectText(log)
      sys.stdout=redir

      self.CreateStatusBar()
      menuBar = wx.MenuBar()
      menu = wx.Menu()
      menu.Append(104, "&Open", "Select Directory to Crawl")
      menuBar.Append(menu, "&File")
      self.SetMenuBar(menuBar)

      self.Bind(wx.EVT_MENU, self.opendir, id=104)

    def opendir(self,event):
        dlg = wx.DirDialog(self, "Choose a directory:", "~/", 0, (10,10), wx.Size(400,300))
        if dlg.ShowModal() == wx.ID_OK:
            self.SetStatusText('You selected: %s\n' % dlg.GetPath())
            crawl(dlg.GetPath())
        else:
            self.SetStatusText('Cancel')
        dlg.Destroy()

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
