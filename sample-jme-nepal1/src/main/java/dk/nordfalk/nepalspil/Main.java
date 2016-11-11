package dk.nordfalk.nepalspil;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.AndroidLocator;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.Arrays;

import dk.nordfalk.nepalspil.kontrol.BrikRoterKontrol;
import dk.nordfalk.nepalspil.kontrol.BrikStøvKontrol;

/**
 *
 * @author Jacob Nordfalk
 */
public class Main extends SimpleApplication {
    public static boolean ANDROID_WORKAROUND;

    private ArrayList<Spatial> felter = new ArrayList<>();
    private ArrayList<Spiller> spillere = new ArrayList<>();
    float tidTilRyk = 1;

    BitmapText infoTekst;

    public static void main(String[] args) {
        Main app = new Main();
        app.setShowSettings(false);

        AppSettings cfg = new AppSettings(true);
        cfg.setFrameRate(60); // set to less than or equal screen refresh rate
        cfg.setResolution(1280, 720);   
        cfg.setFrequency(60); // set to screen refresh rate
        cfg.setTitle("Nepalspillet - Jacob Nordfalk");
        cfg.setVSync(true);   // prevents page tearing
        cfg.setSamples(4);    // anti-aliasing
        app.setSettings(cfg);

        app.start();
    }


    @Override
    public void simpleInitApp() {
        Node manojBrik = lavSpillerbrik(assetManager.loadTexture("Textures/klippet-manoj.png"));
        Node laxmiBrik = lavSpillerbrik(assetManager.loadTexture("Textures/klippet-laxmi.png"));
        Node abishakBrik = lavSpillerbrik(assetManager.loadTexture("Textures/klippet-abishak.png"));
        Node bishalBrik = lavSpillerbrik(assetManager.loadTexture("Textures/klippet-bishal.png"));
        abishakBrik.rotate(0, 10, 0).scale(0.5f);
        bishalBrik.rotate(0, 10, 0).scale(0.5f);;
        laxmiBrik.rotate(0, 10, 0).scale(0.5f);;
        
        spillere.addAll(Arrays.asList(
                new Spiller("Manoj", manojBrik),
                new Spiller("Laxmi", laxmiBrik),
                new Spiller("Abishak", abishakBrik),
                new Spiller("Bishal", bishalBrik)));

        if (ANDROID_WORKAROUND) {
            // Workaround for missing texture because of wrong path in the .j3o files created by the JME3 scene editor
            // See https://github.com/jMonkeyEngine/jmonkeyengine/issues/352
            AssetManager assetManager = getAssetManager();
            assetManager.unregisterLocator("/", AndroidLocator.class);
            assetManager.registerLocator("", AndroidLocator.class);

            // Culling is too aggressive in GVR - so disable it for now
            rootNode.setCullHint(Spatial.CullHint.Never);
        }

        guiFont = assetManager.loadFont("Interface/Fonts/FreeSans.fnt");
        infoTekst = new BitmapText(guiFont, false);
        infoTekst.setText("Her kommer en tekst");
        infoTekst.setLocalTranslation(300, infoTekst.getLineHeight()+30, 0);
        guiNode.attachChild(infoTekst);
        
/*
*/
        Spatial scene = assetManager.loadModel("Scenes/spilScene.j3o");
        rootNode.attachChild(scene);

        for (int i = 1;; i++) {
            Spatial felt = rootNode.getChild("Felt" + i);
            if (felt == null) break;
            felter.add(felt);
        }
        System.out.println("felter= " + felter);
        for (Spiller sp : spillere) {
            rootNode.attachChild(sp.node);
            sp.ryk.startRykTil(felter.get(0));
            sp.node.addControl(new BrikStøvKontrol(assetManager, sp.node));
        }
/*        
        // Flyt belysning fra scenen over til rootNode - ellers belyses brikkerne ikke som resten!!!!
        for (Light l : scene.getLocalLightList().clone()) rootNode.addLight(l); 
        scene.getLocalLightList().clear();

        DirectionalLight l = (DirectionalLight) rootNode.getLocalLightList().get(0); //  new DirectionalLight();

        int SHADOWMAP_SIZE = 1024;
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(l);
        //dlsr.setLambda(0.055f);
        dlsr.setShadowIntensity(0.5f);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.Bilinear);
        viewPort.addProcessor(dlsr);

        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
*/
//        rootNode.setCullHint(Spatial.CullHint.Never);
        // Ryk kameraet op og til siden
        cam.setLocation( cam.getLocation().add(-2, 4, -4));
    }

    private Node lavSpillerbrik(Texture billede) {
        Material fodMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fodMat.setTexture("ColorMap", assetManager.loadTexture("Textures/dirt.jpg"));
        Spatial fod = assetManager.loadModel("Models/nepalbrik-fod/nepalbrik-fodfbx.j3o");
        fod.setMaterial(fodMat);

        Geometry brikGeom = new Geometry("Brikbillede", new Box(1, 2, 0.1f));
        Node billedeNode = new Node();
        billedeNode.attachChild(brikGeom);
        billedeNode.setLocalTranslation(0, 3, 0);
        Material laxmiMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        laxmiMat.setTexture("ColorMap", billede);
        brikGeom.setMaterial(laxmiMat);

        Node fodOgBilledeNode = new Node(billede.getName());
        fodOgBilledeNode.attachChild(billedeNode);
        fodOgBilledeNode.attachChild(fod);
        fodOgBilledeNode.scale(0.5f);
        return fodOgBilledeNode;
    }

    @Override
    public void simpleUpdate(float tpf) {

        tidTilRyk = tidTilRyk - tpf;
        if (tidTilRyk < 0) {
            System.out.println("Tid til at rykke!");
            tidTilRyk = 0.5f;

            Spiller sp = spillere.get((int) (Math.random() * spillere.size()));
            int slag = 1 + (int) (6 * Math.random());

            sp.feltNr = (sp.feltNr + slag) % felter.size();
            sp.ryk.startRykTil(felter.get(sp.feltNr));
            if (slag==6) {
                sp.node.getControl(BrikRoterKontrol.class).start();
                infoTekst.setText(sp.navn + " slog en 6'er!");
            } else {
                infoTekst.setText(sp.navn + " rykker til felt "+sp.feltNr);                
            }
            if (slag >= 5) sp.ryk.støvNårDenLander = true;
        }
    }
}