//
//  SongInfo.h
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SongInfo : NSObject

@property (nonatomic) NSInteger songID;
@property (strong, nonatomic) NSString *songName;
@property (strong, nonatomic) NSString *songArtist;
@property (strong, nonatomic) NSString *songAlbum;

@property (nonatomic) NSInteger votes;

@end
