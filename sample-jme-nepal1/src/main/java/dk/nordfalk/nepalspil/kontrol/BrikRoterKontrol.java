/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.nordfalk.nepalspil.kontrol;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author j
 */
public class BrikRoterKontrol extends AbstractControl {
    float rotTid = 1;

    @Override
    protected void controlUpdate(float tpf) {
        rotTid += tpf;
        if (rotTid>1) {
            setEnabled(false);
            spatial.rotate(0, 0, 0);            
        } else {
            spatial.rotate(0, 10*tpf, 0);            
        }
    }

    public void start() {
        rotTid = 0;
        setEnabled(true);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }    
}
