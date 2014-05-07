//
//  LoginViewController.m
//  MusicCloud
//
//  Created by Josh on 4/15/14.
//  Copyright (c) 2014 CS252. All rights reserved.
//

#import "LoginViewController.h"
#import "BorderButton.h"
#import "SWRevealViewController.h"
#import "PartyViewController.h"

#define MAX_PIN_LENGTH 4

@interface LoginViewController ()

@property (strong, nonatomic) UIView *overlay;

@end

@implementation LoginViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewTapped:)];
    [self.view addGestureRecognizer:tap];
    
    _overlay = [[UIView alloc] initWithFrame:self.view.bounds];
    [_overlay setBackgroundColor:[UIColor colorWithWhite:0.2 alpha:0.5]];
    [_overlay setAlpha:0];
    
    UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    [spinner setCenter:CGPointMake(_enterButton.center.x, _enterButton.center.y)];
    [spinner startAnimating];
    [_overlay addSubview:spinner];
    
    [self.view addSubview:_overlay];
    
    self.navigationController.interactivePopGestureRecognizer.enabled = NO;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    _pinField.text = nil;
    
    [_overlay setAlpha:0.0];
    
    [[ClientSession sharedSession] setDelegate:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"PartySegue"]) {
        //PartyViewController *partyVC = segue.destinationViewController;
        
        UIBarButtonItem *backButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Leave" style:UIBarButtonItemStylePlain target:nil action:nil];
        [self.navigationItem setBackBarButtonItem:backButtonItem];
    }
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

- (void)viewTapped:(id)sender {
    [self.view endEditing:YES];
}

#pragma mark - UITextFieldDelegate

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    NSInteger newLength = [textField.text stringByReplacingCharactersInRange:range withString:string].length;
    if (newLength > MAX_PIN_LENGTH)
        return NO;
    
    NSCharacterSet *numbers = [NSCharacterSet characterSetWithCharactersInString:@"0123456789"];
    for (int i = 0; i < string.length; i++) {
        unichar c = [string characterAtIndex:i];
        if (![numbers characterIsMember:c])
            return NO;
    }
    
    return YES;
}

#pragma mark - ClientSessionDelegate

- (void)clientDidAuthenticate:(BOOL)auth {
    if (auth) {
        [self performSegueWithIdentifier:@"PartySegue" sender:self];
    } else {
        [UIView animateWithDuration:0.3 animations:^{
            [_overlay setAlpha:0.0];
        } completion:^(BOOL finished) {
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"MusicCloud Error" message:@"Invalid PIN" delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
            [alert show];
        }];
    }
}

// could not connect
- (void)clientDidFailTask:(NSURLSessionDataTask *)task error:(NSError *)err {
    [UIView animateWithDuration:0.3 animations:^{
        [_overlay setAlpha:0.0];
    } completion:^(BOOL finished) {
        NSLog(@"%@", err);
        NSLog(@"task %@", task.response);
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"MusicCloud Error" message:@"Could not connect to DJ" delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
        [alert show];
    }];
}

#pragma mark - IBAction

- (IBAction)enterPressed:(id)sender {
    NSString *pin = _pinField.text;
    if (pin.length > 0) {
        [UIView animateWithDuration:0.3 animations:^{
            [self.view endEditing:YES];
            [_overlay setAlpha:1.0];
        }];
        
        [[ClientSession sharedSession] authenticateClient:pin];
    } else {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"MusicCloud" message:@"You gotta enter a PIN" delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK", nil];
        [alert show];
    }
}

@end
