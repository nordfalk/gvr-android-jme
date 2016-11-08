/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.android.nepal1;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.AndroidLocator;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author rudz
 */
public class TestNepal extends SimpleApplication {

    private ArrayList<Spatial> felter = new ArrayList<>();
    private ArrayList<Spiller> spillere = new ArrayList<>();
    private Material fodMat;
    float tidTilRyk = 1;
    float interpolation = 1;

    public static void main(String... argv) {
        TestNepal app = new TestNepal();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void setSettings(AppSettings settings) {
        settings.setAudioRenderer(AppSettings.ANDROID_MEDIAPLAYER);
        super.setSettings(settings);
    }

    @Override
    public void simpleInitApp() {
        Texture manoj = assetManager.loadTexture("Textures/klippet-manoj.png");

        Node laxmiBrik = lavBrik(assetManager.loadTexture("Textures/klippet-laxmi.png"));
        Node abishakBrik = lavBrik(assetManager.loadTexture("Textures/klippet-abishak.png"));
        Node bishalBrik = lavBrik(assetManager.loadTexture("Textures/klippet-bishal.png"));
        abishakBrik.rotate(0, 10, 0).scale(0.6f);
        bishalBrik.rotate(0, 10, 0).scale(0.6f);;
        abishakBrik.getLocalTranslation().x += 2;
        bishalBrik.getLocalTranslation().x -= 3;

        spillere.addAll(Arrays.asList(
                new Spiller(laxmiBrik, "Laxmi"),
                new Spiller(abishakBrik, "Abishak"),
                new Spiller(bishalBrik, "Bishal")));

        // Workaround for missing texture because of wrong path in the .j3o files created by the JME3 scene editor
        // See https://github.com/jMonkeyEngine/jmonkeyengine/issues/352
        assetManager.unregisterLocator("/", AndroidLocator.class);
        assetManager.registerLocator("",AndroidLocator.class);
        rootNode.attachChild(assetManager.loadModel("Scenes/spilScene.j3o"));

        for (int i=1; ; i++) {
            Spatial felt = rootNode.getChild("Felt"+i);
            System.out.println("felt= "+ felt);
            if (felt==null) break;
            felt.setUserData("nummer", i);
            felter.add(felt);
        }
        System.out.println("felter= "+ felter);

        rootNode.attachChild(laxmiBrik);
        rootNode.attachChild(abishakBrik);
        rootNode.attachChild(bishalBrik);

        // Culling is too aggressive in GVR - so disable it for now
        rootNode.setCullHint(Spatial.CullHint.Never);


        // Ryk kameraet op og til siden
        cam.setLocation( cam.getLocation().add(2, 3, -3));
        cam.lookAt(new Vector3f(), new Vector3f(0, 1, 0)); // peg det ind på spillepladen

    }

    private Node lavBrik(Texture billede) {
        fodMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
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

        Node fodOgBilledeNode = new Node();
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

            sp.rykFra = felter.get(sp.feltNr).getLocalTransform();
            sp.feltNr = (sp.feltNr + slag) % felter.size();
            sp.rykTil = felter.get(sp.feltNr).getLocalTransform().clone(); // Variér position lidt
            sp.rykTil.getTranslation().addLocal(FastMath.rand.nextFloat() / 5 - 0.1f, 0, FastMath.rand.nextFloat() / 5 - 0.1f);
            Spatial felt = felter.get(sp.feltNr);

            sp.node.setLocalTranslation(felt.getLocalTranslation());
            sp.node.setLocalRotation(felt.getLocalRotation());
            interpolation = 0;
        }

        if (interpolation == 1) return;
        interpolation += tpf * 3;
        if (interpolation > 1) {
            interpolation = 1;
        }
        float inter = interpolation;
        inter = (inter * inter);
        //inter = (inter*inter*inter + 1-(1-inter)*(1-inter)*(1-inter))/2;
        //System.out.printf("interpolation=%.2f  inter=%.2f\n", interpolation, inter);
        for (Spiller sp : spillere) {
            if (sp.rykFra == sp.rykTil) continue;
            Transform spt = sp.node.getLocalTransform();
            //spt.interpolateTransforms(sp.rykFra, sp.rykTil, interpolation); // ryk uden at hoppe
            spt.getRotation().slerp(sp.rykFra.getRotation(), sp.rykTil.getRotation(), inter);
            Vector3f fra = sp.rykFra.getTranslation();
            Vector3f til = sp.rykTil.getTranslation();
            Vector3f midt = fra.clone().interpolateLocal(til, 0.5f).add(0, 1, 0);
            FastMath.interpolateBezier(inter, fra, midt, midt, til, spt.getTranslation());
            sp.node.setLocalTransform(spt);
            if (interpolation == 1) {
                sp.rykFra = sp.rykTil;
            }
        }
    }
}