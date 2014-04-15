//
//  PartyViewController.m
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "PartyViewController.h"
#import "SWRevealViewController.h"

#import "PassthroughView.h"

#import "VoteButton.h"

@interface PartyViewController ()

@end

@implementation PartyViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [_passthroughView setScrollView:_songScrollView];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)menuPressed:(id)sender {
    [self.revealViewController rightRevealToggleAnimated:YES];
}

@end
