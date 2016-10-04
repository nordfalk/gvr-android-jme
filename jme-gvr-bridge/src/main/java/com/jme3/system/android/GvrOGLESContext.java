package com.jme3.system.android;

/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.FieldOfView;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.jme3.app.LegacyApplication;
import com.jme3.input.JoyInput;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.SoftTextDialogInput;
import com.jme3.input.TouchInput;
import com.jme3.input.android.AndroidInputHandler;
import com.jme3.input.controls.SoftTextDialogInputListener;
import com.jme3.input.dummy.DummyKeyInput;
import com.jme3.input.dummy.DummyMouseInput;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.android.AndroidGL;
import com.jme3.renderer.opengl.GL;
import com.jme3.renderer.opengl.GLExt;
import com.jme3.renderer.opengl.GLFbo;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.NanoTimer;
import com.jme3.system.SystemListener;
import com.jme3.system.Timer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * - original
 * @author oak
 *
 * - Slight modification to limit branching etc
 * @author rudz
 */
public class GvrOGLESContext implements JmeContext, GvrView.StereoRenderer, SoftTextDialogInput {

    private static final Logger logger = Logger.getLogger(GvrOGLESContext.class.getName());
    private static final String TAG = "GvrOGLESContext";
    private final AtomicBoolean created = new AtomicBoolean(false);
    private final AtomicBoolean renderable = new AtomicBoolean(false);
    private final AtomicBoolean needClose = new AtomicBoolean(false);
    protected AppSettings settings = new AppSettings(true);

    private GLRenderer renderer;
    private Timer timer;
    private SystemListener listener;
    private boolean autoFlush = true;
    private AndroidInputHandler androidInput;
    private LegacyApplication app;

    private static final float Z_NEAR = 1.0f;
    private static final float Z_FAR = 1000.0f;

    GvrOGLESContext() {
        super();
    }

    public GvrOGLESContext(final AppSettings settings) {
        setSettings(settings);
    }

    @Override
    public Type getType() {
        return Type.Display;
    }

    private void initInThread() {
        created.set(true);

        logger.fine("GvrOGLESContext create");
        logger.log(Level.FINE, "Running on thread: {0}", Thread.currentThread().getName());

        // Setup unhandled Exception Handler
        Thread.currentThread().setUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());

        timer = new NanoTimer();
        Object gl = new AndroidGL();
        // gl = GLTracer.createGlesTracer((GL)gl, (GLExt)gl);
        // gl = new GLDebugES((GL)gl, (GLExt)gl);
        renderer = new GLRenderer((GL) gl, (GLExt) gl, (GLFbo) gl);
        renderer.initialize();

        JmeSystem.setSoftTextDialogInput(this);

        needClose.set(false);
    }

    /**
     * De-initialize in the OpenGL thread.
     */
    private void deinitInThread() {
        if (!renderable.get()) {
            return;
        }

        created.set(false);
        if (renderer != null) {
            renderer.cleanup();
        }

        listener.destroy();

        listener = null;
        renderer = null;
        timer = null;

        // do android specific cleaning here
        logger.fine("Display destroyed.");

        renderable.set(false);
    }

    @Override
    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);

        if (androidInput == null) {
            return;
        }

        androidInput.loadSettings(settings);
    }

    @Override
    public void setSystemListener(SystemListener listener) {
        this.listener = listener;
    }

    @Override
    public AppSettings getSettings() {
        return settings;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public MouseInput getMouseInput() {
        return new DummyMouseInput();
    }

    @Override
    public KeyInput getKeyInput() {
        return new DummyKeyInput();
    }

    @Override
    public JoyInput getJoyInput() {
        /* FIXME: We have not touch input :-(
        return androidInput.getJoyInput();
         */
        return null;
    }

    @Override
    public TouchInput getTouchInput() {
        /* FIXME: We have not touch input :-(
        return androidInput.getTouchInput();
         */
        return null;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public boolean isCreated() {
        return created.get();
    }

    @Override
    public void setAutoFlushFrames(boolean enabled) {
        this.autoFlush = enabled;
    }

    @Override
    public boolean isRenderable() {
        return renderable.get();
    }

    @Override
    public void create(final boolean waitFor) {
        if (waitFor) {
            waitFor(true);
        }
    }

    @Override
    public void restart() {
    }

    @Override
    public void destroy(boolean waitFor) {
        needClose.set(true);
        if (waitFor) {
            waitFor(false);
        }
    }

    private void waitFor(final boolean createdVal) {
        while (renderable.get() != createdVal) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                // nothing happends.
            }
        }
    }

    public void requestDialog(final int id, final String title, final String initialValue, final SoftTextDialogInputListener listener) {
        throw new RuntimeException("FIXME: we cannot do a dialog right now");
    }

    private final float[] headRotation = new float[4];
    private final Quaternion orientation = new Quaternion();

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the camera matrix and apply it to the ModelView.
        Camera cam = app.getCamera();
        if (cam == null) {
            return;
        }
        headTransform.getQuaternion(headRotation, 0);
        orientation.set(headRotation[3], headRotation[2], headRotation[1], headRotation[0]);
        cam.setRotation(orientation);
    }

    private final float[] perspective = new float[16];
    private final Matrix4f projMatrix = new Matrix4f();

    // SystemListener:update
    @Override
    public void onDrawEye(Eye eye) {
        // FIXME: Move the updating part into onNewFrame(...)


        Camera cam = app.getCamera();

        if (cam != null) {
            logger.fine("Eye: " + (eye.getType() == Eye.Type.LEFT ? "Left" : eye.getType() == Eye.Type.RIGHT ? "Right" : "MONOCULAR"));

            FieldOfView fov = eye.getFov();

            // Fix a Frustum (they represent it as angles, so we have to calculate a little)
            float l = (float) -Math.tan(Math.toRadians(fov.getLeft())) * Z_NEAR;
            float r = (float) Math.tan(Math.toRadians(fov.getRight())) * Z_NEAR;
            float b = (float) -Math.tan(Math.toRadians(fov.getBottom())) * Z_NEAR;
            float t = (float) Math.tan(Math.toRadians(fov.getTop())) * Z_NEAR;
            cam.setFrustum(Z_NEAR, Z_FAR, l, r, t, b);

            // Setup perspective
            fov.toPerspectiveMatrix(Z_NEAR, Z_FAR, perspective, 0);
            projMatrix.set(perspective);
            cam.setProjectionMatrix(projMatrix);

        }

        if (needClose.get()) {
            deinitInThread();
            return;
        }

        if (!renderable.get()) {
            if (created.get()) {
                logger.fine("GL Surface is setup, initializing application");
                listener.initialize();

                // TODO: Figure out if it makes more sense to have left/right camera (we could clone them here)
                //Camera cam = app.getCamera();

                renderable.set(true);
            }
            return;
        }

        if (!created.get()) {
            throw new IllegalStateException("onDrawFrame without create");
        }

        listener.update();

        if (autoFlush) {
            renderer.postFrame();
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        logger.log(Level.FINE, "GL Surface changed, width: {0} height: {1}", new Object[]{width, height});
        // update the application settings with the new resolution
        settings.setResolution(width, height);

        /* FIXME: We have no input!
        // reload settings in androidInput so the correct touch event scaling can be
        // calculated in case the surface resolution is different than the view
        androidInput.loadSettings(settings);
        */
        // if the application has already been initialized (ie renderable is set)
        // then call reshape so the app can adjust to the new resolution.
        if (renderable.get()) {
            logger.log(Level.FINE, "App already initialized, calling reshape");
            listener.reshape(width, height);
        }
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        if (created.get() && renderer != null) {
            renderer.resetGLObjects();
        } else {
            if (created.get()) {
                logger.warning("GL Surface already created");
            } else {
                logger.fine("GL Surface created, initializing JME3 renderer");
                initInThread();
            }
        }
    }

    @Override
    public void onRendererShutdown() {
        Log.v(TAG, "Renderer -> shutdown");
    }

    public void setApp(LegacyApplication app) {
        this.app = app;
    }

    private class ThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread thread, Throwable thrown) {
            listener.handleError("Exception thrown in " + thread, thrown);
        }
    }
}
