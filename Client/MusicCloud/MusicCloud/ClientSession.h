//
//  NetworkManager.h
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AFNetworking.h"

// if set, does not make HTTP requests.
// all requests return successfully
#define OFFLINE 0

#define BASE_URL @"http://klamath.dnsdynamic.com:5050"

@class SongInfo;
@class CurrentSongInfo;

@protocol ClientSessionDelegate <NSObject>
// server to client messages

@optional;
- (void)clientDidAuthenticate:(BOOL)auth;

- (void)clientDidReceiveSongUpdate:(CurrentSongInfo *)song;
- (void)clientDidReceiveVoteUpdate:(NSArray *)song;
- (void)clientDidReceiveLikeUpdate:(CurrentSongInfo *)song;

- (void)clientDidReceiveAlbumArt:(UIImage *)image forSong:(SongInfo *)song;

// array of SongInfo objects
- (void)clientDidReceiveSongList:(NSArray *)list;

// received failure response from server
- (void)clientDidReceiveFailure:(NSString *)message;

// failed making HTTP request
- (void)clientDidFailTask:(NSURLSessionDataTask *)task error:(NSError *)err;
@end

@interface ClientSession : AFHTTPSessionManager

// set if a call to authenticateClient: succeeds
@property (nonatomic) NSInteger clientID;

@property (weak, nonatomic) id <ClientSessionDelegate> delegate;

@property (nonatomic, readonly, getter = isAuthenticated) BOOL authenticated;

+ (instancetype)sharedSession;

// client to server messages

- (void)authenticateClient:(NSString *)pin;
- (void)deauthenticateClient;

- (void)requestSongList;

- (void)sendVote:(NSInteger)songID;
- (void)sendLike:(NSInteger)songID;
- (void)sendDislike:(NSInteger)songID;

// updates are requested continually with a large timout, and
// when either a response or timeout is received, another request is sent
- (void)startUpdateRequests;
- (void)stopUpdateRequests;

- (void)requestLikeUpdate;
- (void)requestVoteUpdate;
- (void)requestSongUpdate;

- (void)requestAlbumArtForSong:(SongInfo *)song;

@end
