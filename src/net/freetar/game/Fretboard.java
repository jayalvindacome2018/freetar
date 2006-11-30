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

import com.jme.util.geom.BufferUtils;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
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
import net.freetar.util.DebugHandler;

/**
 *
 * @author Anton
 */
public class Fretboard{
    private static final String BACKGROUND_NAME = "backgroundTexture.png";
    private static final Logger logger = DebugHandler.getLogger("net.freetar.game.Fretboard");
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
    
    private boolean isLefty;
    
    private FingeringBoard fingeringBoard;
    
    /** Creates a new instance of Fretboard */
    public Fretboard(Song song){
        this(song, 1.0f,false);
    }
    
    public Fretboard(Song song, float scrollSpeed){
        this(song,1.0f,false);
    }
    
    public Fretboard(Song song, float scrollSpeed, boolean lefty) {
        this.song = song;
        this.rootNode = new Node("FretboardRootNode");
        this.scrollingNode = new Node("BackgroundRootNode");
        this.scrollSpeed = scrollSpeed;
        this.isLefty = lefty;
        
        noteToNodeMap = new HashMap<Note, Node>();
        
        fingeringBoard = new FingeringBoard();
        
        rootNode.attachChild(scrollingNode);
        rootNode.attachChild(fingeringBoard.getRootNode());
        rootNode.updateRenderState();
        if(isLefty) {
            rootNode.setLocalScale(new Vector3f(-1,1,1));
            rootNode.setLocalTranslation(new Vector3f(1,0,0));
        }
        
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
        BufferedImage textureImage = null;
        try {
            textureImage = ImageIO.read(bgLocation);
        } catch (IOException ex) {
            DebugHandler.getInstance().logException(logger, ex);
            //TODO do something else here? Exit or something?
        }
        //TODO check for null textureImage
        
        //Calculate some values
        final float imgHeight = (float) textureImage.getHeight();               //The height of the background image
        final float imgWidth = (float) textureImage.getWidth();                 //The width of the background image
        final float texSize = (imgHeight > imgWidth) ? FastMath.nearestPowerOfTwo((int) imgHeight) : FastMath.nearestPowerOfTwo((int) imgWidth);    //The smallest power-of-two texture that can contain the background
        final float texWidth = imgWidth / texSize;                              //The width (in OpenGL coords) of the image
        final float texHeight = imgHeight / texSize;                            //The height (in OpenGL coords) of the image
        
        //Create an Image large enough to store the loaded texture as a power-of-two
        BufferedImage resizedTexture = new BufferedImage((int) texSize, (int) texSize, BufferedImage.TYPE_INT_ARGB);
        
        //Draw the textureImage onto the resizedTexture
        Graphics2D g = (Graphics2D)resizedTexture.getGraphics();

        g.drawImage(textureImage, 0, 0, null);
        Texture bgTexture = TextureManager.loadTexture(resizedTexture, Texture.MM_LINEAR, Texture.FM_LINEAR, false);

        //Set the texture coordinates so the fretboard will have only the data from textureImage
        //the area between (0,0) & (texWidth, texHeight)
        FloatBuffer texCoords = BufferUtils.createVector2Buffer(4);
        texCoords.put(0).put(0);
        texCoords.put(0).put(texHeight);
        texCoords.put(texWidth).put(texHeight);
        texCoords.put(texWidth).put(0);
        
        bgTexture.setWrap(Texture.WM_WRAP_S_WRAP_T);
        
        if(isLefty) {
            bgTexture.setScale(new Vector3f(-1,1,1));
        }
        
        
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
        final float QUAD_HEIGHT = imgHeight * totalWidth / imgWidth;
        int requiredToSpanSong = (int) (totalLength / QUAD_HEIGHT) + 1; //+1 to fix for dropping decimals
        for(int i = 0;i < requiredToSpanSong; i++){
            Quad backgroundQuad = new Quad("BackgroundQuad", totalWidth, QUAD_HEIGHT);
            backgroundQuad.setLocalTranslation(new Vector3f(
                    totalWidth / 2,
                    i * QUAD_HEIGHT + QUAD_HEIGHT / 2,
                    0));
            
            //Assign our calculate texture coords
            backgroundQuad.setTextureBuffer(0, texCoords);
            
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
