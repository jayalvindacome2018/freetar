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
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import java.net.URL;

/**
 *
 * @author Anton
 */
public class FingeringBoard {
    private static final String ACTIVE_NAMES[] = {
        "activeTrack0.png",
        "activeTrack1.png",
        "activeTrack2.png",
        "activeTrack3.png",
        "activeTrack4.png"
    };
    
    private static final String INACTIVE_NAMES[] = {
        "inactiveTrack0.png",
        "inactiveTrack1.png",
        "inactiveTrack2.png",
        "inactiveTrack3.png",
        "inactiveTrack4.png"
    };
    
    private Node rootNode;
    
    private Node[] activeNodes;
    private Node[] inactiveNodes;
    
    /** Creates a new instance of FingeringBoard */
    public FingeringBoard() {
        this.rootNode = new Node("FingeringBoardRootNode");
        
        activeNodes = new Node[Song.TRACKS];
        inactiveNodes = new Node[Song.TRACKS];
        
        init();
        
        rootNode.updateRenderState();
        rootNode.updateGeometricState(0, true);
    }
    
    public void init(){
        final Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        final float size = ModelManager.MODEL_SIZE;
        final float trackWidth = Fretboard.TRACK_WIDTH;
        
        rootNode.setLightCombineMode(TextureState.REPLACE);
        
        LightState ls = DisplaySystem.getDisplaySystem().getRenderer().createLightState();
        ls.setEnabled(false);
        rootNode.setRenderState(ls);
        
        for(int i = 0; i < activeNodes.length; i++){
            //Create the nodes necessary
            activeNodes[i] = new Node("Track " + i + " indicator (active)");
            inactiveNodes[i] = new Node("Track " + i + " indicator (inactive)");
            
            //Create the Quads at the appropriate places
            Vector3f translation = new Vector3f(i * trackWidth + trackWidth / 2f, 0, 0.0001f);
            Quad activeQuad = new Quad("ActiveQuad " + i, size, size);
            Quad inactiveQuad = new Quad("InactiveQuad " + i, size, size);
            activeQuad.setLocalTranslation(translation);
            inactiveQuad.setLocalTranslation(translation);
            
            //Set the texture states for the Quads
            TextureState activeTS = renderer.createTextureState();
            TextureState inactiveTS = renderer.createTextureState();
            
            AlphaState as = renderer.createAlphaState();
            as.setEnabled(true);
            as.setBlendEnabled(true);
            as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
            as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
            as.setTestEnabled(true);
            as.setTestFunction(AlphaState.TF_GREATER);
            
            final URL activeLocation = Skin.getInstance().getResource(ACTIVE_NAMES[i]);
            final URL inactiveLocation = Skin.getInstance().getResource(INACTIVE_NAMES[i]);
            
            Texture activeTexture = TextureManager.loadTexture(activeLocation,
                    Texture.MM_LINEAR,
                    Texture.FM_LINEAR,
                    com.jme.image.Image.GUESS_FORMAT_NO_S3TC,
                    1f,
                    true);
            Texture inactiveTexture = TextureManager.loadTexture(inactiveLocation,
                    Texture.MM_LINEAR,
                    Texture.FM_LINEAR,
                    com.jme.image.Image.GUESS_FORMAT_NO_S3TC,
                    1f,
                    true);
            
            activeTS.setTexture(activeTexture);
            inactiveTS.setTexture(inactiveTexture);
            
            activeQuad.setRenderState(activeTS);
            activeQuad.setRenderState(as);
            inactiveQuad.setRenderState(inactiveTS);
            inactiveQuad.setRenderState(as);
            
            activeNodes[i].attachChild(activeQuad);
            inactiveNodes[i].attachChild(inactiveQuad);
            
            //Update the renderStates
            activeNodes[i].updateRenderState();
            inactiveNodes[i].updateRenderState();
        }
        
        for(int i = 0; i < inactiveNodes.length; i++){
            rootNode.attachChild(inactiveNodes[i]);
            rootNode.attachChild(activeNodes[i]);
            deactivateButton(i);
        }
    }
    
    public Node getRootNode(){
        return rootNode;
    }
    
    public void activateButton(int buttonNumber){
        inactiveNodes[buttonNumber].setCullMode(Spatial.CULL_ALWAYS);
        activeNodes[buttonNumber].setCullMode(Spatial.CULL_DYNAMIC);
        inactiveNodes[buttonNumber].updateRenderState();
        activeNodes[buttonNumber].updateRenderState();
    }
    
    public void deactivateButton(int buttonNumber){
        inactiveNodes[buttonNumber].setCullMode(Spatial.CULL_DYNAMIC);
        activeNodes[buttonNumber].setCullMode(Spatial.CULL_ALWAYS);
        inactiveNodes[buttonNumber].updateRenderState();
        activeNodes[buttonNumber].updateRenderState();
    }
}
