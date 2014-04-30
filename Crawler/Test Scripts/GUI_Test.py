import wx

class crawlerGUI(wx.Frame):
    
    def __init__(self, *args, **kwargs):
        super(crawlerGUI, self).__init__(*args, **kwargs) 
            
        self.InitUI()
        
    def InitUI(self):    
        menubar = wx.MenuBar()
        fileMenu = wx.Menu()
        
        aboutItem = fileMenu.Append(wx.ID_ABOUT, "&About", "Crawler GUI")
        fitem = fileMenu.Append(wx.ID_EXIT, 'Quit', 'Quit application')
        menubar.Append(fileMenu, '&File')
        self.SetMenuBar(menubar)
        
        self.Bind(wx.EVT_MENU, self.OnQuit, fitem)
        self.Bind(wx.EVT_MENU,self.onAbout,aboutItem)

        self.SetSize((300, 200))
        self.SetTitle('Crawler 0.1 BETA')
        self.Centre()
        self.Show(True)
        
    def OnQuit(self, e):
        self.Close()

    def onAbout(self,event):
        dlg = wx.MessageDialog(self, "Crawler GUI", "Cralwer .1 Beta",wx.OK)
        dlg.ShowModal()
        dlg.Destroy() #KILL IT 

def main():
    
    ex = wx.App()
    crawlerGUI(None)
    ex.MainLoop()    


if __name__ == '__main__':
    main()
