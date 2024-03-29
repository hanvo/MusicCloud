//
//  CurrentSongInfo.m
//  MusicCloud
//
//  Created by Josh on 5/2/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "CurrentSongInfo.h"
#import "SongInfo.h"

@implementation CurrentSongInfo

- (id)init {
    if (self = [super init]) {
        _songInfo = [[SongInfo alloc] init];
    }
    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"CurrentSongInfo [SongInfo: %@, likes: %ld, dislikes: %ld]", _songInfo, (long)_likes, (long)_dislikes];
}

@end
