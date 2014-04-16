//
//  BorderButton.m
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "BorderButton.h"
#import <QuartzCore/QuartzCore.h>

@implementation BorderButton

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self _BorderButtonInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self _BorderButtonInit];
    }
    return self;
}

- (void)_BorderButtonInit {
    self.layer.borderWidth = 1.0;
    self.layer.borderColor = [[UIColor whiteColor] CGColor];
    self.layer.cornerRadius = 12.0;
    
    self.backgroundColor = [UIColor colorWithRed:11/255.0 green:211/255.0 blue:24/255.0 alpha:1.0];
    
    [self.titleLabel setFont:[UIFont boldSystemFontOfSize:18.0]];
    [self setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [self setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
}

@end
