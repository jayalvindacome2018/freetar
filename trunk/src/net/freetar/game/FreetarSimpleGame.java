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

import com.jme.app.SimpleGame;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.light.DirectionalLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.FogState;
import com.jme.scene.state.LightState;
import com.jme.system.DisplaySystem;
import com.jmex.game.state.StandardGameState;
import java.util.logging.Logger;
import net.freetar.Note;
import net.freetar.NoteChangeListener;
import net.freetar.NoteEvent;
import net.freetar.Song;
import net.freetar.bgmusic.BackgroundMusic;
import net.freetar.input.ButtonConfig;
import net.freetar.input.ButtonEvent;
import net.freetar.input.ControllerNotSupportedException;
import net.freetar.input.GamepadButtonListener;
import net.freetar.input.GamepadPoller;
import net.freetar.noteStates.HammeredState;
import net.freetar.noteStates.NoteHoldState;
import net.freetar.noteStates.NoteMissedState;
import net.freetar.noteStates.NotePlayedState;
import net.freetar.noteStates.NoteState;
import net.freetar.noteStates.PrematureReleaseState;
import net.freetar.noteStates.PressableState;
import net.freetar.util.DebugHandler;

/**
 *
 * @author Anton
 */
public class FreetarSimpleGame extends SimpleGame
        implements GamepadButtonListener, NoteChangeListener{
    
    public static final float TRACK_WIDTH = 0.2f;
    public static final float SPEED = 10.0f;
    public static final float VISUAL_DISTANCE = 4.0f;
    public static final float FADE_OUT_DISTANCE = 1.0f;
    public static final boolean ENABLE_DEBUG_FEATURES = false;
    
    private static final Logger logger = DebugHandler.getLogger("net.freetar.game.FreetarGame");
    
    private BackgroundMusic music;
    private Song song;
    private ButtonConfig buttonConfig;
    private Score score;
    private GamepadPoller poller;
    private InputHandler inputHandler;
    
    private Node staticBackgroundNode;
    private Node hudNode;
    private GameHUD scoreHUD;
    private LightState lightState;
    
    private Fretboard fretboard;
    private PressEffectManager pressEffectManager;
    
    //TODO find a better way to keep track for hammer ons and pull-offs (more)
    //lastHitNote keeps track of the last note played. It is set ONLY IF the last note
    //was played successfully. Hitting an incorrect note clears it. Used to test for
    //hammer-ons and pull-offs. Very unclean.
    private ButtonHandler buttonHandler;
    
    /** Creates a new instance of PlayingGameState */
    //TODO pass in ALL instance stuff - loading should be 100% done at this point...
    public FreetarSimpleGame(String name,
            BackgroundMusic music,
            Song song,
            ButtonConfig buttonConfig) {
        logger.info("Starting FreetarSimpleGame constructor");
        //Setup instance variables
        this.music = music;
        this.song = song;
        this.buttonConfig = buttonConfig;
        this.score = new Score();
        logger.info("Ending FreetarSimpleGame constructor");
    }
    
    public void simpleInitGame() {
        logger.info("Begining Intialization of game resources");
        
        if(!ENABLE_DEBUG_FEATURES){
            KeyBindingManager kbm = KeyBindingManager.getKeyBindingManager();
            kbm.remove("toggle_pause");
            kbm.remove("toggle_wire");
            kbm.remove("toggle_lights");
            kbm.remove("toggle_bounds");
            kbm.remove("toggle_normals");
            kbm.remove("camera_out");
            kbm.remove("screen_shot");
            kbm.remove("parallel_projection");
            kbm.remove("toggle_depth");
            kbm.remove("mem_report");
            
            input.setEnabled(false);
            
            fpsNode.setCullMode(Spatial.CULL_ALWAYS);
        }
        
        logger.info("Setting Up HUD");
        hudNode = new Node("HUD Node");
//        scoreHUD = new JMEDesktopHUD(score);
        scoreHUD = new Simple2DHud(score);
        hudNode.attachChild(scoreHUD.getRootNode());
        
        logger.info("Setting Up PressEffectManager");
        pressEffectManager = new PressEffectManager();
        rootNode.attachChild(pressEffectManager.getRootNode());
        
        logger.info("Creating The Fretboard");
        //Create the Fretboard
        fretboard = new Fretboard(song, 2.0f, buttonConfig.getLefty());
        
        logger.info("Adding All Notes to Fretboard");
        for(Note n : song.getAllNotes()){
            logger.info("Adding Note: " + n);
            fretboard.addNote(n);
        }
        this.rootNode.attachChild(fretboard.getRootNode());
        
        logger.info("Enabling FOG");
        //Fog
        FogState fs = DisplaySystem.getDisplaySystem().getRenderer().createFogState();
        fs.setEnd(VISUAL_DISTANCE);
        fs.setStart(VISUAL_DISTANCE - FADE_OUT_DISTANCE);
        fs.setColor(ColorRGBA.black);
        fs.setEnabled(true);
        rootNode.setRenderState(fs);
        
        //Enable lighting on the rootNode
        logger.info("Setting Up Lighting");
        LightState lightState = DisplaySystem.getDisplaySystem().getRenderer().createLightState();
        lightState.setEnabled(true);
        rootNode.setRenderState(lightState);
        
        //Add a light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(0,-0.5f,1));
        dl.setAmbient(new ColorRGBA(0.9f,0.9f,0.9f,1));
        dl.setEnabled(true);
        lightState.attach(dl);
        
        logger.info("Positioning Camera");
        cam.setLocation(new Vector3f(TRACK_WIDTH * (float) Song.TRACKS / 2.0f, -0.2f, 2f));
        cam.lookAt(new Vector3f(TRACK_WIDTH * (float) Song.TRACKS / 2.0f, 0.7f, 0), new Vector3f(0,1,0));
        cam.setFrustumFar(VISUAL_DISTANCE);
        cam.update();
        
        logger.info("Creating Gamepad Poller");
        //Create a poller to generate ButtonEvents with the specified
        //buttonConfig's gamepad
        try {
            poller = new GamepadPoller(buttonConfig.getGamepad());
        } catch (ControllerNotSupportedException ex) {
            handleException("Controller " + buttonConfig.getGamepad() + " is not supported.", ex);
        }
        poller.addButtonPressListener(this);
        song.addNoteChangeListener(this);
        
        logger.info("Loading Error Sounds");
        //Load some error sounds
        CoreSoundPlayer.getInstance().loadSound(Skin.getInstance().getResource("sounds/MissedNote0.wav"), "MissedNote0");
        CoreSoundPlayer.getInstance().loadSound(Skin.getInstance().getResource("sounds/MissedNote1.wav"), "MissedNote1");
        CoreSoundPlayer.getInstance().loadSound(Skin.getInstance().getResource("sounds/MissedNote2.wav"), "MissedNote2");
        CoreSoundPlayer.getInstance().loadSound(Skin.getInstance().getResource("sounds/MissedNote3.wav"), "MissedNote3");
        CoreSoundPlayer.getInstance().loadSound(Skin.getInstance().getResource("sounds/MissedNote4.wav"), "MissedNote4");
        CoreSoundPlayer.getInstance().loadSound(Skin.getInstance().getResource("sounds/MissedNote5.wav"), "MissedNote5");
        
        logger.info("Setting up ButtonHandler");
        //Setup the button handler
        buttonHandler = new ButtonHandler(buttonConfig, song, fretboard.getFingeringBoard(), score, music);

        logger.info("Updating rootNode renderstate and geometric state");
        rootNode.updateRenderState();
        rootNode.updateGeometricState(0, true);
        
        logger.info("Done initializing game resources");
    }
    
    public void simpleUpdate(){
       
        final float tpf = timer.getTimePerFrame();
        
        //Have to call this manually so the buttonActionTriggered() will occur in this thread
        //Otherwise JME isn't happy about scenegraph being changed from polling thread
        poller.pollController();
        
        final float musicTime = music.getTimeInSeconds();
        song.updateForTimeChange(musicTime);
        scoreHUD.setTime(music.getTimeInSeconds());
        scoreHUD.update(tpf);
        fretboard.updateBackgroundPosition(musicTime);
        pressEffectManager.update(tpf);
    }
    
    public void simpleRender(){
        //Have to call draw() on the HUD node manually since its not under rootnode
        DisplaySystem.getDisplaySystem().getRenderer().draw(hudNode);
    }
    
    public void buttonActionTriggered(ButtonEvent event) {
        buttonHandler.buttonActionTriggered(event);
    }
    
    private void handleException(String message, Exception ex){
        logger.warning(message);
        logger.warning(ex.getMessage());
        ex.printStackTrace();
        System.exit(0);
    }
    
    public void noteChangedState(NoteEvent event) {
        final NoteState oldState = event.getOldState();
        final NoteState newState = event.getNewState();
        
        if( (newState == NotePlayedState.getInstance() && oldState != HammeredState.getInstance()) ||
                newState == HammeredState.getInstance()){
            buttonHandler.setLastPressedNote(event.getNote());
            fretboard.removeNote(event.getNote());
            if(event.getNote().getDuration() == 0){
                pressEffectManager.notePressed(event.getNote().getButtonNumber());
            }else{
                //Turn off the 'hold' animation
                pressEffectManager.releaseNote(event.getNote().getButtonNumber());
            }
            score.hitNote();
            music.endErrorSilence();
        }else if(newState  == NoteMissedState.getInstance()){
            buttonHandler.setLastPressedNote(null);
            score.missedNote();
            music.beginErrorSilence();
            //CoreSoundPlayer.getInstance().playSound("MissedNoteSound");
        }else if(newState == NoteHoldState.getInstance()){
            buttonHandler.setLastPressedNote(null);
            music.endErrorSilence();
            pressEffectManager.notePressed(event.getNote().getButtonNumber());
            pressEffectManager.holdingNote(event.getNote().getButtonNumber());
        }else if(newState == PrematureReleaseState.getInstance()){
            buttonHandler.setLastPressedNote(null);
            pressEffectManager.releaseNote(event.getNote().getButtonNumber());
            music.beginErrorSilence();
        }else if(newState == PressableState.getInstance()){
            //Don't do anything
        }
    }
    
    public void invalidButtonPress() {
        CoreSoundPlayer.playRandomErrorSound();
        music.beginErrorSilence();
    }
    
    public void cleanup(){
        logger.info("Cleaning up FreetarGame");
        super.cleanup();
        music.stop();
        music = null;
        poller = null;
        song = null;
    }
}
