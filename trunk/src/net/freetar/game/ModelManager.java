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

import com.jme.util.export.binary.BinaryImporter;
import com.jmex.model.XMLparser.Converters.ObjToJme;
import com.jmex.model.XMLparser.JmeBinaryReader;
import com.jmex.model.XMLparser.XMLtoBinary;
import java.util.logging.Logger;
import net.freetar.Note;
import net.freetar.Song;
import net.freetar.util.DebugHandler;
import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.SharedNode;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Anton
 */
public class ModelManager {
    private static ModelManager INSTANCE = null;
    public static final float MODEL_SIZE = 0.2f;
    
    private static final Logger logger = DebugHandler.getLogger("net.freetar.game.ModelManager");
    
    private Node[] baseGeometry;
    private Node[] hammerGeometry;
    
    /** Creates a new instance of ModelManager */
    private ModelManager() {
        logger.info("Starting ModelManager Constructor");
        baseGeometry = new Node[Song.TRACKS];
        hammerGeometry = new Node[Song.TRACKS];
        
        logger.info("Loading 'normal' Note Models");
        //Load the base geometry nodes
        for(int i = 0; i < Song.TRACKS; i++){
            URL modelURL = Skin.getInstance().getResource("models/Track" + i + "Button.obj");
            Node modelNode = loadBlenderModel(modelURL);
            modelNode.setLocalScale(MODEL_SIZE);
            baseGeometry[i] = modelNode;
        }
        
        logger.info("Loading 'Hammeron' Note Models");
        //Load the geometry for the Hammer-ons and pull-offs
        for(int i = 0; i < Song.TRACKS; i++){
            URL modelURL = Skin.getInstance().getResource("models/Track"+i+"HammerButton.obj");
            Node modelNode = null;
            if(modelURL != null){
                modelNode = loadBlenderModel(modelURL);
                modelNode.setLocalScale(MODEL_SIZE);
            }else{
                DebugHandler.getLogger("com.astruyk.freetar.game.ModelManager").warning("No Hammer-On Model Found for track " + i + " using default geometry.");
                modelNode = baseGeometry[i];
            }
            hammerGeometry[i] = modelNode;
        }
    }
    
    public static ModelManager getModelManager(){
        if(INSTANCE != null){
            return INSTANCE;
        }
        
        logger.info("ModelManager instance is NULL, creating new ModelManager");
        INSTANCE = new ModelManager();
        return INSTANCE;
    }
    
    public Node createModelNodeFor(Note n){
        Node modelNode = null;
        if(!n.isHammerOnAllowed()){
            //Get the geometry for the button press indicator
            modelNode = new SharedNode("Model", baseGeometry[n.getButtonNumber()]);
        }else{
            //Get the geometry for a hammer-on node
            modelNode = new SharedNode("Model", hammerGeometry[n.getButtonNumber()]);
        }
        modelNode.updateRenderState();
        return modelNode;
    }
    
    public Node loadBlenderModel(URL modelURL){
        Node returnNode = loadOBJModel(modelURL);
        return returnNode;
    }
    
    public Node loadOBJModel(URL modelURL){
        logger.info("Loading OBJ format model");
        
        ObjToJme converter = new ObjToJme();
        converter.setProperty("mtllib",modelURL);
        ByteArrayOutputStream BO = new ByteArrayOutputStream();
        JmeBinaryReader jbr = new JmeBinaryReader();
        Node modelNode = null;
        try {
            converter.convert(modelURL.openStream(), BO);
            jbr.setProperty("bound","box");
            BinaryImporter importer = new BinaryImporter();
            modelNode = (Node) importer.load(new ByteArrayInputStream(BO.toByteArray()));
            modelNode.setModelBound(new BoundingBox());
            modelNode.updateModelBound();
        } catch (IOException ex) {   // Just in case anything happens
            logger.warning("Error loading OBJ model: " + ex.getMessage());
            ex.printStackTrace();
        }
        logger.info("Done Loading OBJ format model");
        return modelNode;
        
    }
    
    public Node loadXMLModel(URL modelURL){
        XMLtoBinary converter=new XMLtoBinary();
        ByteArrayOutputStream BO=new ByteArrayOutputStream();
        JmeBinaryReader jbr=new JmeBinaryReader();
        Node modelNode = null;
        try {
            converter.sendXMLtoBinary(modelURL.openStream(), BO);
            jbr.setProperty("bound","box");
            modelNode = jbr.loadBinaryFormat(new ByteArrayInputStream(BO.toByteArray()));
        } catch (IOException e) {   // Just in case anything happens
            e.printStackTrace();
        }
        return modelNode;
    }
}
