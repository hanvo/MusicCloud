//
//  AppDelegate.m
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "AppDelegate.h"

#import "ClientSession.h"

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // UIAppearance customization
    [[UINavigationBar appearance] setTintColor:[UIColor whiteColor]];
    [[UINavigationBar appearance] setBarTintColor:[UIColor colorWithWhite:0.15 alpha:1.0]];
    
    NSDictionary *titleTextAttrs = @{ NSForegroundColorAttributeName: [UIColor whiteColor] };
    [[UINavigationBar appearance] setTitleTextAttributes:titleTextAttrs];
    
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    
    // Override point for customization after application launch.
    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    ClientSession *session = [ClientSession sharedSession];
    if (session.isAuthenticated) {
        NSLog(@"Deauthenticating client with clientID %ld", (long)session.clientID);
        [session deauthenticateClient];
    }
}

@end
