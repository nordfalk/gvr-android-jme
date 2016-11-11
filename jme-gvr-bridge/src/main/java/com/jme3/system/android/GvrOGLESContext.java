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

import com.google.vr.sdk.base.*;
import com.jme3.app.LegacyApplication;
import com.jme3.input.*;
import com.jme3.input.android.AndroidInputHandler;
import com.jme3.input.controls.SoftTextDialogInputListener;
import com.jme3.input.dummy.DummyKeyInput;
import com.jme3.input.dummy.DummyMouseInput;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.renderer.Camera;
import com.jme3.renderer.android.AndroidGL;
import com.jme3.renderer.opengl.GL;
import com.jme3.renderer.opengl.GLExt;
import com.jme3.renderer.opengl.GLFbo;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.system.*;

import javax.microedition.khronos.egl.EGLConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author oak
 */
public class GvrOGLESContext implements JmeContext, GvrView.StereoRenderer, SoftTextDialogInput {

    private static final Logger logger = Logger.getLogger(GvrOGLESContext.class.getName());
    protected final AtomicBoolean created = new AtomicBoolean(false);
    protected final AtomicBoolean renderable = new AtomicBoolean(false);
    protected final AtomicBoolean needClose = new AtomicBoolean(false);
    protected AppSettings settings = new AppSettings(true);

    protected GLRenderer renderer;
    protected Timer timer;
    protected SystemListener listener;
    protected boolean autoFlush = true;
    protected AndroidInputHandler androidInput;
    private LegacyApplication app;

    private static final float Z_NEAR = 1.0f;
    private static final float Z_FAR = 1000.0f;

    public GvrOGLESContext() {
    }

    @Override
    public Type getType() {
        return Type.Display;
    }

    protected void initInThread() {
        created.set(true);

        logger.fine("GvrOGLESContext create");
        logger.log(Level.FINE, "Running on thread: {0}", Thread.currentThread().getName());

        // Setup unhandled Exception Handler
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable thrown) {
                listener.handleError("Exception thrown in " + thread.toString(), thrown);
            }
        });

        timer = new NanoTimer();
        Object gl = new AndroidGL();
        // gl = GLTracer.createGlesTracer((GL)gl, (GLExt)gl);
        // gl = new GLDebugES((GL)gl, (GLExt)gl);
        renderer = new GLRenderer((GL)gl, (GLExt)gl, (GLFbo)gl);
        renderer.initialize();

        JmeSystem.setSoftTextDialogInput(this);

        needClose.set(false);
    }

    /**
     * De-initialize in the OpenGL thread.
     */
    protected void deinitInThread() {
        if (renderable.get()) {
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
    }

    @Override
    public void setSettings(AppSettings settings) {
        this.settings.copyFrom(settings);
        if (androidInput != null) {
            androidInput.loadSettings(settings);
        }
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
    public com.jme3.renderer.Renderer getRenderer() {
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
    public void create(boolean waitFor) {
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

    protected void waitFor(boolean createdVal) {
        while (renderable.get() != createdVal) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void requestDialog(final int id, final String title, final String initialValue, final SoftTextDialogInputListener listener) {
        throw new RuntimeException("FIXME: we cannot do a dialog right now");
    }

    private float[] headRotation = new float[4];
    private Quaternion orientation = new Quaternion();

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        logger.fine("onNewFrame ");
        // Build the camera matrix and apply it to the ModelView.
        if(app.getCamera() != null) {
            Camera cam = app.getCamera();
            headTransform.getQuaternion(headRotation, 0);
            orientation.set(headRotation[3], headRotation[2], headRotation[1], headRotation[0]);
            cam.setRotation(orientation);
        }
    }

    float[] perspective = new float[16];
    private Matrix4f projMatrix = new Matrix4f();

    private void setCamProjectionMatrix(Eye eye, Camera cam) {
        FieldOfView fov = eye.getFov();

        // Fix a Frustum (they represent it as angles, so we have to calculate a little)
        float l = (float) (-Math.tan(Math.toRadians((double) fov.getLeft()))) * Z_NEAR;
        float r = (float) Math.tan(Math.toRadians((double) fov.getRight())) * Z_NEAR;
        float b = (float) (-Math.tan(Math.toRadians((double) fov.getBottom()))) * Z_NEAR;
        float t = (float) Math.tan(Math.toRadians((double) fov.getTop())) * Z_NEAR;
        cam.setFrustum(Z_NEAR, Z_FAR, l, r, t, b);

        // Setup perspective
        fov.toPerspectiveMatrix(Z_NEAR, Z_FAR, perspective, 0);
        projMatrix.set(perspective);
        cam.setProjectionMatrix(projMatrix);
    }

    // SystemListener:update
    @Override
    public void onDrawEye(Eye eye) {
        // FIXME: Move the updating part into onNewFrame(...)
        String eyeName = (eye.getType() == Eye.Type.LEFT ? "Left" : (eye.getType() == Eye.Type.RIGHT ? "Right" : "MONOCULAR"));
        logger.fine("onDrawEye "+eyeName);

        if(app.getCamera() != null) {

            setCamProjectionMatrix(eye, app.getCamera());

        }


        if (needClose.get()) {
            deinitInThread();
            return;
        }

        if (!renderable.get()) {
            if (created.get()) try {
                logger.fine("GL Surface is setup, initializing application");
                listener.initialize();

                // TODO: Figure out if it makes more sense to have left/right camera (we could clone them here)
                //Camera cam = app.getCamera();

                renderable.set(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("SEVERE ERROR - EXITING !");
                // com.google.vr.sdk.base.CardboardViewNativeImpl.nativeOnDrawFrame(Native Method) does not kill the app
                // (as of GVR 1.0 / october 1st 2016), so we must do it ourselves to avoid loops
                System.exit(-1);
            }
        } else {
            if (!created.get()) {
                throw new IllegalStateException("onDrawFrame without create");
            }

            listener.update();
            if (autoFlush) {
                renderer.postFrame();
            }
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
            if (!created.get()) {
                logger.fine("GL Surface created, initializing JME3 renderer");
                initInThread();
            } else {
                logger.warning("GL Surface already created");
            }
        }
    }

    @Override
    public void onRendererShutdown() {

    }

    public void setApp(LegacyApplication app) {
        this.app = app;
    }
}
