//
//  MopubManager.h
//  MoPub
//
//  Copyright (c) 2017 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MPAdView.h"
#import "MPInterstitialAdController.h"
#import "MPRewardedVideoReward.h"
#import "MPRewardedVideo.h"


typedef enum
{
	MoPubBannerType_320x50,
	MoPubBannerType_300x250,
	MoPubBannerType_728x90,
	MoPubBannerType_160x600
} MoPubBannerType;

typedef enum
{
	MoPubAdPositionTopLeft,
	MoPubAdPositionTopCenter,
	MoPubAdPositionTopRight,
	MoPubAdPositionCentered,
	MoPubAdPositionBottomLeft,
	MoPubAdPositionBottomCenter,
	MoPubAdPositionBottomRight
} MoPubAdPosition;


@interface MoPubManager : NSObject <MPAdViewDelegate, MPInterstitialAdControllerDelegate, CLLocationManagerDelegate, MPRewardedVideoDelegate>
{
@private
    BOOL _locationEnabled;
    NSString *_adUnitId;
}
@property (nonatomic, retain) MPAdView *adView;
@property (nonatomic, retain) CLLocationManager *locationManager;
@property (nonatomic, retain) CLLocation *lastKnownLocation;
@property (nonatomic) MoPubAdPosition bannerPosition;


+ (MoPubManager*)sharedManager;

+ (MoPubManager*)managerForAdunit:(NSString*)adUnitId;

+ (UIViewController*)unityViewController;

- (id)initWithAdUnit:(NSString*)adUnitId;

- (void)enableLocationSupport:(BOOL)shouldEnable;

- (void)reportApplicationOpen:(NSString*)iTunesId;

- (void)createBanner:(MoPubBannerType)bannerType atPosition:(MoPubAdPosition)position;

- (void)destroyBanner;

- (void)showBanner;

- (void)hideBanner:(BOOL)shouldDestroy;

- (void)refreshAd:(NSString*)keywords;

- (void)requestInterstitialAd:(NSString*)keywords;

- (void)showInterstitialAd;



@end
