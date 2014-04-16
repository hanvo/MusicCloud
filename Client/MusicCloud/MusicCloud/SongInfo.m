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
    return [NSString stringWithFormat:@"SongInfo [id: %d, name: %@, artist: %@, album: %@, votes: %d]", _songID, _songName, _songArtist, _songAlbum, _votes];
}

@end
