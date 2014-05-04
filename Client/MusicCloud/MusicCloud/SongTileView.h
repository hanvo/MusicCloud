//
//  SongTileView.h
//  MusicCloud
//
//  Created by Josh on 4/20/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MarqueeLabel;
@class SongInfo;

@interface SongTileView : UIView

@property (strong, nonatomic) SongInfo *song;

@property (weak, nonatomic) IBOutlet UIImageView *albumImageView;
@property (weak, nonatomic) IBOutlet MarqueeLabel *titleLabel;
@property (weak, nonatomic) IBOutlet MarqueeLabel *albumLabel;
@property (weak, nonatomic) IBOutlet MarqueeLabel *artistLabel;

- (id)initWithSong:(SongInfo *)song;

@end
