//
//  NetworkManager.m
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "ClientSession.h"
#import "SongInfo.h"

// if set, does not make HTTP requests.
// all requests return successfully
#define OFFLINE 1

#define BASE_URL @"http://192.168.1.100:1234"

@interface ClientSession ()

@end

@implementation ClientSession

- (id)initWithBaseURL:(NSURL *)url {
    if (self = [super initWithBaseURL:url]) {
        
    }
    return self;
}

+ (instancetype)sharedSession {
    static ClientSession *sharedManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSURL *url = [NSURL URLWithString:BASE_URL];
        sharedManager = [[ClientSession alloc] initWithBaseURL:url];
    });
    return sharedManager;
}

- (void)authenticateClient:(NSString *)pin {
    if (OFFLINE) {
        _clientID = 123456;
        [_delegate clientDidAuthenticate:YES];
        return;
    }
    
    NSDictionary *params = @{@"Pin": pin};
    
    [self POST:@"authenticate_client" parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        NSInteger clientID = [[responseObject objectForKey:@"ClientID"] integerValue];
        _clientID = clientID;
        
        [_delegate clientDidAuthenticate:YES];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidAuthenticate:NO];
    }];
}

- (void)deauthenticateClient {
    if (OFFLINE) {
        _clientID = -1;
        return;
    }
    
    NSDictionary *params = @{ @"ClientID": [NSString stringWithFormat:@"%d", _clientID]};
    [self POST:@"deauthenticate_client" parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        _clientID = -1;
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidReceiveFailure:@"Deauthenticate failed"];
    }];
}

- (void)requestSongList {
    if (OFFLINE) {
        SongInfo *song1 = [[SongInfo alloc] init];
        song1.songID = 1;
        song1.songName = @"Your Guardian Angel";
        song1.songArtist = @"Red Jumpsuit Apparatus";
        song1.songAlbum = @"Don't You Fake It";
        song1.votes = 8;
        SongInfo *song2 = [[SongInfo alloc] init];
        song2.songID = 2;
        song2.songName = @"Dark Horse";
        song2.songArtist = @"Katy Perry";
        song2.songAlbum = @"Prism";
        song2.votes = 11;
        SongInfo *song3 = [[SongInfo alloc] init];
        song3.songID = 3;
        song3.songName = @"Clocks";
        song3.songArtist = @"Coldplay";
        song3.songAlbum = @"A Rush of Blood to the Head";
        song3.votes = 12;
        NSArray *list = @[song1, song2, song3];
        [_delegate clientDidReceiveSongList:list];
        return;
    }
    
    [self GET:@"request_song_list" parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        NSArray *infoDicts = [responseObject objectForKey:@"songs"];
        
        NSMutableArray *songs = [NSMutableArray arrayWithCapacity:infoDicts.count];
        for (NSDictionary *dict in infoDicts) {
            SongInfo *song = [[SongInfo alloc] init];
            song.songID = [[dict objectForKey:@"SongID"] integerValue];
            song.songName = [dict objectForKey:@"SongName"];
            song.songArtist = [dict objectForKey:@"SongArtist"];
            song.songAlbum = [dict objectForKey:@"SongAlbum"];
            song.votes = [[dict objectForKey:@"Votes"] integerValue];
            [songs addObject:song];
        }
        
        [_delegate clientDidReceiveSongList:songs];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidReceiveFailure:@"Request song list failed"];
    }];
}

- (void)sendVote:(NSInteger)songID {
    
}

- (void)sendLike:(NSInteger)songID {
    
}

- (void)sendDislike:(NSInteger)songID {
    
}

- (void)requestUpdate {
    [self requestLikeUpdate];
    [self requestVoteUpdate];
    [self requestSongUpdate];
}

- (void)requestLikeUpdate {
    
}

- (void)requestVoteUpdate {
    
}

- (void)requestSongUpdate {
    
}

@end
