//
//  NetworkManager.m
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "ClientSession.h"
#import "SongInfo.h"
#import "CurrentSongInfo.h"
#import "ImageFetchSession.h"

@interface ClientSession ()

@property (strong, nonatomic) ImageFetchSession *imageFetchSession;

@property (nonatomic) BOOL requestingUpdates;

@end

@implementation ClientSession

- (id)initWithBaseURL:(NSURL *)url {
    if (self = [super initWithBaseURL:url]) {
        self.requestSerializer = [AFJSONRequestSerializer serializer];
        self.requestSerializer.timeoutInterval = 30;
        
        self.responseSerializer = [AFJSONResponseSerializer serializerWithReadingOptions:NSJSONReadingAllowFragments];
        
        _imageFetchSession = [ImageFetchSession sharedSession];
        
        _requestingUpdates = NO;
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

- (NSString *)createURLQuery:(NSString *)query {
    return [NSString stringWithFormat:@"%@?clientID=%ld", query, (long)_clientID];
}

- (void)handleUpdateResponse:(id)response {
    NSString *updateType = [response objectForKey:@"update_type"];
    if ([updateType isEqualToString:@"song_list"]) {
        NSArray *songList = [response objectForKey:@"values"];
        
        NSMutableArray *songs = [NSMutableArray arrayWithCapacity:songList.count];
        for (NSDictionary *dict in songList) {
            SongInfo *song = [[SongInfo alloc] init];
            song.songID = [[dict objectForKey:@"id"] integerValue];
            song.songName = [dict objectForKey:@"name"];
            if ([[song.songName substringFromIndex:song.songName.length-4] isEqualToString:@".mp3"])
                song.songName = [song.songName substringToIndex:song.songName.length-4];
            song.songArtist = [dict objectForKey:@"artist"];
            song.songAlbum = [dict objectForKey:@"album"];
            song.votes = [[dict objectForKey:@"votes"] integerValue];
            [songs addObject:song];
        }
        
        if ([_delegate respondsToSelector:@selector(clientDidReceiveSongList:)]) {
            [_delegate clientDidReceiveSongList:songs];
            NSLog(@"clientDidReceiveSongList");
        }
    } else if ([updateType isEqualToString:@"likes"]) {
        CurrentSongInfo *songInfo = [[CurrentSongInfo alloc] init];
        NSDictionary *likeInfo = [response objectForKey:@"values"];
        songInfo.songInfo.songID = [[likeInfo objectForKey:@"id"] integerValue];
        songInfo.likes = [[likeInfo objectForKey:@"likes"] integerValue];
        songInfo.dislikes = [[likeInfo objectForKey:@"dislikes"] integerValue];
        songInfo.balance = [[likeInfo objectForKey:@"balance"] doubleValue];
        
        if ([_delegate respondsToSelector:@selector(clientDidReceiveLikeUpdate:)]) {
            [_delegate clientDidReceiveLikeUpdate:songInfo];
            NSLog(@"clientDidReceiveLikeUpdate");
        }
    } else if ([updateType isEqualToString:@"votes"]) {
        NSMutableArray *votes = [NSMutableArray array];
        NSArray *voteDicts = [response objectForKey:@"values"];
        for (NSDictionary *dict in voteDicts) {
            SongInfo *info = [[SongInfo alloc] init];
            info.songID = [[dict objectForKey:@"id"] integerValue];
            info.votes = [[dict objectForKey:@"votes"] integerValue];
            [votes addObject:info];
        }
        if ([_delegate respondsToSelector:@selector(clientDidReceiveVoteUpdate:)]) {
            [_delegate clientDidReceiveVoteUpdate:votes];
            NSLog(@"clientDidReceiveVoteUpdate");
        }
    } else if ([updateType isEqualToString:@"current_song"]) {
        NSDictionary *values = [response objectForKey:@"values"];
        
        NSLog(@"vals %@", values);
        
        CurrentSongInfo *song = [[CurrentSongInfo alloc] init];
        song.songInfo.songID = [[values objectForKey:@"id"] integerValue];
        song.songInfo.songName = [values objectForKey:@"name"];
        if ([[song.songInfo.songName substringFromIndex:song.songInfo.songName.length-4] isEqualToString:@".mp3"])
            song.songInfo.songName = [song.songInfo.songName substringToIndex:song.songInfo.songName.length-4];
        song.songInfo.songArtist = [values objectForKey:@"artist"];
        song.songInfo.songAlbum = [values objectForKey:@"album"];
        song.songInfo.songLength = [[values objectForKey:@"length"] integerValue];
        NSString *status = [values objectForKey:@"status"];
        if ([status isEqualToString:@"Playing"])
            song.songInfo.status = SS_PLAYING;
        else if ([status isEqualToString:@"Paused"])
            song.songInfo.status = SS_PAUSED;
        else
            NSLog(@"Unknown song status: %@", status);
        song.position = [[values objectForKey:@"position"] integerValue];
        
        NSLog(@"song update %@", song);
        if ([_delegate respondsToSelector:@selector(clientDidReceiveSongUpdate:)]) {
            [_delegate clientDidReceiveSongUpdate:song];
            NSLog(@"clientDidReceiveSongUpdate");
        }

    } else {
        NSLog(@"Error - unknown update type from server.");
        NSLog(@"Response: %@", response);
    }
}

#pragma mark - Client to server messages

- (void)authenticateClient:(NSString *)pin {
    if (OFFLINE) {
        _clientID = 123456;
        _authenticated = YES;
        if ([_delegate respondsToSelector:@selector(clientDidAuthenticate:)]) [_delegate clientDidAuthenticate:YES];
        return;
    }
    
    NSDictionary *params = @{@"pin": @([pin integerValue])};
    [self POST:@"client/authenticate" parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        NSInteger clientID = [[responseObject objectForKey:@"id"] integerValue];
        _clientID = clientID;
        
        NSLog(@"CLIENT ID %ld", (long)clientID);
        
        _authenticated = YES;
        if ([_delegate respondsToSelector:@selector(clientDidAuthenticate:)])
            [_delegate clientDidAuthenticate:YES];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)deauthenticateClient {
    if (OFFLINE) {
        _clientID = -1;
        return;
    }
    
    NSDictionary *params = @{@"id": @(_clientID)};
    [self POST:@"client/deauthenticate" parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        _clientID = -1;
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
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
        if ([_delegate respondsToSelector:@selector(clientDidReceiveSongList:)])
            [_delegate clientDidReceiveSongList:list];
        return;
    }
    
    NSString *query = [self createURLQuery:@"client/request_song_list"];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        [self handleUpdateResponse:responseObject];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)sendVote:(NSInteger)songID {
    if (OFFLINE) {
        return;
    }
    
    NSString *query = [self createURLQuery:@"client/vote"];
    NSDictionary *params = @{@"id": @(songID)};
    [self POST:query parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        // Do nothing
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)sendLike:(NSInteger)songID {
    if (OFFLINE) {
        return;
    }
    
    NSString *query = [self createURLQuery:@"client/like"];
    NSDictionary *params = @{@"id": @(songID)};
    [self POST:query parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)sendDislike:(NSInteger)songID {
    if (OFFLINE) {
        return;
    }
    
    NSString *query = [self createURLQuery:@"client/dislike"];
    NSDictionary *params = @{@"id": @(songID)};
    [self POST:query parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)requestLikeUpdate {
    if (OFFLINE) {
        
        return;
    }
    
    NSString *query = [self createURLQuery:@"client/request_like_update"];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)updateRequestLoop {
    NSString *query = [self createURLQuery:@"client/request_update"];
    NSLog(@"request update");
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        NSLog(@"update success");
        [self handleUpdateResponse:responseObject];
        
        if (_requestingUpdates)
            [self performSelectorInBackground:@selector(updateRequestLoop) withObject:nil];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if (error.code == -1001) { // timeout error code (these are normal)
            if (_requestingUpdates)
                [self performSelectorInBackground:@selector(updateRequestLoop) withObject:nil];
        } else {
            if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
                [_delegate clientDidFailTask:task error:error];
            
            NSLog(@"Stop requesting updates...");
            
            _requestingUpdates = NO;
            
            //if (_requestingUpdates)
                //[self updateRequestLoop];
        }
    }];
}

- (void)startUpdateRequests {
    if (OFFLINE) {
        
        return;
    }
    
    if (!_requestingUpdates) {
        _requestingUpdates = YES;
        
        [self updateRequestLoop];
    }
}

- (void)stopUpdateRequests {
    if (OFFLINE) {
        return;
    }
    
    if (_requestingUpdates) {
        _requestingUpdates = NO;
        // will end update request loop
    }
}

- (void)requestVoteUpdate {
    if (OFFLINE) {
        
        return;
    }
    
    NSString *query = [self createURLQuery:@"request_vote_update"];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)requestSongUpdate {
    if (OFFLINE) {
        static int count = 0;
        CurrentSongInfo *song = [[CurrentSongInfo alloc] init];

        if (count == 0) {
            song.songInfo.songID = 1;
            song.songInfo.songName = @"Your Guardian Angel";
            song.songInfo.songArtist = @"Red Jumpsuit Apparatus";
            song.songInfo.songAlbum = @"Don't You Fake It";
            song.songInfo.songLength = 180;
            song.songInfo.status = SS_PLAYING;
            song.position = 0;
            count++;
        } else if (count == 1) {
            song.songInfo.songID = 2;
            song.songInfo.songName = @"Dark Horse";
            song.songInfo.songArtist = @"Katy Perry";
            song.songInfo.songAlbum = @"Prism";
            song.songInfo.songLength = 160;
            song.songInfo.status = SS_PLAYING;
            song.position = 12;
            count = 0;
        }
        
        if ([_delegate respondsToSelector:@selector(clientDidReceiveSongUpdate:)])
            [_delegate clientDidReceiveSongUpdate:song];
        return;
    }
    
    NSString *query = [self createURLQuery:@"client/request_song_update"];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        [self handleUpdateResponse:responseObject];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)requestAlbumArtForSong:(SongInfo *)song {
    if (OFFLINE) {
        return;
    }
    
    NSString *songIDParam = [NSString stringWithFormat:@"&songID=%ld", (long)song.songID];
    NSString *query = [[self createURLQuery:@"client/request_photo"] stringByAppendingString:songIDParam];
    
    [_imageFetchSession GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        UIImage *image = (UIImage *)responseObject;
        if (!image) {
            NSLog(@"received null image for songid %ld", (long)song.songID);
            NSLog(@"response %@", task.response);
        }
        if ([_delegate respondsToSelector:@selector(clientDidReceiveAlbumArt:forSong:)])
            [_delegate clientDidReceiveAlbumArt:image forSong:song];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([_delegate respondsToSelector:@selector(clientDidFailTask:error:)])
            [_delegate clientDidFailTask:task error:error];
    }];
}

@end
