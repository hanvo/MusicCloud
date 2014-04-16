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
#import "VoteMeterView.h"
#import "SongProgressView.h"
#import "SongTableViewCell.h"
#import "SongInfo.h"

@interface PartyViewController ()

@property (strong, nonatomic) ClientSession *session;

@property (strong, nonatomic) NSMutableArray *songList;

@end

@implementation PartyViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    _session = [ClientSession sharedSession];
    _session.delegate = self;
    
    [_passthroughView setScrollView:_songScrollView];
    
    [_voteMeterView setBalance:-1.0];
    [_progressLabel setText:@"0:00"];
    [_totalLabel setText:@"0:00"];
    
    [_progressView setCurrentTime:0];
    [_progressView setTotalTime:0];
    
    [_session requestSongList];
    [_session requestUpdate];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - UITableViewDelegate

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return _songList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    SongTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"SongCell"];
    
    SongInfo *info = _songList[indexPath.row];
    cell.nameLabel.text = info.songName;
    cell.artistLabel.text = info.songArtist;
    cell.voteLabel.text = [NSString stringWithFormat:@"%d", info.votes];
    
    return cell;
}

#pragma mark - ClientSessionDelegate

- (void)clientDidReceiveSongList:(NSArray *)list {
    _songList = [NSMutableArray arrayWithArray:list];
    [_songList sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        SongInfo *s1 = obj1, *s2 = obj2;
        if (s1.votes == s2.votes) {
            return [s1.songName compare:s2.songName];
        } else
            return (s1.votes < s2.votes);
    }];
    [self.tableView reloadData];
}

- (void)clientDidReceiveLikeUpdate:(NSDictionary *)likeInfo {
    
}

- (void)clientDidReceiveSongUpdate:(NSDictionary *)songInfo {
    
}

- (void)clientDidReceiveVoteUpdate:(NSDictionary *)voteInfo {
    
}

- (void)clientDidReceiveFailure:(NSString *)message {
    
}

#pragma mark - IBAction

- (IBAction)menuPressed:(id)sender {
    [self.revealViewController rightRevealToggleAnimated:YES];
}

- (IBAction)downVotePressed:(id)sender {
    
}

- (IBAction)upVotePressed:(id)sender {
    
}

@end
