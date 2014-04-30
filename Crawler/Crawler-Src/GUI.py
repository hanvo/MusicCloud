import wx

class MyFrame(wx.Frame):
    def __intit__(self, parent, id, title):
        wx.Frame.__init__(self, parent, id, title)
        menubar = wx.MenuBar() #whole task bar

        #File sub menu
        fileMenu = wx.Menu()
        openItem = fileMenu.Append(wx.ID_OPEN, "&Open")
        aboutItem = fileMenu.Append(wx.ID_ABOUT, "&About", "Crawler GUI")
        fitem = fileMenu.Append(wx.ID_EXIT, 'Quit', 'Quit application')

        menubar.Append(fileMenu, '&File')
        self.SetMenuBar(menubar)
        
        self.Bind(wx.EVT_MENU,self.onQuit,fitem)
        self.Bind(wx.EVT_MENU,self.onAbout,aboutItem)
        self.Bind(wx.EVT_MENU,self.onOpen,openItem)
        
        self.icon = wx.Icon('icon-120.png',wx.BITMAP_TYPE_ANY)
        self.SetIcon(self.icon)
        self.SetSize((300, 200))
        self.SetTitle('Crawler 0.1 BETA')
        self.Center()
        self.Show(True)
        
    def onQuit(self, e):
        self.Close()

    def onAbout(self,event):
        dlg = wx.MessageDialog(self, "Crawler GUI", "Cralwer .1 Beta",wx.OK)
        dlg.ShowModal()
        dlg.Destroy() #KILL IT
    
    def onOpen(self,event):
        #Args of DirDialog(Parent, Question, Dialong Title, Default Answer)
        dlg = wx.DirDialog(None, "Select Crawling Folder", "~/", 0, (10,10), wx.Size(400,300))
        ret = dlg.ShowModal()

        #Check if you Pressed OK or Pressed Enter
        if ret == wx.ID_OK:
            print 'Selected: ' + dlg.GetPath()
        else:
            print 'Cancelled'

        dlg.Destroy()
        return True


class crawlerGUI(wx.Frame):
    def __init__(self, *args, **kwargs):
        super(crawlerGUI, self).__init__(*args, **kwargs) 
        self.InitUI()
        
    def InitUI(self):    
        

class MyApp(wx.App):
    def OnInit(self):
        myFrame = MyFrame(None, -1, "Testing")
        myFrame.CenterOnScreen()
        myFrame.Show(True)
        return True


def main():
    
    app = myApp(0)
    app.MainLoop()


if __name__ == '__main__':
    main()
