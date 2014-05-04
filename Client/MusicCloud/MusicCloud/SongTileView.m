//
//  SongTileView.m
//  MusicCloud
//
//  Created by Josh on 4/20/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "SongTileView.h"
#import "SongInfo.h"
#import "MarqueeLabel.h"

#import <QuartzCore/QuartzCore.h>

#define SHADOW_X 4
#define SHADOW_Y 4

@implementation SongTileView

- (id)initWithSong:(SongInfo *)song {
    self = [[[NSBundle mainBundle] loadNibNamed:@"SongTileView" owner:self options:nil] firstObject];
    if (self) {
        _song = song;
        
        self.backgroundColor = [UIColor whiteColor];
        
        //self.layer.borderWidth = 1.0;
        //self.layer.borderColor = [[UIColor blackColor] CGColor];
        self.layer.masksToBounds = NO;
        self.layer.shadowOffset = CGSizeMake(SHADOW_X, SHADOW_Y);
        self.layer.shadowRadius = 5;
        self.layer.shadowOpacity = 0.5;
        self.layer.shadowPath = [[UIBezierPath bezierPathWithRect:self.bounds] CGPath]; // improves performance
        
        _titleLabel.marqueeType = MLContinuous;
        _titleLabel.animationCurve = UIViewAnimationOptionCurveLinear;
        _titleLabel.continuousMarqueeExtraBuffer = 50.0f;
        _titleLabel.numberOfLines = 1;
        _titleLabel.opaque = NO;
        _titleLabel.enabled = YES;
        _titleLabel.lengthOfScroll = 5;
        _titleLabel.backgroundColor = [UIColor clearColor];
        
        _albumLabel.marqueeType = MLContinuous;
        _albumLabel.animationCurve = UIViewAnimationOptionCurveLinear;
        _albumLabel.continuousMarqueeExtraBuffer = 50.0f;
        _albumLabel.numberOfLines = 1;
        _albumLabel.opaque = NO;
        _albumLabel.enabled = YES;
        _albumLabel.lengthOfScroll = 5;
        _albumLabel.backgroundColor = [UIColor clearColor];
        
        _artistLabel.marqueeType = MLContinuous;
        _artistLabel.animationCurve = UIViewAnimationOptionCurveLinear;
        _artistLabel.continuousMarqueeExtraBuffer = 50.0f;
        _artistLabel.numberOfLines = 1;
        _artistLabel.opaque = NO;
        _artistLabel.enabled = YES;
        _artistLabel.lengthOfScroll = 5;
        _artistLabel.backgroundColor = [UIColor clearColor];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    self.titleLabel.text = _song.songName;
    self.albumLabel.text = _song.songAlbum;
    self.artistLabel.text = _song.songArtist;
    
    self.albumImageView.image = _song.albumArt;
}

@end
