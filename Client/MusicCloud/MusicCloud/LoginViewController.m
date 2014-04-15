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

#define MAX_PIN_LENGTH 5

@interface LoginViewController ()

@end

@implementation LoginViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewTapped:)];
    [self.view addGestureRecognizer:tap];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"PartySegue"]) {
        PartyViewController *partyVC = segue.destinationViewController;
        
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

#pragma mark - IBAction

- (IBAction)pinFieldEditingDidEnd:(id)sender {
    [_enterButton sendActionsForControlEvents:UIControlEventTouchUpInside];
}

- (IBAction)enterPressed:(id)sender {
    [self performSegueWithIdentifier:@"PartySegue" sender:self];
}

@end
