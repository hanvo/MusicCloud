//
//  SongInfo.m
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "SongInfo.h"

@implementation SongInfo

- (NSString *)description {
    return [NSString stringWithFormat:@"SongInfo [id: %ld, name: %@, artist: %@, album: %@, votes: %ld]", (long)_songID, _songName, _songArtist, _songAlbum, (long)_votes];
}

@end
