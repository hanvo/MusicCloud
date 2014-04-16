//
//  LoginViewController.h
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ClientSession.h"

@class BorderButton;

@interface LoginViewController : UIViewController <UITextFieldDelegate, ClientSessionDelegate>

@property (weak, nonatomic) IBOutlet UITextField *pinField;
@property (weak, nonatomic) IBOutlet BorderButton *enterButton;

@end
