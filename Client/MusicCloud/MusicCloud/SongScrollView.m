//
//  SongScrollView.m
//  MusicCloud
//
//  Created by Josh on 4/20/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "SongScrollView.h"
#import "SongInfo.h"
#import "SongTIleView.h"

#import <QuartzCore/QuartzCore.h>

#define TILE_PAD 8
#define TILE_WIDTH 280

@interface SongScrollView ()

// array of SongTileView objects
@property (strong, nonatomic) NSMutableArray *tiles;

@end

@implementation SongScrollView

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        self.contentSize = self.bounds.size;
        
        //self.layer.borderColor = [[UIColor blackColor] CGColor];
        //self.layer.borderWidth = 1.0;
        
        _tiles = [NSMutableArray array];
    }
    return self;
}

- (void)clearSongs {
    for (SongTileView *tile in _tiles)
        [tile removeFromSuperview];
    
    [_tiles removeAllObjects];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    CGFloat x = self.contentSize.width-TILE_PAD;
    for (NSInteger i = _tiles.count-1; i >= 0; i--) {
        SongTileView *tile = _tiles[i];
        
        x -= tile.bounds.size.width;
        
        CGRect frame = tile.frame;
        frame.origin.x = x;
        [tile setFrame:frame];
        
        x -= TILE_PAD;
    }
}

- (void)addSong:(SongInfo *)song animated:(BOOL)anim {
    CGRect tileFrame = CGRectMake(self.contentSize.width, 0, TILE_WIDTH, self.contentSize.height-TILE_PAD);
    SongTileView *tile = [[SongTileView alloc] initWithSong:song];
    [tile setFrame:tileFrame];
    [self addSubview:tile];
    [_tiles addObject:tile];
    
    CGSize contentSize = self.contentSize;
    contentSize.width = self.bounds.size.width * _tiles.count;
    [self setContentSize:contentSize];
    
    if (anim) {
        [UIView animateWithDuration:0.2 delay:0 options:UIViewAnimationOptionCurveEaseIn animations:^{
            [self layoutSubviews];
        } completion:^(BOOL finished) {
            
        }];
    } else {
        [self layoutSubviews];
    }
    
    CGPoint contentOffset = CGPointMake(contentSize.width - TILE_WIDTH - TILE_PAD, 0);
    [self setContentOffset:contentOffset animated:anim];
}

- (void)updateCurrentSong {
    SongTileView *song = [_tiles lastObject];
    [song layoutSubviews];
}

@end
