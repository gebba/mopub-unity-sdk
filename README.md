# MoPub Unity SDK

Thanks for taking a look at MoPub! We take pride in having an easy-to-use, flexible monetization solution that works across multiple platforms.

Sign up for an account at [http://app.mopub.com/](http://app.mopub.com/).

## Need Help?

To get started visit our [Unity Engine Integration](https://www.mopub.com/resources/docs/unity-engine-integration/) guide and find additional help documentation on our [developer help site](http://dev.twitter.com/mopub).

To file an issue with our team please email [support@mopub.com](mailto:support@mopub.com).

## New in This Version (4.16.1 - September 8, 2017)
- The MoPub Unity Plugin is now fully open source! Please see below for details and building instructions.
- This release does not change the SDK compatibility; the Plugin is still compatible with version 4.16.1 of the MoPub Android SDK and version 4.16.0 of the MoPub iOS SDK.

Please view the [changelog](https://github.com/mopub/mopub-unity-sdk/blob/master/CHANGELOG.md) for a complete list of additions, fixes, and enhancements in all releases.

## License

The MoPub SDK License can be found at [http://www.mopub.com/legal/sdk-license-agreement/](http://www.mopub.com/legal/sdk-license-agreement/).

## Developing on the MoPub Unity Plugin

### Cloning the project
```
git clone https://github.com/mopub/unity-mopub
git submodule init
git submodule update
```

### Repository structure

* `mopub-android-sdk/` - Git submodule of the MoPub Android SDK
* `mopub-android-sdk-unity/` - Contains a project that adds Unity-specific files to the Android SDK
* `mopub-ios-sdk/` - Git submodule of the MoPub iOS SDK
* `mopub-ios-sdk-unity/` - Contains a project that adds Unity-specific files to the iOS SDK
* `unity/` - Contains the Unity Plugin
* `mopub-unity-plugin/` - Where the Unity packages are exported after running `./unity-export-package.sh`

### How do I build?

Simply run `./build.sh` (make sure the Unity IDE is *not* running), which runs `git submodule update` and then invokes the following scripts:

* `mopub-android-sdk-unity-build.sh` - builds the mopub-android-sdk-unity project and copies the resulting artifacts into `unity/`
* `mopub-ios-sdk-unity-build.sh` - builds the mopub-ios-sdk-unity project and copies the resulting artifacts into `unity/`
* `unity-export-package.sh`  - exports the unity package into `mopub-unity-plugin/`

Each script can be invoked separately. Exporting the unity package can also be done manually, by opening the `unity/` project in Unity, right-clicking the `Assets/` folder and chosing `Export Package...`.

### How do I run the sample unity project and test?

After building per instructions above, open the `unity/` project in Unity, click `File > Build Settings...`, select iOS or Android, click `Build and Run`.
