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

import net.freetar.Note;
import net.freetar.Song;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.intersection.CollisionResults;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.geom.Debugger;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Anton
 */
public class Fretboard{
    private static final String BACKGROUND_NAME = "backgroundTexture.png";
    public static final float TRACK_WIDTH = 0.2f;
    
    public static final ColorRGBA[] trackColors = {
        ColorRGBA.green,
        ColorRGBA.red,
        ColorRGBA.yellow,
        ColorRGBA.blue,
        ColorRGBA.orange
    };
    
    public static final MaterialState[] trackMaterials = new MaterialState[trackColors.length];
    
    
    private Node rootNode;
    private Node scrollingNode;
    private Song song;
    
    private Map<Note, Node> noteToNodeMap;
    private float scrollSpeed;
    
    private FingeringBoard fingeringBoard;
    
    /** Creates a new instance of Fretboard */
    public Fretboard(Song song){
        this(song, 1.0f);
    }
    
    public Fretboard(Song song, float scrollSpeed) {
        this.song = song;
        this.rootNode = new Node("FretboardRootNode");
        this.scrollingNode = new Node("BackgroundRootNode");
        this.scrollSpeed = scrollSpeed;
        
        noteToNodeMap = new HashMap<Note, Node>();
        
        fingeringBoard = new FingeringBoard();
        
        rootNode.attachChild(scrollingNode);
        rootNode.attachChild(fingeringBoard.getRootNode());
        rootNode.updateRenderState();
        
        setupTrackMaterials();
        setupBackground(BACKGROUND_NAME);
        
        rootNode.updateGeometricState(0, true);
        rootNode.updateRenderState();
    }
    
    private void setupTrackMaterials(){
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        for(int i = 0; i < trackColors.length; i++){
            MaterialState ms = renderer.createMaterialState();
            ms.setAmbient(trackColors[i]);
            ms.setSpecular(ColorRGBA.white);
            trackMaterials[i] = ms;
        }
    }
    
    private void setupBackground(String textureFileName){
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();

        //Load the texture for the background
        TextureState ts = renderer.createTextureState();
        URL bgLocation = Skin.getInstance().getResource(textureFileName);
        //TODO do some error checking for the URL location (throw fileNotFoundException I guess)
        Texture bgTexture = TextureManager.loadTexture(bgLocation, Texture.MM_LINEAR, Texture.FM_LINEAR);
        bgTexture.setWrap(Texture.WM_WRAP_S_WRAP_T);
        ts.setTexture(bgTexture);
        
        AlphaState as = renderer.createAlphaState();
        as.setEnabled(true);
        as.setBlendEnabled(true);
        as.setSrcFunction(AlphaState.SB_SRC_ALPHA);
        as.setDstFunction(AlphaState.DB_ONE_MINUS_SRC_ALPHA);
        as.setTestEnabled(true);
        as.setTestFunction(AlphaState.TF_GREATER);
        
        //Material state TODO necessary?
        MaterialState ms = renderer.createMaterialState();
        ms.setAmbient(new ColorRGBA(1,1,1,1));
        
        //Create the quad for the background
        final float totalLength = song.getProperties().getLength() * scrollSpeed;
        final float totalWidth = Song.TRACKS * TRACK_WIDTH;
        final float texHeight = (float) bgTexture.getImage().getHeight();
        final float texWidth = (float) bgTexture.getImage().getWidth();
        final float QUAD_HEIGHT = texHeight * totalWidth / texWidth;
        int requiredToSpanSong = (int) (totalLength / QUAD_HEIGHT) + 1; //+1 to fix for dropping decimals
        for(int i = 0;i < requiredToSpanSong; i++){
            Quad backgroundQuad = new Quad("BackgroundQuad", totalWidth, QUAD_HEIGHT);
            backgroundQuad.setLocalTranslation(new Vector3f(
                    totalWidth / 2,
                    i * QUAD_HEIGHT + QUAD_HEIGHT / 2,
                    0));
            backgroundQuad.setModelBound(new BoundingBox());
            backgroundQuad.updateModelBound();
            backgroundQuad.setRenderState(ts);
            backgroundQuad.setRenderState(ms);
            backgroundQuad.setRenderState(as);
            backgroundQuad.updateRenderState();
            scrollingNode.attachChild(backgroundQuad);
        }
    }
    
    private Node getCompleteNodeFor(Note n){
        Node returnNode = new Node("Node For Note: " + n);
        Node modelNode = getModelNodeFor(n);
        Geometry holdBar = getHoldBarFor(n);
        if(holdBar != null){
            returnNode.attachChild(holdBar);
        }
        returnNode.attachChild(modelNode);
        return returnNode;
    }
    
    private Node getModelNodeFor(Note n){
        Node modelNode = ModelManager.getModelManager().createModelNodeFor(n);
        modelNode.setLocalTranslation(
                new Vector3f(n.getButtonNumber() * TRACK_WIDTH + TRACK_WIDTH / 2f,
                n.getTime() * scrollSpeed,
                0));
        return modelNode;
    }
    
    public void update(float timePerFrame){
        //TODO animate something?
    }
    
    private Geometry getHoldBarFor(Note n){
        //Create the hold-bar if necessary
        if(n.getDuration() > 0){
            final float height = n.getDuration() * scrollSpeed;
            /*Quad holdBar = new Quad("HoldQuad",
                    ModelManager.MODEL_SIZE / 4,
                    height);
             */
            Cylinder holdBar = new Cylinder(
                    "HoldCylinder" + n.getDuration(),
                    4,
                    9,
                    ModelManager.MODEL_SIZE / 4,
                    height);
            
            //Rotate the holdbar into position
            Quaternion q = new Quaternion();
            q.fromAngleAxis(FastMath.PI / 2, new Vector3f(1,0,0));
            holdBar.setLocalRotation(q);
            
            //Move the holdBar into the right spot
            holdBar.setLocalTranslation(new Vector3f(n.getButtonNumber() * TRACK_WIDTH + TRACK_WIDTH / 2f,
                    n.getTime() * scrollSpeed + height / 2,
                    0));
            
            holdBar.setModelBound(new BoundingBox());
            holdBar.updateModelBound();
            
            holdBar.setRenderState(trackMaterials[n.getButtonNumber()]);
            holdBar.setRenderState(scrollingNode.getRenderState(RenderState.RS_LIGHT));
            
            holdBar.setTextureCombineMode(TextureState.OFF);
            holdBar.updateRenderState();
            return holdBar;
        }
        return null;
    }
    
    public void addNote(Note n){
        if(noteToNodeMap.containsKey(n)){
            //If the note is already in this song, simply re-show it
            noteToNodeMap.get(n).setCullMode(Spatial.CULL_DYNAMIC);
        }else{
            Node noteNode = getCompleteNodeFor(n);
            noteToNodeMap.put(n, noteNode);
            scrollingNode.attachChild(noteNode);
        }
    }
    
    public void removeNote(Note n){
        if(noteToNodeMap.containsKey(n)){
            //Hide the node (takes more time to re-structure the entire scene graph)
            Node noteNode = noteToNodeMap.get(n);
            noteNode.setCullMode(Spatial.CULL_ALWAYS);
            noteNode.updateRenderState();
        }
    }
    
    public void updateBackgroundPosition(float songTime){
        scrollingNode.setLocalTranslation(new Vector3f(0, -songTime * scrollSpeed, 0));
    }
    
    public void cleanup(){
        //Remove all notes
        Set<Note> notes = noteToNodeMap.keySet();
        for(Note n : notes){
            removeNote(n);
        }
        notes = null;
        
        //Detatch this from the rootNode
        scrollingNode.detachAllChildren();
        scrollingNode = null;
    }
    
    public FingeringBoard getFingeringBoard(){
        return fingeringBoard;
    }
    
    public Node getRootNode(){
        return rootNode;
    }
}
