//
//  SongTileView.h
//  MusicCloud
//
//  Created by Josh on 4/20/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>

@class SongInfo;

@interface SongTileView : UIView

@property (weak, nonatomic) IBOutlet UIImageView *albumImageView;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UILabel *albumLabel;
@property (weak, nonatomic) IBOutlet UILabel *artistLabel;

- (id)initWithSong:(SongInfo *)song;

@end
