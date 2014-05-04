//
//  VoteMeterView.h
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface VoteMeterView : UIView

// must be double in range [-1.0, 1.0]
// unless balanceEnabled is NO, then
// the meter will be blank
@property (nonatomic) double balance;

@property (nonatomic) BOOL balanceEnabled;

- (void)setBalance:(double)balance animated:(BOOL)anim;

@end
