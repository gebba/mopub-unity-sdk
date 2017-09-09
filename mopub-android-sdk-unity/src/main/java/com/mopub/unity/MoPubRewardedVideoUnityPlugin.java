package com.mopub.unity;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.mopub.common.MediationSettings;
import com.mopub.common.MoPubReward;
import com.mopub.common.Preconditions;
import com.mopub.mobileads.CustomEventRewardedVideo;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;
import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * Provides an API that bridges the Unity Plugin with the MoPub Rewarded Ad SDK.
 */
public class MoPubRewardedVideoUnityPlugin extends MoPubUnityPlugin
        implements MoPubRewardedVideoListener {

    private static boolean sRewardedVideoInitialized;

    private static final String CHARTBOOST_MEDIATION_SETTINGS =
            "com.mopub.mobileads.ChartboostRewardedVideo$ChartboostMediationSettings";
    private static final String VUNGLE_MEDIATION_SETTINGS =
            "com.mopub.mobileads.VungleRewardedVideo$VungleMediationSettings$Builder";
    private static final String ADCOLONY_MEDIATION_SETTINGS =
            "com.mopub.mobileads.AdColonyRewardedVideo$AdColonyInstanceMediationSettings";

    /**
     * Creates a {@link MoPubRewardedVideoUnityPlugin} for the given ad unit ID.
     *
     * @param adUnitId String for the ad unit ID to use for this rewarded video.
     */
    public MoPubRewardedVideoUnityPlugin(final String adUnitId) {
        super(adUnitId);
    }


    /* ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****
     * Rewarded Ads API                                                                        *
     * ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****/

    /**
     * Initializes rewarded ad system, if it hasn't been initialized already.
     */
    public static void initializeRewardedVideo() {
        runSafelyOnUiThread(new Runnable() {
            public void run() {
                if (!sRewardedVideoInitialized) {
                    MoPubRewardedVideos.initializeRewardedVideo(getActivity());
                    sRewardedVideoInitialized = true;
                }
            }
        });
    }

    /**
     * Initializes rewarded ad system, if it hasn't been initialized already, with the given
     * networks.
     *
     * @param networksToInitString String of comma-separated network rewarded video adapter classes.
     */
    public static void initializeRewardedVideoWithNetworks(
            @Nullable final String networksToInitString) {

        // Extract classes from network strings
        final List<Class<? extends CustomEventRewardedVideo>> networksToInit = new LinkedList<>();
        if (!TextUtils.isEmpty(networksToInitString)) {
            String[] networksArray = networksToInitString.split(",");
            for (String networkString : networksArray) {
                try {
                    Class<? extends CustomEventRewardedVideo> networkClass =
                            Class.forName(networkString).asSubclass(CustomEventRewardedVideo.class);
                    networksToInit.add(networkClass);
                } catch (ClassNotFoundException e) {
                    Log.w(TAG, "Class not found for attempted network adapter class name: "
                            + networksToInitString);
                }
            }
        }

        runSafelyOnUiThread(new Runnable() {
            public void run() {
                if (!sRewardedVideoInitialized) {
                    MoPubRewardedVideos.initializeRewardedVideo(getActivity(), networksToInit);
                    sRewardedVideoInitialized = true;
                }
            }
        });
    }

    /**
     * Loads a rewarded ad for the current ad unit ID and the given mediation settings,
     * keywords, latitude, longitude and customer ID.
     *
     * Options for mediation settings for each network are as follows on Android:
     *  {
     *      "adVendor": "AdColony",
     *      "withConfirmationDialog": false,
     *      "withResultsDialog": true
     *  }
     *  {
     *      "adVendor": "Chartboost",
     *      "customId": "the-user-id"
     *  }
     *  {
     *      "adVendor": "Vungle",
     *      "userId": "the-user-id",
     *      "cancelDialogBody": "Cancel Body",
     *      "cancelDialogCloseButton": "Shut it Down",
     *      "cancelDialogKeepWatchingButton": "Watch On",
     *      "cancelDialogTitle": "Cancel Title"
     *  }
     * See https://www.mopub.com/resources/docs/unity-engine-integration/#RewardedVideo for more
     * details and sample helper methods to generate mediation settings.
     *
     * @param json String with JSON containing third-party network specific settings.
     * @param keywords String with comma-separated key:value pairs of keywords.
     * @param latitude double with the desired latitude.
     * @param longitude double with the desired longitude.
     * @param customerId String with the customer ID.
     */
    public void requestRewardedVideo(final String json, final String keywords,
            final double latitude, final double longitude, final String customerId) {
        runSafelyOnUiThread(new Runnable() {
            public void run() {
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);

                MoPubRewardedVideoManager.RequestParameters requestParameters =
                        new MoPubRewardedVideoManager.RequestParameters(
                                keywords, location, customerId);

                MoPubRewardedVideos.setRewardedVideoListener(MoPubRewardedVideoUnityPlugin.this);

                if (json != null) {
                    MoPubRewardedVideos.loadRewardedVideo(
                            mAdUnitId, requestParameters, extractMediationSettingsFromJson(json));
                } else {
                    MoPubRewardedVideos.loadRewardedVideo(mAdUnitId, requestParameters);
                }
            }
        });
    }

    /**
     * Whether there is a rewarded ad ready to play or not.
     *
     * @return true if there is a rewarded ad loaded and ready to play; false otherwise.
     */
    public boolean hasRewardedVideo() {
        return MoPubRewardedVideos.hasRewardedVideo(mAdUnitId);
    }

    /**
     * Takes over the screen and shows rewarded ad, if one is loaded and ready to play.
     */
    public void showRewardedVideo() {
        runSafelyOnUiThread(new Runnable() {
            public void run() {
                if (!MoPubRewardedVideos.hasRewardedVideo(mAdUnitId)) {
                    Log.i(TAG, String.format(Locale.US,
                            "No rewarded ad is available at this time."));
                    return;
                }

                MoPubRewardedVideos.setRewardedVideoListener(MoPubRewardedVideoUnityPlugin.this);
                MoPubRewardedVideos.showRewardedVideo(mAdUnitId);
            }
        });
    }

    /**
     * Retrieves the list of available {@link MoPubReward}s for the current ad unit ID.
     *
     * @return an array with the available {@link MoPubReward}s.
     */
    public MoPubReward[] getAvailableRewards() {
        Set<MoPubReward> rewardsSet = MoPubRewardedVideos.getAvailableRewards(mAdUnitId);

        Log.i(TAG, String.format(Locale.US, "%d MoPub rewards available", rewardsSet.size()));

        return rewardsSet.toArray(new MoPubReward[rewardsSet.size()]);
    }

    /**
     * Specifies which reward should be given to the user on video completion.
     *
     * @param selectedReward a {@link MoPubReward} to reward the user with.
     */
    public void selectReward(@NonNull MoPubReward selectedReward) {
        Preconditions.checkNotNull(selectedReward);

        Log.i(TAG, String.format(Locale.US, "Selected reward \"%d %s\"",
                selectedReward.getAmount(),
                selectedReward.getLabel()));

        MoPubRewardedVideos.selectReward(mAdUnitId, selectedReward);
    }


    /* ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****
     * RewardedVideoListener implementation                                                    *
     * ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****/

    @Override
    public void onRewardedVideoLoadSuccess(String adUnitId) {
        if (mAdUnitId.equals(adUnitId)) {
            Log.i(TAG, "Rewarded ad loaded.");
            UnityPlayer.UnitySendMessage("MoPubManager", "onRewardedVideoLoaded", adUnitId);
        }
    }

    @Override
    public void onRewardedVideoLoadFailure(String adUnitId, MoPubErrorCode errorCode) {
        if (mAdUnitId.equals(adUnitId)) {
            String errorMsg = String.format(Locale.US,
                    "Rewarded ad failed to load for ad unit %s: %s",
                    adUnitId,
                    errorCode.toString());

            Log.e(TAG, errorMsg);
            UnityPlayer.UnitySendMessage("MoPubManager", "onRewardedVideoFailed", errorMsg);
        }
    }

    @Override
    public void onRewardedVideoStarted(String adUnitId) {
        if (mAdUnitId.equals(adUnitId)) {
            Log.i(TAG, "Rewarded ad started.");
            UnityPlayer.UnitySendMessage("MoPubManager", "onRewardedVideoShown", adUnitId);
        }
    }

    @Override
    public void onRewardedVideoClicked(@NonNull String adUnitId) {
        if (mAdUnitId.equals(adUnitId)) {
            Log.i(TAG, "Rewarded ad clicked.");
            UnityPlayer.UnitySendMessage("MoPubManager", "onRewardedVideoClicked", adUnitId);
        }
    }

    @Override
    public void onRewardedVideoPlaybackError(String adUnitId, MoPubErrorCode errorCode) {
        if (mAdUnitId.equals(adUnitId)) {
            String errorMsg = String.format(Locale.US,
                    "Rewarded ad playback error for ad unit %s: %s",
                    adUnitId,
                    errorCode.toString());

            Log.e(TAG, errorMsg);
            UnityPlayer.UnitySendMessage("MoPubManager", "onRewardedVideoFailedToPlay", errorMsg);
        }
    }

    @Override
    public void onRewardedVideoClosed(String adUnitId) {
        if (mAdUnitId.equals(adUnitId)) {
            Log.i(TAG, "Rewarded ad closed.");
            UnityPlayer.UnitySendMessage("MoPubManager", "onRewardedVideoClosed", adUnitId);
        }
    }

    @Override
    public void onRewardedVideoCompleted(Set<String> adUnitIds, MoPubReward reward) {
        if (adUnitIds.size() == 0 || reward == null) {
            Log.e(TAG, String.format(Locale.US,
                    "Rewarded ad completed without ad unit ID and/or reward."));
            return;
        }

        String adUnitId = adUnitIds.toArray()[0].toString();
        if (mAdUnitId.equals(adUnitId)) {
            try {
                Log.i(TAG, String.format(Locale.US,
                        "Rewarded ad completed with reward  \"%d %s\"",
                        reward.getAmount(),
                        reward.getLabel()));

                JSONObject json = new JSONObject();
                json.put("adUnitId", adUnitId);
                json.put("currencyType", "");
                json.put("amount", reward.getAmount());

                UnityPlayer.UnitySendMessage(
                        "MoPubManager", "onRewardedVideoReceivedReward", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /* ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****
     * Private helpers                                                                         *
     * ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****/

    private MediationSettings[] extractMediationSettingsFromJson(String json) {
        ArrayList<MediationSettings> settings = new ArrayList<MediationSettings>();

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String adVendor = jsonObj.getString("adVendor");
                Log.i(TAG, "adding MediationSettings for ad vendor: " + adVendor);
                if (adVendor.equalsIgnoreCase("chartboost")) {
                    if (jsonObj.has("customId")) {
                        try {
                            Class<?> mediationSettingsClass =
                                    Class.forName(CHARTBOOST_MEDIATION_SETTINGS);
                            Constructor<?> mediationSettingsConstructor =
                                    mediationSettingsClass.getConstructor(String.class);
                            MediationSettings s =
                                    (MediationSettings) mediationSettingsConstructor
                                            .newInstance(jsonObj.getString("customId"));
                            settings.add(s);
                        } catch (ClassNotFoundException e) {
                            Log.i(TAG, "could not find ChartboostMediationSettings class. " +
                                    "Did you add Chartboost Network SDK to your Android folder?");
                        } catch (Exception e) {
                            printExceptionStackTrace(e);
                        }
                    } else {
                        Log.i(TAG, "No customId key found in the settings object. " +
                                "Aborting adding Chartboost MediationSettings");
                    }
                } else if (adVendor.equalsIgnoreCase("vungle")) {
                    try {
                        Class<?> builderClass = Class.forName(VUNGLE_MEDIATION_SETTINGS);
                        Constructor<?> builderConstructor = builderClass.getConstructor();
                        Object b = builderConstructor.newInstance();

                        Method withUserId =
                                builderClass.getDeclaredMethod("withUserId",
                                        String.class);
                        Method withCancelDialogBody =
                                builderClass.getDeclaredMethod("withCancelDialogBody",
                                        String.class);
                        Method withCancelDialogCloseButton =
                                builderClass.getDeclaredMethod("withCancelDialogCloseButton",
                                        String.class);
                        Method withCancelDialogKeepWatchingButton =
                                builderClass.getDeclaredMethod("withCancelDialogKeepWatchingButton",
                                        String.class);
                        Method withCancelDialogTitle =
                                builderClass.getDeclaredMethod("withCancelDialogTitle",
                                        String.class);
                        Method build = builderClass.getDeclaredMethod("build");

                        if (jsonObj.has("userId")) {
                            withUserId.invoke(b, jsonObj.getString("userId"));
                        }

                        if (jsonObj.has("cancelDialogBody")) {
                            withCancelDialogBody.invoke(b, jsonObj.getString("cancelDialogBody"));
                        }

                        if (jsonObj.has("cancelDialogCloseButton")) {
                            withCancelDialogCloseButton
                                    .invoke(b, jsonObj.getString("cancelDialogCloseButton"));
                        }

                        if (jsonObj.has("cancelDialogKeepWatchingButton")) {
                            withCancelDialogKeepWatchingButton
                                    .invoke(b, jsonObj.getString("cancelDialogKeepWatchingButton"));
                        }

                        if (jsonObj.has("cancelDialogTitle")) {
                            withCancelDialogTitle.invoke(b, jsonObj.getString("cancelDialogTitle"));
                        }

                        settings.add((MediationSettings) build.invoke(b));

                    } catch (ClassNotFoundException e) {
                        Log.i(TAG, "could not find VungleMediationSettings class. " +
                                "Did you add Vungle Network SDK to your Android folder?");
                    } catch (Exception e) {
                        printExceptionStackTrace(e);
                    }
                } else if (adVendor.equalsIgnoreCase("adcolony")) {
                    if (jsonObj.has("withConfirmationDialog") && jsonObj.has("withResultsDialog")) {
                        boolean withConfirmationDialog =
                                jsonObj.getBoolean("withConfirmationDialog");
                        boolean withResultsDialog =
                                jsonObj.getBoolean("withResultsDialog");

                        try {
                            Class<?> mediationSettingsClass =
                                    Class.forName(ADCOLONY_MEDIATION_SETTINGS);
                            Constructor<?> mediationSettingsConstructor =
                                    mediationSettingsClass
                                            .getConstructor(boolean.class, boolean.class);
                            MediationSettings s =
                                    (MediationSettings) mediationSettingsConstructor
                                            .newInstance(withConfirmationDialog, withResultsDialog);
                            settings.add(s);
                        } catch (ClassNotFoundException e) {
                            Log.i(TAG, "could not find AdColonyInstanceMediationSettings class. " +
                                    "Did you add AdColony Network SDK to your Android folder?");
                        } catch (Exception e) {
                            printExceptionStackTrace(e);
                        }
                    }
                } else {
                    Log.e(TAG, "adVendor not available for custom mediation settings: " +
                            "[" + adVendor + "]");
                }
            }
        } catch (JSONException e) {
            printExceptionStackTrace(e);
        }

        return settings.toArray(new MediationSettings[settings.size()]);
    }
}






