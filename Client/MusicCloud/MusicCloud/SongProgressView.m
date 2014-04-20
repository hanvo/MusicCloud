//
//  SongProgressView.m
//  MusicCloud
//
//  Created by Josh on 4/16/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "SongProgressView.h"

@interface SongProgressView ()

@property (strong, nonatomic) UIView *barView;
@property (strong, nonatomic) UIView *indicatorView;

@end

@implementation SongProgressView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self _SongProgressViewInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self _SongProgressViewInit];
    }
    return self;
}

- (void)_SongProgressViewInit {
    self.backgroundColor = [UIColor clearColor];
    
    static CGFloat BarHeight = 4.0;
    CGRect barFrame = CGRectMake(0, (self.bounds.size.height-BarHeight)/2, self.bounds.size.width, BarHeight);
    _barView = [[UIView alloc] initWithFrame:barFrame];
    [_barView setBackgroundColor:[UIColor whiteColor]];
    [self addSubview:_barView];
    
    static CGFloat IndicatorSize = 12.0;
    CGRect indicatorFrame = CGRectMake(0, (self.bounds.size.height-IndicatorSize)/2, IndicatorSize, IndicatorSize);
    _indicatorView = [[UIView alloc] initWithFrame:indicatorFrame];
    [_indicatorView setBackgroundColor:[UIColor colorWithWhite:0.8 alpha:1.0]];
    _indicatorView.layer.cornerRadius = IndicatorSize/2;
    _indicatorView.layer.masksToBounds = YES;
    [self addSubview:_indicatorView];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [self layoutBar];
}

- (void)layoutBar {
    CGRect frame = _indicatorView.frame;
    CGFloat x;
    if (_totalTime == 0)
        x = 0;
    else
        x = ((double)_currentTime/(double)_totalTime) * _barView.frame.size.width;
    frame.origin.x = x;
    _indicatorView.frame = frame;
}

- (void)setCurrentTime:(NSInteger)currentTime {
    _currentTime = currentTime;
    
    [self setNeedsLayout];
}

- (void)setTotalTime:(NSInteger)totalTime {
    _totalTime = totalTime;
    
    [self setNeedsLayout];
}

@end
