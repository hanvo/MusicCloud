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
#import "SongScrollView.h"
#import "CurrentSongInfo.h"

@interface PartyViewController ()

@property (strong, nonatomic) ClientSession *session;

// array of SongInfo objects
@property (strong, nonatomic) NSMutableArray *songList;
@property (strong, nonatomic) CurrentSongInfo *currentSong;

@property (strong, nonatomic) NSTimer *timer;

@property (strong, nonatomic) NSIndexPath *selectedPath;

@end

@implementation PartyViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    _session = [ClientSession sharedSession];
    _session.delegate = self;
    
    [_passthroughView setScrollView:_songScrollView];
    
    [_progressLabel setText:@"0:00"];
    [_totalLabel setText:@"0:00"];
    
    [_progressView setCurrentTime:0];
    [_progressView setTotalTime:0];
    
    [_upVoteButton setEnabled:NO];
    [_upVoteButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_downVoteButton setEnabled:NO];
    [_downVoteButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    
    _selectedPath = nil;
    
    // make server requests
    
    [_session requestSongList];
    [_session requestSongUpdate];
    //[_session requestLikeUpdate];
    
    //[self performSelector:@selector(test) withObject:nil afterDelay:3];
    
    _timer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(tick) userInfo:nil repeats:YES];
    
    [_session startUpdateRequests];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    CGRect frame = _tableView.frame;
    frame.size.height = self.view.frame.size.height - frame.origin.y;
    [_tableView setFrame:frame];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    if (self.isMovingFromParentViewController) {
        [_session stopUpdateRequests];
        [_session deauthenticateClient];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)sortSongList {
    [_songList sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        SongInfo *s1 = obj1, *s2 = obj2;
        if (s1.votes == s2.votes)
            return [s1.songName compare:s2.songName];
        else
            return (s1.votes < s2.votes);
    }];
}

- (SongInfo *)songForSongID:(NSInteger)songID {
    for (SongInfo *song in _songList) {
        if (song.songID == songID) return song;
    }
    return nil;
}

- (void)test {
    [_session requestSongUpdate];
}

- (void)updateLabels:(CurrentSongInfo *)song {
    NSInteger secs = song.position;
    int mins = (int)((double)secs/60.0);
    secs -= mins*60.0;
    
    _progressLabel.text = [NSString stringWithFormat:@"%d:%02ld", mins, (long)secs];
    
    secs = song.songInfo.songLength;
    mins = (int)((double)secs/60.0);
    secs -= mins*60.0;
    
    _totalLabel.text = [NSString stringWithFormat:@"%d:%02ld", mins, (long)secs];
}

- (void)tick {
    if (_currentSong.songInfo.status == SS_PLAYING) {
        if (_currentSong.position < _currentSong.songInfo.songLength)
            _currentSong.position += 1;
    
        [self updateLabels:_currentSong];
        [_progressView setCurrentTime:_currentSong.position];
    }
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView setAllowsSelection:NO];
    
    _selectedPath = indexPath;
    
    SongInfo *song = _songList[indexPath.row];
    [_session sendVote:song.songID];
}

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
    cell.voteLabel.text = [NSString stringWithFormat:@"%ld", (long)info.votes];
    cell.idLabel.text = [NSString stringWithFormat:@"%ld", (long)info.songID];
    
    cell.albumImageView.image = info.albumArt;
    
    return cell;
}

#pragma mark - ClientSessionDelegate

- (void)clientDidReceiveSongList:(NSArray *)list {
    _songList = [NSMutableArray arrayWithArray:list];
    [self sortSongList];
    
    [self.tableView reloadData];
    
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0);
    dispatch_async(queue, ^{
        for (SongInfo *song in _songList) {
            [_session requestAlbumArtForSong:song];
            [NSThread sleepForTimeInterval:0.1];
        }
    });
}

- (void)clientDidReceiveLikeUpdate:(CurrentSongInfo *)song {
    _currentSong.likes = song.likes;
    _currentSong.dislikes = song.dislikes;
    _currentSong.balance = song.balance;
    
    [_voteMeterView setBalanceEnabled:YES];
    [_voteMeterView setBalance:_currentSong.balance animated:YES];
}

- (void)clientDidReceiveVoteUpdate:(NSArray *)song {
    for (SongInfo *info in song) {
        SongInfo *match = [self songForSongID:info.songID];
        match.votes = info.votes;
    }
    
    NSArray *oldSongList = [NSArray arrayWithArray:_songList];
    
    [self sortSongList];
    
    // update table view (with fanceyyyy animations)
    [_tableView beginUpdates];
    NSMutableArray *newIPS = [NSMutableArray arrayWithCapacity:_songList.count];
    for (int i = 0; i < _songList.count; i++) {
        NSInteger newRow = [_songList indexOfObject:oldSongList[i]];
        NSIndexPath *oldIP = [NSIndexPath indexPathForRow:i inSection:0];
        NSIndexPath *newIP = [NSIndexPath indexPathForRow:newRow inSection:0];
        if ([_selectedPath isEqual:oldIP])
            _selectedPath = newIP;
        [newIPS addObject:newIP];
        
        [_tableView moveRowAtIndexPath:oldIP toIndexPath:newIP];
    }
    [_tableView endUpdates];
    
    [_tableView reloadRowsAtIndexPaths:newIPS withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([indexPath isEqual:_selectedPath]) {
        [cell setSelected:YES animated:NO];
    }
}

- (void)clientDidReceiveSongUpdate:(CurrentSongInfo *)song {
    BOOL newSong = YES;
    if (_songList.count > 0) {
        SongInfo *current = [_songList lastObject];
        newSong = (current.songID != song.songInfo.songID);
    }
    
    if (newSong) {
        SongInfo *match = [self songForSongID:song.songInfo.songID];
        if (match) { // already found album art
            song.songInfo.albumArt = match.albumArt;
        }
        
        //[_songList addObject:song.songInfo];
        _currentSong = song;
        
        [_songScrollView addSong:song.songInfo animated:YES];
        
        [self updateLabels:song];
        [_downVoteButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_upVoteButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_downVoteButton setEnabled:YES];
        [_upVoteButton setEnabled:YES];
        
        [_progressView setTotalTime:song.songInfo.songLength];
        [_progressView setCurrentTime:song.position];
    }
}

- (void)clientDidReceiveAlbumArt:(UIImage *)image forSong:(SongInfo *)song {
    song.albumArt = image;
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:[_songList indexOfObject:song] inSection:0];
    [self.tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
    
    if (song.songID == _currentSong.songInfo.songID) {
        _currentSong.songInfo.albumArt = image;
        [_songScrollView updateCurrentSong];
    }
}

- (void)clientDidReceiveFailure:(NSString *)message {
    NSString *msg = [NSString stringWithFormat:@"Error communicating with DJ: %@", message];
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"MusicCloud" message:msg delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
    [alert show];
}

- (void)clientDidFailTask:(NSURLSessionDataTask *)task error:(NSError *)err {
    NSLog(@"Task failed: %@", task.currentRequest.URL);
    NSLog(@"Failed with err: %@", err);
}

#pragma mark - IBAction

- (IBAction)menuPressed:(id)sender {
    [self.revealViewController rightRevealToggleAnimated:YES];
}

- (IBAction)downVotePressed:(id)sender {
    [_session sendDislike:_currentSong.songInfo.songID];
    
    [_downVoteButton setBackgroundColor:[UIColor redColor]];
    [_downVoteButton setEnabled:NO];
    [_upVoteButton setEnabled:NO];
}

- (IBAction)upVotePressed:(id)sender {
    [_session sendLike:_currentSong.songInfo.songID];
    
    [_upVoteButton setBackgroundColor:[UIColor greenColor]];
    [_downVoteButton setEnabled:NO];
    [_upVoteButton setEnabled:NO];
}

@end
