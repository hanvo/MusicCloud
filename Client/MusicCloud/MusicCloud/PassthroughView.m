//
//  PassthroughView.m
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "PassthroughView.h"

@implementation PassthroughView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self _PassthroughViewInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self _PassthroughViewInit];
    }
    return self;
}

- (void)_PassthroughViewInit {
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    if ([self pointInside:point withEvent:event])
        return _scrollView;
    return nil;
}

@end
