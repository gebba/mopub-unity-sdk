#!/usr/bin/env bash
my_dir="$(dirname "$0")"
source "$my_dir/validate.sh"

PACKAGE_NAME=MoPubUnity
UNITY_BIN=/Applications/Unity/Unity.app/Contents/MacOS/Unity
PROJECT_PATH="`pwd`/unity/MoPubUnityPlugin"
OUT_DIR="`pwd`/mopub-unity-plugin"
DEST_PACKAGE="$OUT_DIR/$PACKAGE_NAME.unitypackage"
EXPORT_FOLDERS_MAIN="Assets/MoPub Assets/Plugins Assets/Scripts Assets/Scenes"
EXPORT_LOG="$OUT_DIR/exportlog.txt"

rm -rf $OUT_DIR
validate
mkdir -p $OUT_DIR
validate

echo "Attempting to export the main package..."

# Programatically export MoPub.unitypackage. This will export all folders under Assets/MoPub and Assets/Plugins, including
# all third-party network adapters. This is ok, in the next step, we remove those folders.

$UNITY_BIN -projectPath $PROJECT_PATH -quit -batchmode -logFile $EXPORT_LOG -exportPackage $EXPORT_FOLDERS_MAIN $DEST_PACKAGE
validate "Building the unity package has failed, please check $EXPORT_LOG\nMake sure Unity isn't running when invoking this script!"

# We need to remove the third-party network adapters from the main package. They will be exported separately.

echo "Done, removing third-party support folders from the main package..."

# Unpack the generated package.

mkdir -p $OUT_DIR/trim
validate
mv $DEST_PACKAGE $OUT_DIR/trim/mopub.tar.gz
validate
cd $OUT_DIR/trim
validate
tar -xf mopub.tar.gz
validate
rm mopub.tar.gz
validate

find . -name pathname -print0 | xargs -0 awk '{print $1, FILENAME}' | while read pathname filename; do
    parent="$(dirname "$filename")"

    # Remove any path that contains "Support", but leave the MoPubSDK headers in place.

    if [[ $pathname == *"Support/"* ]] && [[ $pathname != *"Support/MoPubSDK"* ]]; then
    	echo "Removing $filename ($pathname), and parent dir $parent from main package"
    	rm -rf $filename
    	validate
    	rm -rf $parent
    	validate
    fi

    # Remove third-party network adapters and dependencies for Android.

    if [[ $pathname == *"mopub-support/libs"* ]] || [[ $pathname == *"mm-ad-sdk"* ]] || [[ $pathname == *".aar"* ]]; then
    	echo "Removing $filename ($pathname), and parent dir $parent from main package"
    	rm -rf $filename
        validate
    	rm -rf $parent
        validate
    fi
done

# Repack the package.

tar -zcf "$PACKAGE_NAME.unitypackage" *
validate
mv "$PACKAGE_NAME.unitypackage" ../
validate
cd ..
rm -rf trim
validate

echo "Exported $DEST_PACKAGE"

# Now, export each of the third-party network adapters.

SUPPORT_LIBS=( "AdColony" "AdMob" "Chartboost" "Facebook" "UnityAds" "Vungle" )

for SUPPORT_LIB in "${SUPPORT_LIBS[@]}"
do
    IOS_EXPORT_FOLDERS_SUPPORT="Assets/MoPub/Editor/Support/$SUPPORT_LIB"
    ANDROID_EXPORT_FOLDERS_SUPPORT="Assets/Plugins/Android/mopub-support/libs/$SUPPORT_LIB"
    DEST_PACKAGE="$OUT_DIR/${SUPPORT_LIB}Support.unitypackage"

    $UNITY_BIN -projectPath $PROJECT_PATH -quit -batchmode -logFile $EXPORT_LOG -exportPackage $IOS_EXPORT_FOLDERS_SUPPORT $ANDROID_EXPORT_FOLDERS_SUPPORT $DEST_PACKAGE
    validate
    echo "Exported $SUPPORT_LIB (iOS: $IOS_EXPORT_FOLDERS_SUPPORT | Android: $ANDROID_EXPORT_FOLDERS_SUPPORT) to $DEST_PACKAGE"
done

# Millennial
MM_IOS_EXPORT_FOLDERS_SUPPORT="Assets/MoPub/Editor/Support/Millennial"
MM_ANDROID_EXPORT_FOLDERS_SUPPORT="Assets/Plugins/Android/mopub-support/libs/Millennial"
MM_ANDROID_SDK_FOLDER="Assets/Plugins/Android/mopub-support/libs/Millennial"
MM_DEST_PACKAGE="$OUT_DIR/MillennialSupport.unitypackage"

$UNITY_BIN -projectPath $PROJECT_PATH -quit -batchmode -logFile $EXPORT_LOG -exportPackage $MM_IOS_EXPORT_FOLDERS_SUPPORT $MM_ANDROID_EXPORT_FOLDERS_SUPPORT $MM_ANDROID_SDK_FOLDER $MM_DEST_PACKAGE
validate
echo "Exported Millennial (iOS: $MM_IOS_EXPORT_FOLDERS_SUPPORT | Android: $MM_ANDROID_EXPORT_FOLDERS_SUPPORT | MM Android SDK: $MM_ANDROID_SDK_FOLDER) to $MM_DEST_PACKAGE"
