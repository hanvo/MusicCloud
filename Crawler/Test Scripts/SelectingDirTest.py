import wx
class MyApp(wx.App):
    def OnInit(self):
        # Args below are: parent, question, dialog title, default answer
        dd = wx.DirDialog(None, "Select directory to open", "~/", 0, (10, 10), wx.Size(400, 300))

        # This function returns the button pressed to close the dialog
        ret = dd.ShowModal()

        # Let's check if user clicked OK or pressed ENTER
        if ret == wx.ID_OK:
            print('You selected: %s\n' % dd.GetPath())
        else:
            print('You clicked cancel')

        # The dialog is not in the screen anymore, but it's still in memory
        #for you to access it's values. remove it from there.
        dd.Destroy()
        return True

# Always use zero here. otherwise, you will have an error window that will last only nano seconds
# on the screen. dumb.
# (Only if the error is in the startup code before MainLoop is called. --RobinDunn)
app = MyApp(redirect = 0)
app.MainLoop()
