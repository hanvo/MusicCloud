//
//  SongScrollView.h
//  MusicCloud
//
//  Created by Josh on 4/20/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>

@class SongInfo;

@interface SongScrollView : UIScrollView

- (void)clearSongs;

- (void)addSong:(SongInfo *)song animated:(BOOL)anim;

@end
