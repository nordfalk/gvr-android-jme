/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.android.nepal1;


import android.util.Log;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Nehon
 */
public class TestNepal extends SimpleApplication {

    ArrayList<Spatial> felter = new ArrayList<>();
    ArrayList<Spiller> spillere = new ArrayList<>();

    public static void main(String... argv) {
        TestNepal app = new TestNepal();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void setSettings(AppSettings settings) {
        final String old = settings.getAudioRenderer();
        settings.setAudioRenderer(AppSettings.ANDROID_MEDIAPLAYER);
        for (int i = 0; i < 10000; i++) {
            Log.v("AAAAAAAAAAARGH", old + " --> " + settings.getAudioRenderer());
        }
        super.setSettings(settings);
    }

    @Override
    public void simpleInitApp() {

        Node laxmiBrik = lavBrik(assetManager.loadTexture("Textures/klippet-laxmi.png"));
        Node abishakBrik = lavBrik(assetManager.loadTexture("Textures/klippet-abishak.png"));
        Node bishalBrik = lavBrik(assetManager.loadTexture("Textures/klippet-bishal.png"));
        abishakBrik.rotate(0, 10, 0).scale(0.6f);
        bishalBrik.rotate(0, 10, 0).scale(0.6f);
        abishakBrik.getLocalTranslation().x += 2;
        bishalBrik.getLocalTranslation().x -= 3;

        spillere.addAll(Arrays.asList(
                new Spiller(laxmiBrik, "Laxmi"),
                new Spiller(abishakBrik, "Abishak"),
                new Spiller(bishalBrik, "Bishal")));

        rootNode.attachChild(assetManager.loadModel("Scenes/spilScene.j3o"));

        //Spatial f1 = rootNode.getChild("Felt1");
        //abishakBrik.setLocalTranslation(f1.getWorldTranslation());
        for (int i = 1; ; i++) {
            Spatial felt = rootNode.getChild("Felt" + i);
            System.out.println("felt= " + felt);
            if (felt == null) {
                break;
            }
            felt.setUserData("nummer", i);
            felter.add(felt);
        }
        System.out.println("felter= " + felter);

        rootNode.attachChild(laxmiBrik);
        rootNode.attachChild(abishakBrik);
        rootNode.attachChild(bishalBrik);

        // Ryk kameraet op og til siden
        cam.setLocation(cam.getLocation().add(2, 10, -20));
        cam.lookAt(new Vector3f(), new Vector3f(0, 1, 0)); // peg det ind på spillepladen
    }

    private Node lavBrik(Texture billede) {
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

        Node fodOgBilledeNode = new Node();
        fodOgBilledeNode.attachChild(billedeNode);
        fodOgBilledeNode.attachChild(fod);
        //fodOgBilledeNode.scale(5f);
        fodOgBilledeNode.getLocalTranslation().z += 2.5f;
        return fodOgBilledeNode;
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        //if (this.timer.getTime() % 10 == 0) {
        Spiller sp = spillere.get((int) (Math.random() * spillere.size()));
        sp.feltNr = (sp.feltNr + 1) % felter.size();
        Spatial felt;
        if (sp.feltNr == 0) {
            System.out.println("Hurra spilleren er f\u00E6rdig! " + sp.navn);
            felt = rootNode.getChild("M\u00E5lfelt");
        } else {
            felt = felter.get(sp.feltNr);
            System.out.println("Rykker " + sp.navn + " til " + felt);
        }
        sp.brik.setLocalTranslation(felt.getLocalTranslation());
        sp.brik.setLocalRotation(felt.getLocalRotation());
        //}
    }
}