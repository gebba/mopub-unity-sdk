#!/usr/bin/env bash
my_dir="$(dirname "$0")"
source "$my_dir/validate.sh"

# Current SDK version
SDK_VERSION=4.16.1

# Append "+unity" suffix to SDK_VERSION in MoPub.java
sed -i.bak 's/^\(.*public static final String SDK_VERSION\)\(.*\)"/\1\2+unity"/' mopub-android-sdk/mopub-sdk/mopub-sdk-base/src/main/java/com/mopub/common/MoPub.java
validate

# Build mopub-android-sdk-unity project
cd mopub-android-sdk-unity
./gradlew clean
./gradlew assembleRelease
validate
cd ..

# Undo +unity suffix after build
cd mopub-android-sdk
git checkout mopub-sdk/mopub-sdk-base/src/main/java/com/mopub/common/MoPub.java
validate
rm -f mopub-sdk/mopub-sdk-base/src/main/java/com/mopub/common/MoPub.java.bak
validate
cd ..

# Copy the generated jars into the unity package:
#   * mopub-unity-plugins.jar: unity plugins for banner, interstitial, and rewarded video
#   * mopub-sdk-*.jar: modularized SDK jars (excluding native-static and native-video)
cp mopub-android-sdk-unity/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub/libs/mopub-unity-plugins.jar
validate
cp mopub-android-sdk/mopub-sdk/mopub-sdk-base/build/intermediates/bundles/default/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub/libs/mopub-sdk-base.jar
validate
cp mopub-android-sdk/mopub-sdk/mopub-sdk-banner/build/intermediates/bundles/default/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub/libs/mopub-sdk-banner.jar
validate
cp mopub-android-sdk/mopub-sdk/mopub-sdk-interstitial/build/intermediates/bundles/default/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub/libs/mopub-sdk-interstitial.jar
validate
cp mopub-android-sdk/mopub-sdk/mopub-sdk-rewardedvideo/build/intermediates/bundles/default/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub/libs/mopub-sdk-rewardedvideo.jar
validate

# Copy MoPub SDK dependency jars
cp $ANDROID_HOME/extras/android/support/v4/android-support-v4.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub/libs/android-support-v4-23.1.1.jar
validate

# Copy MoPub Custom Events jars
cp mopub-android-sdk-unity/adcolony-custom-events/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub-support/libs/AdColony/mopub-adcolony-custom-events.jar
validate
cp mopub-android-sdk-unity/admob-custom-events/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub-support/libs/AdMob/mopub-admob-custom-events.jar
validate
cp mopub-android-sdk-unity/chartboost-custom-events/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub-support/libs/Chartboost/mopub-chartboost-custom-events.jar
validate
cp mopub-android-sdk-unity/facebook-custom-events/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub-support/libs/Facebook/mopub-facebook-custom-events.jar
validate
cp mopub-android-sdk-unity/millennial-custom-events/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub-support/libs/Millennial/mopub-millennial-custom-events.jar
validate
cp mopub-android-sdk-unity/unityads-custom-events/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub-support/libs/UnityAds/mopub-unityads-custom-events.jar
validate
cp mopub-android-sdk-unity/vungle-custom-events/build/intermediates/bundles/release/classes.jar unity/MoPubUnityPlugin/Assets/Plugins/Android/mopub-support/libs/Vungle/mopub-vungle-custom-events.jar
validate
