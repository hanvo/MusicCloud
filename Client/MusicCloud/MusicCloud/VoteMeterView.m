//
//  VoteMeterView.m
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "VoteMeterView.h"

@interface VoteMeterView ()

@property (strong, nonatomic) UIView *redBar;

@end

@implementation VoteMeterView

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self _VoteMeterViewInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self _VoteMeterViewInit];
    }
    return self;
}

- (void)_VoteMeterViewInit {
    self.layer.cornerRadius = self.bounds.size.height/2;
    
    self.layer.borderColor = [[UIColor whiteColor] CGColor];
    self.layer.borderWidth = 1.0;
    self.layer.masksToBounds = YES;
    
    self.backgroundColor = [UIColor greenColor];
    
    CGRect frame = CGRectMake(0, 0, 0, self.bounds.size.height);
    _redBar = [[UIView alloc] initWithFrame:frame];
    _redBar.backgroundColor = [UIColor redColor];
    [self addSubview:_redBar];
}

- (void)layoutBar {
    CGRect frame = _redBar.frame;
    if (_balance == -1.0) {
        frame.size.width = 0;
        self.backgroundColor = [UIColor blackColor];
    } else {
        frame.size.width = _balance * self.bounds.size.width;
        self.backgroundColor = [UIColor greenColor];
    }
    [_redBar setFrame:frame];
}

- (void)setBalance:(double)balance {
    [self setBalance:balance animated:NO];
}

- (void)setBalance:(double)balance animated:(BOOL)anim {
    _balance = balance;
    
    if (anim) {
        [UIView animateWithDuration:0.3 delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
            [self layoutBar];
        } completion:^(BOOL finished) {
            
        }];
    } else {
        [self layoutBar];
    }
}

@end
