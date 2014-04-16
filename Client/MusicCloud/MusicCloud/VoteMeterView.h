//
//  VoteMeterView.h
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface VoteMeterView : UIView

// must be double in range [0.0, 1.0]
// unless the value is set to -1.0, in which case
// the meter will be blank
@property (nonatomic) double balance;

- (void)setBalance:(double)balance animated:(BOOL)anim;

@end
