/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jme3test.android.startravel;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.jme3.app.AndroidGvrHarness;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * A Google VR sample application.
 * </p><p>
 * The TreasureHunt scene consists of a planar ground grid and a floating
 * "treasure" cube. When the user looks at the cube, the cube will turn gold.
 * While gold, the user can activate the Carboard trigger, which will in turn
 * randomly reposition the cube.
 */
public class StarTravelActivity extends AndroidGvrHarness implements GvrView.StereoRenderer {

  private static final String TAG = "StarTravelActivity";

  private static final String SOUND_FILE = "cube_sound.wav";

  private Vibrator vibrator;

  private GvrAudioEngine gvrAudioEngine;
  private volatile int soundId = GvrAudioEngine.INVALID_ID;

  public StarTravelActivity() {
    super();
    appClass = CardboardStarTravel.class.getCanonicalName();
  }

  /**
   * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
   *
   * @param label Label to report in case of error.
   */
  private static void checkGLError(String label) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Log.e(TAG, label + ": glError " + error);
      throw new RuntimeException(label + ": glError " + error);
    }
  }

  /**
   * Sets the view to our GvrView and initializes the transformation matrices we will use
   * to render our scene.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initializeGvrView();

    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    // Initialize 3D audio engine.
    gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
  }

  public void initializeGvrView() {
    setContentView(R.layout.common_ui);

    GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
    gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

    gvrView.setRenderer(this);
    gvrView.setTransitionViewEnabled(true); // https://developers.google.com/vr/android/reference/com/google/vr/sdk/base/GvrView.html#public-methods_2
    gvrView.setOnCardboardBackListener(
        new Runnable() {
          @Override
          public void run() {
            onBackPressed();
          }
        });
    setGvrView(gvrView);
  }

  @Override
  public void onPause() {
    gvrAudioEngine.pause();
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    gvrAudioEngine.resume();
  }

  @Override
  public void onRendererShutdown() {
    ctx.onRendererShutdown();
    Log.i(TAG, "onRendererShutdown");
  }

  @Override
  public void onSurfaceChanged(int width, int height) {
    ctx.onSurfaceChanged(width, height);
    Log.i(TAG, "onSurfaceChanged");
  }

  /**
   * Creates the buffers we use to store information about the 3D world.
   *
   * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
   * Hence we use ByteBuffers.
   *
   * @param config The EGL configuration used when creating the surface.
   */
  @Override
  public void onSurfaceCreated(EGLConfig config) {
    ctx.onSurfaceCreated(config);

    Log.i(TAG, "onSurfaceCreated");
    GLES20.glClearColor(0.1f, 0.7f, 0.1f, 0.5f); // Dark background so text shows up well.

    // Avoid any delays during start-up due to decoding of sound files.
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                // Start spatial audio playback of SOUND_FILE at the model postion. The returned
                //soundId handle is stored and allows for repositioning the sound object whenever
                // the cube position changes.
                gvrAudioEngine.preloadSoundFile(SOUND_FILE);
                soundId = gvrAudioEngine.createSoundObject(SOUND_FILE);
                gvrAudioEngine.setSoundObjectPosition(
                    soundId, 0.1f, 2.0f, 0.0f);
                gvrAudioEngine.playSound(soundId, true /* looped playback */);
              }
            })
        .start();

    checkGLError("onSurfaceCreated");
  }

  /**
   * Prepares OpenGL ES before we draw a frame.
   *
   * @param headTransform The head transformation in the new frame.
   */
  @Override
  public void onNewFrame(HeadTransform headTransform) {


    /*
    // Build the camera matrix and apply it to the ModelView.
    Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

    headTransform.getHeadView(headView, 0);
    */

    // Update the 3d audio engine with the most recent head rotation.
    /* FIXME: Add jME AudioRenderer
    headTransform.getQuaternion(headRotation, 0);
    gvrAudioEngine.setHeadRotation(
        headRotation[0], headRotation[1], headRotation[2], headRotation[3]);

    // Regular update call to GVR audio engine.
    gvrAudioEngine.update();

    checkGLError("gvrAudioEngine.update()");
    */

    ctx.onNewFrame(headTransform);
  }

  /**
   * Draws a frame for an eye.
   *
   * @param eye The eye to render. Includes all required transformations.
   */
  @Override
  public void onDrawEye(Eye eye) {
    ctx.onDrawEye(eye);

    checkGLError("ctx.onDrawEye(eye)");
  }

  @Override
  public void onFinishFrame(Viewport viewport) {
    ctx.onFinishFrame(viewport);
  }

  /**
   * Called when the Cardboard trigger is pulled.
   */
  @Override
  public void onCardboardTrigger() {
    Log.i(TAG, "onCardboardTrigger XXXX");

    // Always give user feedback.
    vibrator.vibrate(50);
  }
}
