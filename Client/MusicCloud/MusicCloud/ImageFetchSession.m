//
//  ImageFetchSession.m
//  MusicCloud
//
//  Created by Josh on 5/3/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "ImageFetchSession.h"
#import "ClientSession.h"

@implementation ImageFetchSession

- (id)initWithBaseURL:(NSURL *)url {
    if (self = [super initWithBaseURL:url]) {
        self.requestSerializer = [AFJSONRequestSerializer serializer];
        self.responseSerializer = [AFImageResponseSerializer serializer];
    }
    return self;
}

+ (instancetype)sharedSession {
    static ImageFetchSession *sharedManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSURL *url = [NSURL URLWithString:BASE_URL];
        sharedManager = [[ImageFetchSession alloc] initWithBaseURL:url];
    });
    return sharedManager;
}

@end
