/*
 * Copyright (C) 2006  Antonie Struyk
 *
 * This file is part of Freetar Hero.
 *
 *    Freetar Hero is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    Freetar Hero is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Freetar Hero; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.freetar.game;

import net.freetar.Song;
import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.effects.particles.ParticleFactory;
import com.jmex.effects.particles.ParticleGeometry;
import com.jmex.effects.particles.ParticleInfluence;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.SimpleParticleInfluenceFactory;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Anton
 */
public class PressEffectManager {
    
    private Node rootNode;
    
    private Node[] pressParticleNode;
    private Node[] holdParticleNode;
    
    /** Creates a new instance of EffectsManager */
    public PressEffectManager() {
        rootNode = new Node("PressEffectRootNode");
        
        rootNode.setLightCombineMode(TextureState.REPLACE);
        LightState ls = DisplaySystem.getDisplaySystem().getRenderer().createLightState();
        ls.setEnabled(false);
        rootNode.setRenderState(ls);
        
        pressParticleNode = new Node[Song.TRACKS];
        holdParticleNode = new Node[Song.TRACKS];
        
        for(int i = 0; i < Song.TRACKS; i ++){
            pressParticleNode[i] = new Node("PressParticleNode" + i);
            holdParticleNode[i] = new Node("HoldParticleNode" + i);
            
            rootNode.attachChild(pressParticleNode[i]);
            rootNode.attachChild(holdParticleNode[i]);
        }
        
        initNoteParticles();
        
        rootNode.updateGeometricState(0, true);
        rootNode.updateRenderState();
    }
    
    private void initNoteParticles(){
        //Load the AS and Texture for the particles
        
        ZBufferState zs = DisplaySystem.getDisplaySystem().getRenderer().createZBufferState();
        //zs.setEnabled(false);
        //zs.setFunction(ZBufferState.CF_ALWAYS);
        
        AlphaState as = DisplaySystem.getDisplaySystem().getRenderer().createAlphaState();
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        //as.setDstFunction(AlphaState.DB_ONE);
        as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        
        as.setTestEnabled(true);
        as.setTestFunction(AlphaState.TF_GREATER);
        
        
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setTexture(TextureManager.loadTexture(Skin.getInstance().getResource(
                "particles/whiteFlare.png"),
                Texture.FM_LINEAR, Texture.FM_LINEAR));
        
        ParticleInfluence gravity = SimpleParticleInfluenceFactory.createBasicGravity(new Vector3f(0,0,-0.01f), false);
        ParticleInfluence strongGravity = SimpleParticleInfluenceFactory.createBasicGravity(new Vector3f(0,0,-0.06f), false);
        //Load the particles
        for(int i = 0; i < Song.TRACKS; i++){
            final float trackCenter = i * Fretboard.TRACK_WIDTH + Fretboard.TRACK_WIDTH / 2f;
            final ColorRGBA startColor = Fretboard.trackColors[i];
            final ColorRGBA finalColor = new ColorRGBA(startColor.r, startColor.g, startColor.b, 0.0f);
            
            //Setup the press particle effects
            ParticleMesh pressParticles = ParticleFactory.buildParticles("pressParticles" + i, 20, ParticleGeometry.PT_QUAD);
            pressParticles.setEmissionDirection(new Vector3f(0,0,1));
            pressParticles.setMinimumAngle(FastMath.DEG_TO_RAD * 4.0f);
            pressParticles.setMaximumAngle(FastMath.DEG_TO_RAD * 40.0f);
            pressParticles.setInitialVelocity(0.002f);
            pressParticles.setStartColor(startColor);
            pressParticles.setEndColor(finalColor);
            pressParticles.setStartSize(Fretboard.TRACK_WIDTH / 4f);
            pressParticles.setEndSize(Fretboard.TRACK_WIDTH / 5f);
            pressParticles.setMinimumLifeTime(200f);
            pressParticles.setMaximumLifeTime(300f);
            pressParticles.setRepeatType(Controller.RT_CLAMP);
            pressParticles.addInfluence(gravity);
            pressParticleNode[i].attachChild(pressParticles);
            
            ParticleMesh holdParticles = ParticleFactory.buildParticles("holdParticles" + i, 20, ParticleGeometry.PT_QUAD);
            holdParticles.setEmissionDirection(new Vector3f(0,0,1));
            holdParticles.setMinimumAngle(FastMath.DEG_TO_RAD * 4.0f);
            holdParticles.setMaximumAngle(FastMath.DEG_TO_RAD * 40.0f);
            holdParticles.setInitialVelocity(0.004f);
            holdParticles.setStartColor(startColor);
            holdParticles.setEndColor(finalColor);
            holdParticles.setStartSize(Fretboard.TRACK_WIDTH / 5f);
            holdParticles.setEndSize(Fretboard.TRACK_WIDTH / 4f);
            holdParticles.setMinimumLifeTime(250f);
            holdParticles.setMaximumLifeTime(300f);
            holdParticles.setRepeatType(Controller.RT_WRAP);
            holdParticles.addInfluence(strongGravity);
            holdParticles.setControlFlow(true);
            holdParticles.setReleaseRate(300);
            
            
            holdParticles.getParticleController().setActive(false);
            holdParticles.setCullMode(Spatial.CULL_ALWAYS);
            
            holdParticleNode[i].attachChild(holdParticles);
            
            //Set the AlphaState and Textures for the hold and press Particles
            holdParticleNode[i].setRenderState(as);
            holdParticleNode[i].setRenderState(zs);
            holdParticleNode[i].setRenderState(ts);
            
            pressParticleNode[i].setRenderState(as);
            pressParticleNode[i].setRenderState(zs);
            pressParticleNode[i].setRenderState(ts);
            
            //Move to the correct location and scale
            pressParticleNode[i].setLocalTranslation(new Vector3f(trackCenter, 0, 0));
            holdParticleNode[i].setLocalTranslation(new Vector3f(trackCenter, 0, 0));
        }
    }
    
    public void update(float timePerFrame){
        for(int i = 0; i < pressParticleNode.length; i++){
            //Update pressParticleNodes
            for(int ii = 0; ii < pressParticleNode[i].getQuantity(); ii++){
                ParticleGeometry geom = (ParticleGeometry) pressParticleNode[i].getChild(ii);
                if(geom.getParticleController().isActive()){
                    geom.getParticleController().update(timePerFrame);
                }
            }
            
            //Update holdParticleNodes
            for(int ii = 0; ii < holdParticleNode[i].getQuantity(); ii++){
                ParticleGeometry geom = (ParticleGeometry) holdParticleNode[i].getChild(ii);
                if(geom.getParticleController().isActive()){
                    geom.getParticleController().update(timePerFrame);
                }
            }
        }
    }
    
    public Node getRootNode(){
        return rootNode;
    }
    
    public void notePressed(int trackNumber){
        for(int i = 0; i < pressParticleNode[trackNumber].getQuantity(); i++){
            ParticleGeometry geom = (ParticleGeometry) pressParticleNode[trackNumber].getChild(i);
            geom.forceRespawn();
        }
    }
    
    public void holdingNote(int trackNumber){
        for(int i = 0; i < holdParticleNode[trackNumber].getQuantity(); i++){
            ParticleGeometry geom = (ParticleGeometry) holdParticleNode[trackNumber].getChild(i);
            geom.forceRespawn();
            geom.getParticleController().setActive(true);
            geom.setCullMode(Spatial.CULL_DYNAMIC);
        }
    }
    
    public void releaseNote(int trackNumber){
        for(int i = 0; i < holdParticleNode[trackNumber].getQuantity(); i++){
            ParticleGeometry geom = (ParticleGeometry) holdParticleNode[trackNumber].getChild(i);
            geom.getParticleController().setActive(false);
            geom.setCullMode(Spatial.CULL_ALWAYS);
        }
    }
}
