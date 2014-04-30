import wx
import os, sys
from crawling import crawl


class MyFrame(wx.Frame):
    def __init__(self, parent, id, title):
      wx.Frame.__init__(self, parent, id, title)

      self.CreateStatusBar()
      menuBar = wx.MenuBar()
      menu = wx.Menu()
      menu.Append(104, "&Open", "Select Directory to Crawl")
      menuBar.Append(menu, "&File")
      self.SetMenuBar(menuBar)

      self.Bind(wx.EVT_MENU, self.opendir, id=104)

    
    def opendir(self,event):
        dlg = wx.DirDialog(self, "Choose a directory:", style=wx.DD_DEFAULT_STYLE | wx.DD_NEW_DIR_BUTTON)
        if dlg.ShowModal() == wx.ID_OK:
            self.SetStatusText('You selected: %s\n' % dlg.GetPath())
            execfile("crawling.py")
            crawl()
        else:
            print 'Cancel'
        dlg.Destroy()

class MyApp(wx.App):
    def OnInit(self):
        myFrame = MyFrame(None, -1, "Crawler Beta")
        myFrame.CenterOnScreen()
        myFrame.Show(True)
        return True

def main():
    app = MyApp(0)
    app.MainLoop()


if __name__ == '__main__':
    main()
