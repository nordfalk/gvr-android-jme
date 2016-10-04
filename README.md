# gvr-android-jme

JMonkeyEngine integration with Google Cardboard VR SDK 1.0 for Android http://developers.google.com/vr/android/

Works in the Android Emulator for x86

This work is baset on https://github.com/oak1980/gvr-android-sdk-jme and https://github.com/neph1/jme-cardboard-example .
In includes libraries and the Treasurehunt sample from https://developers.google.com/vr/android/.

For information on JME see http://jmonkeyengine.org



# How to get started?

There is no setup - just clone the repo, open in Android Studio and run the samples

When you are ready to create your own app you can copy 'sample-jme-startravel' module.
You can also create a new Android module from scratch, just jake sure to copy the contents of build.gradle of the sample-jme-startravel sample.


# How to run JME+GVR in the emulator

Make sure you are running on a x86_64 emulator
Enter the settings of the emulator and change Graphics to "Software - GLES 2.0" in 'Emulated performance'.
(tested on Ubuntu 16, Android 6.0 x86_64 emulator 27/9/2016)


# Further information

Join the discussion at https://hub.jmonkeyengine.org/t/google-cardboard-support/31684


# Troubleshooting


## UnsatisfiedLinkError: couldn't find "libgvrbase.so"

You need to exclude JME processor archtectures that are not supported by GVR. In build.gradle, add the following

    defaultConfig {
        ...
        // Exclude architectures beyond those that are included in GVR to avoid
        // UnsatisfiedLinkError: couldn't find "libgvrbase.so"
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a", "x86"
        }
        ...
    }

## I need to modify something in the settings, which isnt exposed

Right now the only way I found (this is a really clumsy solution) is to override setSettings()

  public class TestJaime extends SimpleApplication {

    @Override
    public void setSettings(AppSettings settings) {
        settings.setAudioRenderer(AppSettings.ANDROID_MEDIAPLAYER);
        super.setSettings(settings);
    }

