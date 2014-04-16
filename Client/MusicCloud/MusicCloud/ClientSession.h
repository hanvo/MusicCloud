//
//  NetworkManager.h
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AFNetworking.h"

@protocol ClientSessionDelegate <NSObject>
// server to client messages

@optional;
- (void)clientDidAuthenticate:(BOOL)auth;

- (void)clientDidReceiveSongUpdate:(NSDictionary *)songInfo;
- (void)clientDidReceiveVoteUpdate:(NSDictionary *)voteInfo;
- (void)clientDidReceiveLikeUpdate:(NSDictionary *)likeInfo;

// array of NSDictionary objects
- (void)clientDidReceiveSongList:(NSArray *)list;

- (void)clientDidReceiveFailure:(NSString *)message;
@end

@interface ClientSession : AFHTTPSessionManager

// set if a call to authenticateClient: succeeds
@property (nonatomic) NSInteger clientID;

@property (weak, nonatomic) id <ClientSessionDelegate> delegate;

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

@end
