//
//  SongTileView.m
//  MusicCloud
//
//  Created by Josh on 4/20/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "SongTileView.h"
#import "SongInfo.h"

#import <QuartzCore/QuartzCore.h>

#define SHADOW_X 4
#define SHADOW_Y 4

@implementation SongTileView

- (id)initWithSong:(SongInfo *)song {
    self = [[[NSBundle mainBundle] loadNibNamed:@"SongTileView" owner:self options:nil] firstObject];
    if (self) {
        self.backgroundColor = [UIColor whiteColor];
        
        //self.layer.borderWidth = 1.0;
        //self.layer.borderColor = [[UIColor blackColor] CGColor];
        self.layer.masksToBounds = NO;
        self.layer.shadowOffset = CGSizeMake(SHADOW_X, SHADOW_Y);
        self.layer.shadowRadius = 5;
        self.layer.shadowOpacity = 0.5;
        self.layer.shadowPath = [[UIBezierPath bezierPathWithRect:self.bounds] CGPath]; // improves performance
        
        self.titleLabel.text = song.songName;
        self.albumLabel.text = song.songAlbum;
        self.artistLabel.text = song.songArtist;
    }
    return self;
}

@end
