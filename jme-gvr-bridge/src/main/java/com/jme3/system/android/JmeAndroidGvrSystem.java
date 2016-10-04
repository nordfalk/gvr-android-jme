/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.system.android;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;

/**
 *
 * @author emanuel
 */
public class JmeAndroidGvrSystem extends JmeAndroidSystem {
    @Override
    public JmeContext newContext(AppSettings settings, JmeContext.Type contextType) {

        // TODO : This can actually be removed as it can easily be replaced by generic settings configuration module.
        /*
        if (settings.getAudioRenderer().equals(AppSettings.ANDROID_MEDIAPLAYER)) {
            audioRendererType = AppSettings.ANDROID_MEDIAPLAYER;
        } else if (settings.getAudioRenderer().equals(AppSettings.ANDROID_OPENAL_SOFT)) {
            audioRendererType = AppSettings.ANDROID_OPENAL_SOFT;
        } else {
            logger.log(Level.INFO, "AudioRenderer not set. Defaulting to OpenAL Soft");
            audioRendererType = AppSettings.ANDROID_OPENAL_SOFT;
        }
        */

        initialize(settings);
        return new GvrOGLESContext(settings);
    }

}
