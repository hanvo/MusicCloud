//
//  PartyViewController.h
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>

@class PassthroughView;
@class VoteButton;

@interface PartyViewController : UIViewController

@property (weak, nonatomic) IBOutlet VoteButton *downVoteButton;
@property (weak, nonatomic) IBOutlet VoteButton *upVoteButton;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIScrollView *songScrollView;
@property (weak, nonatomic) IBOutlet PassthroughView *passthroughView;

- (IBAction)menuPressed:(id)sender;

@end
