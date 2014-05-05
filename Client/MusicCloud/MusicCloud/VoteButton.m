//
//  VoteButton.m
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "VoteButton.h"

@interface VoteButton ()

@property (strong, nonatomic) CAShapeLayer *circleLayer;

@end

@implementation VoteButton

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self _VoteButtonInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self _VoteButtonInit];
    }
    return self;
}

- (void)_VoteButtonInit {
    self.circleLayer = [CAShapeLayer layer];
    [self.circleLayer setBounds:CGRectMake(0, 0, self.bounds.size.width, self.bounds.size.height)];
    [self.circleLayer setPosition:CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds))];
    [self.circleLayer setStrokeColor:[[UIColor whiteColor] CGColor]];
    [self.circleLayer setLineWidth:1.0];
    [self.circleLayer setFillColor:[[UIColor blackColor] CGColor]];
    
    UIBezierPath *circlePath = [UIBezierPath bezierPathWithOvalInRect:self.bounds];
    [self.circleLayer setPath:[circlePath CGPath]];
    
    [self.layer insertSublayer:_circleLayer below:self.imageView.layer];
    
    [self.titleLabel setFont:[UIFont boldSystemFontOfSize:22.0]];
    [self setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
}

- (void)setHighlighted:(BOOL)highlighted {
    [super setHighlighted:highlighted];
    
    if (highlighted)
        [self.circleLayer setFillColor:[[UIColor whiteColor] CGColor]];
}

- (void)setBackgroundColor:(UIColor *)backgroundColor {
    [_circleLayer setFillColor:[backgroundColor CGColor]];
}

@end
