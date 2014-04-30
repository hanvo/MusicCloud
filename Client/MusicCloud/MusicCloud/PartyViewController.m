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

@interface PartyViewController ()

@property (strong, nonatomic) ClientSession *session;

// array of SongInfo objects
@property (strong, nonatomic) NSMutableArray *songList;
@property (strong, nonatomic) SongInfo *currentSong;

@property (strong, nonatomic) NSTimer *timer;

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
    //[_session requestSongUpdate];
    //[_session requestLikeUpdate];
    
    //[self performSelector:@selector(test) withObject:nil afterDelay:3];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)test {
    [_session requestSongUpdate];
}

- (void)updateLabels:(SongInfo *)song {
    NSInteger secs = song.position;
    int mins = (int)((double)secs/60.0);
    secs -= mins*60.0;
    
    _progressLabel.text = [NSString stringWithFormat:@"%d:%02d", mins, secs];
    
    secs = song.songLength;
    mins = (int)((double)secs/60.0);
    secs -= mins*60.0;
    
    _totalLabel.text = [NSString stringWithFormat:@"%d:%02d", mins, secs];
}

- (void)tick {
    if (_currentSong.position < _currentSong.songLength)
        _currentSong.position += 1;
    
    [self updateLabels:_currentSong];
    [_progressView setCurrentTime:_currentSong.position];
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
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
    cell.voteLabel.text = [NSString stringWithFormat:@"%d", info.votes];
    
    NSLog(@"image %@", info.albumArt);
    cell.albumImageView.image = info.albumArt;
    
    return cell;
}

#pragma mark - ClientSessionDelegate

- (void)clientDidReceiveSongList:(NSArray *)list {
    _songList = [NSMutableArray arrayWithArray:list];
    [_songList sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        SongInfo *s1 = obj1, *s2 = obj2;
        if (s1.votes == s2.votes)
            return [s1.songName compare:s2.songName];
        else
            return (s1.votes < s2.votes);
    }];
    
    [self.tableView reloadData];
    
    for (SongInfo *song in _songList) {
        [_session requestAlbumArtForSong:song];
    }
}

- (void)clientDidReceiveLikeUpdate:(SongInfo *)song {
    
}

- (void)clientDidReceiveSongUpdate:(SongInfo *)song {
    BOOL newSong = YES;
    if (_songList.count > 0) {
        SongInfo *current = [_songList lastObject];
        newSong = (current.songID != song.songID);
    }
    
    if (newSong) {
        [_songList addObject:song];
        _currentSong = song;
        
        [_songScrollView addSong:song animated:YES];
        
        [self updateLabels:song];
        [_downVoteButton setBackgroundColor:[UIColor blackColor]];
        [_upVoteButton setBackgroundColor:[UIColor blackColor]];
        [_downVoteButton setEnabled:YES];
        [_upVoteButton setEnabled:YES];
        
        [_progressView setTotalTime:song.songLength];
        [_progressView setCurrentTime:song.position];
        
        if (_timer) {
            [_timer invalidate];
        }
        _timer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(tick) userInfo:nil repeats:YES];
    }
}

- (void)clientDidReceiveVoteUpdate:(SongInfo *)song {
    
}

- (void)clientDidReceiveAlbumArt:(UIImage *)image forSong:(SongInfo *)song {
    song.albumArt = image;
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:[_songList indexOfObject:song] inSection:0];
    NSLog(@"ip %@", indexPath);
    [self.tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (void)clientDidReceiveFailure:(NSString *)message {
    NSString *msg = [NSString stringWithFormat:@"Error communicating with DJ: %@", message];
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"MusicCloud" message:msg delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
    [alert show];
}

- (void)clientDidFailTask:(NSURLSessionDataTask *)task error:(NSError *)err {
    NSLog(@"Failed task: %@", task);
    NSLog(@"Error: %@", err);
}

#pragma mark - IBAction

- (IBAction)menuPressed:(id)sender {
    [self.revealViewController rightRevealToggleAnimated:YES];
}

- (IBAction)downVotePressed:(id)sender {
    [_session sendDislike:_currentSong.songID];
    
    [_downVoteButton setBackgroundColor:[UIColor redColor]];
    [_downVoteButton setEnabled:NO];
    [_upVoteButton setEnabled:NO];
}

- (IBAction)upVotePressed:(id)sender {
    [_session sendLike:_currentSong.songID];
    
    [_upVoteButton setBackgroundColor:[UIColor greenColor]];
    [_downVoteButton setEnabled:NO];
    [_upVoteButton setEnabled:NO];
}

@end
