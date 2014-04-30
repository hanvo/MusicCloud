//
//  NetworkManager.h
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AFNetworking.h"

@class SongInfo;

@protocol ClientSessionDelegate <NSObject>
// server to client messages

@optional;
- (void)clientDidAuthenticate:(BOOL)auth;

- (void)clientDidReceiveSongUpdate:(SongInfo *)song;
- (void)clientDidReceiveVoteUpdate:(SongInfo *)song;
- (void)clientDidReceiveLikeUpdate:(SongInfo *)song;

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

- (void)requestUpdate;
- (void)requestLikeUpdate;
- (void)requestVoteUpdate;
- (void)requestSongUpdate;

- (void)requestAlbumArtForSong:(SongInfo *)song;

@end
