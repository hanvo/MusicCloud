//
//  CurrentSongInfo.h
//  MusicCloud
//
//  Created by Josh on 5/2/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <Foundation/Foundation.h>

@class SongInfo;

@interface CurrentSongInfo : NSObject

@property (strong, nonatomic) SongInfo *songInfo;
@property (nonatomic) NSInteger likes;
@property (nonatomic) NSInteger dislikes;

@property (nonatomic) NSInteger position;


@property (nonatomic) double balance;

@end
