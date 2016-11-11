/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nordfalk.nepalspil.kontrol;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author j
 */
public class BrikStøvKontrol extends AbstractControl {
    float tid = 1;
    private final ParticleEmitter effekt;

    public BrikStøvKontrol(AssetManager assetManager, Node rootNode) {
        effekt = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", assetManager.loadTexture("Textures/Smoke.png"));
        effekt.setMaterial(fireMat);
        effekt.setImagesX(15); effekt.setImagesY(1); // 15x1 texture animation
        effekt.setStartColor( ColorRGBA.Brown);
        effekt.setEndColor( ColorRGBA.Black );
        effekt.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 5, 0));
        effekt.setStartSize(0.6f);
        effekt.setEndSize(0.1f);
        effekt.setGravity(0f,10f,0f);
        effekt.setLowLife(0.4f);
        effekt.setHighLife(0.5f);
        effekt.getParticleInfluencer().setVelocityVariation(0.5f);
        effekt.setParticlesPerSec(0);
        rootNode.attachChild(effekt);
        effekt.setEnabled(false);
    }

    @Override
    protected void controlUpdate(float tpf) {        
        tid += tpf;
        if (tid>1) {
            setEnabled(false);
            effekt.setEnabled(false);
        }
    }
    
    public void start() {
        tid = 0;        
        setEnabled(true);
        effekt.setEnabled(true);
        effekt.emitAllParticles();
    }


    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }    
}
