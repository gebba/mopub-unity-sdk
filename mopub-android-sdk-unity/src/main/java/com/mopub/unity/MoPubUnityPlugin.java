package com.mopub.unity;

import android.app.Activity;
import android.util.Log;

import com.mopub.common.MoPub;
import com.mopub.mobileads.MoPubConversionTracker;
import com.unity3d.player.UnityPlayer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Base class for every available ad format plugin. Exposes APIs that the plugins might need.
 */
public class MoPubUnityPlugin {
    protected static String TAG = "MoPub";
    protected final String mAdUnitId;

    /**
     * Subclasses should use this to create instances of themselves.
     *
     * @param adUnitId String for the ad unit ID to use for the plugin.
     */
    public MoPubUnityPlugin(final String adUnitId) {
        mAdUnitId = adUnitId;
    }

    /**
     * Registers the given device as a Facebook Ads test device.
     * See https://developers.facebook.com/docs/reference/android/current/class/AdSettings/
     *
     * @param hashedDeviceId String with the hashed ID of the device.
     */
    public static void addFacebookTestDeviceId(String hashedDeviceId) {
        try {
            Class<?> cls = Class.forName("com.facebook.ads.AdSettings");
            Method method = cls.getMethod("addTestDevice", new Class[]{String.class});
            method.invoke(cls, hashedDeviceId);
            Log.i(TAG, "successfully added Facebook test device: " + hashedDeviceId);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "could not find Facebook AdSettings class. " +
                    "Did you add the Audience Network SDK to your Android folder?");
        } catch (NoSuchMethodException e) {
            Log.i(TAG, "could not find Facebook AdSettings.addTestDevice method. " +
                    "Did you add the Audience Network SDK to your Android folder?");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    /**
     * Reports an application being open for conversion tracking purposes.
     */
    public static void reportApplicationOpen() {
        runSafelyOnUiThread(new Runnable() {
            public void run() {
                new MoPubConversionTracker().reportAppOpen(getActivity());
            }
        });
    }


    /**
     * Specifies the desired location awareness settings: DISABLED, TRUNCATED or NORMAL.
     *
     * @param locationAwareness String with location awareness setting to be parsed as a
     *      {@link com.mopub.common.MoPub.LocationAwareness}.
     */
    public static void setLocationAwareness(final String locationAwareness) {
        runSafelyOnUiThread(new Runnable() {
            public void run() {
                MoPub.setLocationAwareness(MoPub.LocationAwareness.valueOf(locationAwareness));
            }
        });
    }


    /* ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****
     * Helper Methods                                                                          *
     * ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** ***** *****/

    protected static Activity getActivity() {
        return UnityPlayer.currentActivity;
    }

    protected static void runSafelyOnUiThread(final Runnable runner) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    runner.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected static void printExceptionStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Log.e(TAG, sw.toString());
    }
}
