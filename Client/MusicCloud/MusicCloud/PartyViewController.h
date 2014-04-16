//
//  PartyViewController.h
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ClientSession.h"

@class PassthroughView;
@class VoteButton;
@class VoteMeterView;
@class SongProgressView;

@interface PartyViewController : UIViewController <UITableViewDelegate, UITableViewDataSource, ClientSessionDelegate>

@property (weak, nonatomic) IBOutlet VoteButton *downVoteButton;
@property (weak, nonatomic) IBOutlet VoteButton *upVoteButton;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIScrollView *songScrollView;
@property (weak, nonatomic) IBOutlet PassthroughView *passthroughView;
@property (weak, nonatomic) IBOutlet VoteMeterView *voteMeterView;
@property (weak, nonatomic) IBOutlet UILabel *progressLabel;
@property (weak, nonatomic) IBOutlet UILabel *totalLabel;
@property (weak, nonatomic) IBOutlet SongProgressView *progressView;

- (IBAction)menuPressed:(id)sender;
- (IBAction)downVotePressed:(id)sender;
- (IBAction)upVotePressed:(id)sender;

@end
