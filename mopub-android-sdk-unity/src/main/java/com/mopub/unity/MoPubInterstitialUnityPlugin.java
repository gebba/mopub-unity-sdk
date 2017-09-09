package com.mopub.unity;

import android.util.Log;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.unity3d.player.UnityPlayer;


/**
 * Provides an API that bridges the Unity Plugin with the MoPub Interstitial SDK.
 */
public class MoPubInterstitialUnityPlugin extends MoPubUnityPlugin
        implements MoPubInterstitial.InterstitialAdListener {

    private MoPubInterstitial mMoPubInterstitial;

    /**
     * Creates a {@link MoPubInterstitialUnityPlugin} for the given ad unit ID.
     *
     * @param adUnitId String for the ad unit ID to use for this interstitial.
     */
    public MoPubInterstitialUnityPlugin(final String adUnitId) {
        super(adUnitId);
    }


    /* ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****
     * Interstitials API                                                                       *
     * ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****/

    /**
     * Loads an interstitial ad for the current ad unit ID and with the given keywords.
     *
     * @param keywords String with comma-separated key:value pairs of keywords.
     */
    public void requestInterstitialAd(final String keywords) {
        runSafelyOnUiThread(new Runnable() {
            public void run() {
                mMoPubInterstitial = new MoPubInterstitial(getActivity(), mAdUnitId);
                mMoPubInterstitial.setInterstitialAdListener(MoPubInterstitialUnityPlugin.this);

                if (keywords != null && keywords.length() > 0)
                    mMoPubInterstitial.setKeywords(keywords);

                mMoPubInterstitial.load();
            }
        });
    }

    /**
     * Shows the loaded interstitial ad.
     */
    public void showInterstitialAd() {
        runSafelyOnUiThread(new Runnable() {
            public void run() {
                mMoPubInterstitial.show();
            }
        });
    }


    /* ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****
     * InterstitialAdListener implementation                                                   *
     * ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****/

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Log.i(TAG, "onInterstitialLoaded: " + interstitial);
        UnityPlayer.UnitySendMessage("MoPubManager", "onInterstitialLoaded", mAdUnitId);
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        String errorMsg =
                String.format("adUnitId = %s, errorCode = %s", mAdUnitId, errorCode.toString());
        Log.i(TAG, "onInterstitialFailed: " + errorMsg);
        UnityPlayer.UnitySendMessage("MoPubManager", "onInterstitialFailed", errorMsg);
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        Log.i(TAG, "onInterstitialShown: " + interstitial);
        UnityPlayer.UnitySendMessage("MoPubManager", "onInterstitialShown", mAdUnitId);
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        Log.i(TAG, "onInterstitialClicked: " + interstitial);
        UnityPlayer.UnitySendMessage("MoPubManager", "onInterstitialClicked", mAdUnitId);
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        Log.i(TAG, "onInterstitialDismissed: " + interstitial);
        UnityPlayer.UnitySendMessage("MoPubManager", "onInterstitialDismissed", mAdUnitId);
    }
}
