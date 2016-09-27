/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.android.jaime;


import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimationFactory;
import com.jme3.animation.LoopMode;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.ResetStatsState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.PlayState;
import com.jme3.cinematic.events.AnimationEvent;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;

/**
 *
 * @author Nehon
 */
public class TestJaime extends SimpleApplication {

    private Node observer;
    Cinematic cinematic;
    private Material mat;

    public static void main(String... argv){
        TestJaime app = new TestJaime();
        app.setShowSettings(false);
        app.start();
    }
/*
    @Override
    public void setSettings(AppSettings settings) {
        settings.setAudioRenderer(AppSettings.ANDROID_MEDIAPLAYER);
        super.setSettings(settings);
    }
*/
    @Override
    public void simpleInitApp() {
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        stateManager.detach(stateManager.getState(ResetStatsState.class));
        stateManager.detach(stateManager.getState(DebugKeysAppState.class));
        stateManager.detach(stateManager.getState(StatsAppState.class));
        final Node jaime = LoadModel();
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        setupLights();
        setupCamera();
        setupFloor();
        setupCinematic(jaime);
        setupInput();
    }

    public Node LoadModel() {
        Node jaime = (Node)assetManager.loadModel("Models/Jaime/Jaime.j3o");
        jaime.move(0, 0, -3);
        jaime.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(jaime);
        return jaime;
    }

    public void setupLights() {
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        rootNode.addLight(al);

        SpotLight sl = new SpotLight();
        sl.setColor(ColorRGBA.White.mult(1.0f));
        sl.setPosition(new Vector3f(1.2074411f, 10.6868908f, 4.1489987f));
        sl.setDirection(sl.getPosition().mult(-1));
        sl.setSpotOuterAngle(FastMath.QUARTER_PI);
        sl.setSpotInnerAngle(0.004f);
        rootNode.addLight(sl);

//        SpotLightShadowRenderer shadows = new SpotLightShadowRenderer(assetManager, 1024);
//        shadows.setLight(sl);
//        shadows.setShadowIntensity(0.3f);
//        shadows.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
//        viewPort.addProcessor(shadows);
//
//

    }

    public void setupCamera() {
        flyCam.setEnabled(false);
        observer = new Node("Observer");
        rootNode.attachChild(observer);
    }

    public void setupCinematic(final Node jaime) {
        cinematic = new Cinematic(rootNode, 60);
        stateManager.attach(cinematic);

        jaime.move(0, -1, -6);
        AnimationFactory af = new AnimationFactory(0.7f, "JumpForward");
        af.addTimeTranslation(0, new Vector3f(0, -1, -6));
        af.addTimeTranslation(0.35f, new Vector3f(0, 0, -4.5f));
        af.addTimeTranslation(0.7f, new Vector3f(0, -1, -3));
        jaime.getControl(AnimControl.class).addAnim(af.buildAnimation());

        cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "Idle",3, LoopMode.DontLoop));
        float jumpStart = cinematic.enqueueCinematicEvent(new AnimationEvent(jaime, "JumpStart", LoopMode.DontLoop));
        cinematic.addCinematicEvent(jumpStart+0.2f, new AnimationEvent(jaime, "JumpForward", LoopMode.DontLoop,1));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "JumpEnd", LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Punches", LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "SideKick", LoopMode.DontLoop));
        float camStart = cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Taunt", LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Idle",1, LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Wave", LoopMode.DontLoop));
        cinematic.enqueueCinematicEvent( new AnimationEvent(jaime, "Idle", LoopMode.DontLoop));

        CameraNode camNode = cinematic.bindCamera("cam", cam);
        observer.attachChild(camNode);

//        camNode.lookAt(new Vector3f(0, 0.5f, 0), Vector3f.UNIT_Y);

//        MotionPath path = new MotionPath();
//        path.addWayPoint(new Vector3f(1.1f, 1.2f, 2.9f));
//        path.addWayPoint(new Vector3f(0f, 1.2f, 3.0f));
//        path.addWayPoint(new Vector3f(-1.1f, 1.2f, 2.9f));
//        path.enableDebugShape(assetManager, rootNode);
//        path.setCurveTension(0.8f);

//        MotionEvent camMotion = new MotionEvent(camNode, path,6);
//        camMotion.setDirectionType(MotionEvent.Direction.LookAt);
//        camMotion.setLookAt(new Vector3f(0, 0.5f, 0), Vector3f.UNIT_Y);
//        cinematic.addCinematicEvent(camStart, camMotion);
//        cinematic.activateCamera(0, "cam");


        cinematic.fitDuration();
        cinematic.setSpeed(1.2f);
        cinematic.setLoopMode(LoopMode.Loop);
        cinematic.play();
    }

    public void setupFloor() {
        Quad q = new Quad(20, 20);
        q.scaleTextureCoordinates(Vector2f.UNIT_XY.mult(10));
        Geometry geom = new Geometry("floor", q);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setColor("Ambient", ColorRGBA.Black);
        mat.setBoolean("UseMaterialColors", true);
        mat.setFloat("Shininess", 0);
        geom.setMaterial(mat);

        geom.rotate(-FastMath.HALF_PI, 0, 0);
        geom.center();
        geom.setShadowMode(RenderQueue.ShadowMode.Receive);
        rootNode.attachChild(geom);
    }

    public void setupInput() {
        inputManager.addMapping("start", new KeyTrigger(KeyInput.KEY_PAUSE));
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                if(name.equals("start") && isPressed){
                    if(cinematic.getPlayState() != PlayState.Playing){
                        cinematic.play();
                    }else{
                        cinematic.pause();
                    }
                }
            }
        }, "start");
    }
}