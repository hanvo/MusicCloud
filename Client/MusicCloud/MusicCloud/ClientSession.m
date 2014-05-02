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
#define OFFLINE 0

#define BASE_URL @"http://128.211.222.68:5050"

@interface ClientSession ()

@end

@implementation ClientSession

- (id)initWithBaseURL:(NSURL *)url {
    if (self = [super initWithBaseURL:url]) {
        self.requestSerializer = [AFJSONRequestSerializer serializer];
        self.responseSerializer = [AFJSONResponseSerializer serializerWithReadingOptions:NSJSONReadingAllowFragments];
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
    return [NSString stringWithFormat:@"%@?clientID=%d", query, _clientID];
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
            song.songArtist = [dict objectForKey:@"artist"];
            song.songAlbum = [dict objectForKey:@"album"];
            song.votes = [[dict objectForKey:@"votes"] integerValue];
            [songs addObject:song];
        }
        
        [_delegate clientDidReceiveSongList:songs];
    } else if ([updateType isEqualToString:@"likes"]) {
        NSArray *likeInfos = [response objectForKey:@"values"];
        for (NSDictionary *likeInfo in likeInfos) {
            NSInteger songID = [[likeInfo objectForKey:@"id"] integerValue];
            NSInteger likes = [[likeInfo objectForKey:@"likes"] integerValue];
            NSInteger dislikes = [[likeInfo objectForKey:@"dislikes"] integerValue];
        }
        
        //[_delegate clientDidReceiveLikeUpdate:<#(SongInfo *)#>]
    } else if ([updateType isEqualToString:@"votes"]) {
        
    } else if ([updateType isEqualToString:@"current_song"]) {
        NSDictionary *values = [response objectForKey:@"values"];
        
        SongInfo *song = [[SongInfo alloc] init];
        song.songID = [[values objectForKey:@"id"] integerValue];
        song.songName = [values objectForKey:@"name"];
        song.songArtist = [values objectForKey:@"artist"];
        song.songAlbum = [values objectForKey:@"album"];
        song.songLength = [[values objectForKey:@"length"] integerValue];
        NSString *status = [values objectForKey:@"status"];
        if ([status isEqualToString:@"Playing"])
            song.status = SS_PLAYING;
        else if ([status isEqualToString:@"Paused"])
            song.status = SS_PAUSED;
        else
            NSLog(@"Unknown song status: %@", status);
        song.position = [[values objectForKey:@"position"] integerValue];
        
        [_delegate clientDidReceiveSongUpdate:song];

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
        [_delegate clientDidAuthenticate:YES];
        return;
    }
    
    NSDictionary *params = @{@"pin": @([pin integerValue])};
    [self POST:@"client/authenticate" parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        NSInteger clientID = [[responseObject objectForKey:@"id"] integerValue];
        _clientID = clientID;
        
        _authenticated = YES;
        [_delegate clientDidAuthenticate:YES];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        NSLog(@"task: %@", task);
        NSLog(@"err %@", error);
        [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)deauthenticateClient {
    if (OFFLINE) {
        _clientID = -1;
        return;
    }
    
    NSDictionary *params = @{ @"ClientID": [NSString stringWithFormat:@"%d", _clientID]};
    [self POST:@"client/deauthenticate" parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        _clientID = -1;
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
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
        [_delegate clientDidReceiveSongList:list];
        return;
    }
    
    NSString *query = [self createURLQuery:@"client/request_song_list"];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        [self handleUpdateResponse:responseObject];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)sendVote:(NSInteger)songID {
    if (OFFLINE) {
        return;
    }
    
    NSDictionary *params = @{ @"clientID": [NSString stringWithFormat:@"%d", _clientID] };
    [self POST:@"client/vote" parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)sendLike:(NSInteger)songID {
    if (OFFLINE) {
        return;
    }
    
    
}

- (void)sendDislike:(NSInteger)songID {
    if (OFFLINE) {
        return;
    }
    
    
}

- (void)requestUpdate {
    if (OFFLINE) {
        
        return;
    }
    
    NSString *query = [self createURLQuery:@"request_update"];
    NSDictionary *params = @{ @"ClientID": [NSString stringWithFormat:@"%d", _clientID] };
    [self GET:query parameters:params success:^(NSURLSessionDataTask *task, id responseObject) {
        NSLog(@"response obj %@", responseObject);
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)requestLikeUpdate {
    if (OFFLINE) {
        
        return;
    }
    
    NSString *query = [self createURLQuery:@"request_like_update"];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)requestVoteUpdate {
    if (OFFLINE) {
        
        return;
    }
    
    NSString *query = [self createURLQuery:@"request_vote_update"];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)requestSongUpdate {
    if (OFFLINE) {
        static int count = 0;
        SongInfo *song = [[SongInfo alloc] init];

        if (count == 0) {
            song.songID = 2;
            song.songName = @"Dark Horse";
            song.songArtist = @"Katy Perry";
            song.songAlbum = @"Prism";
            song.songLength = 160;
            song.status = SS_PLAYING;
            song.position = 12;
            count++;
        } else if (count == 1) {
            song.songID = 1;
            song.songName = @"Your Guardian Angel";
            song.songArtist = @"Red Jumpsuit Apparatus";
            song.songAlbum = @"Don't You Fake It";
            song.songLength = 180;
            song.status = SS_PLAYING;
            song.position = 0;
            count = 0;
        }
        
        [_delegate clientDidReceiveSongUpdate:song];
        return;
    }
    
    NSString *query = [self createURLQuery:@"request_song_update"];
    NSLog(@"query %@", query);
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        [self handleUpdateResponse:responseObject];
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidFailTask:task error:error];
    }];
}

- (void)requestAlbumArtForSong:(SongInfo *)song {
    if (OFFLINE) {
        return;
    }
    
    NSString *songIDParam = [NSString stringWithFormat:@"&songID=%d", song.songID];
    NSString *query = [[self createURLQuery:@"client/request_photo"] stringByAppendingString:songIDParam];
    
    // save default serializer
    AFHTTPResponseSerializer <AFURLResponseSerialization> *defaultSerializer = self.responseSerializer;
    // set image serializer
    self.responseSerializer = [AFImageResponseSerializer serializer];
    [self GET:query parameters:nil success:^(NSURLSessionDataTask *task, id responseObject) {
        UIImage *image = (UIImage *)responseObject;
        [_delegate clientDidReceiveAlbumArt:image forSong:song];
        
        // restore default serializer
        self.responseSerializer = defaultSerializer;
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        [_delegate clientDidFailTask:task error:error];
        // restore default serializer
        self.responseSerializer = defaultSerializer;
    }];
}

@end
