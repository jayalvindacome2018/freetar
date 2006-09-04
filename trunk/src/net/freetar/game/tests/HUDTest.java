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

package net.freetar.game.tests;

import net.freetar.Note;
import net.freetar.Song;
import net.freetar.editor.MusicEditor;
import net.freetar.game.*;
import net.freetar.io.FileFormatException;
import net.freetar.io.IncorrectVersionException;
import net.freetar.util.SongUtils;
import net.freetar.util.UnsupportedVersionException;
import com.jme.app.SimpleGame;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Box;
import com.jme.util.LoggingSystem;
import com.jme.util.Timer;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author Anton
 */
public class HUDTest extends SimpleGame{
    
    private Score testScore;
    private JMEDesktopHUD scoreDisplay;
    
    Fretboard fretboard;
    PressEffectManager pressEffects;
    Song song;
    
    int nextToRemove = 0;
    
    /** Creates a new instance of HUDTest */
    public HUDTest() {
    }
    
    protected void simpleInitGame() {
        testScore = new Score();
        scoreDisplay = new JMEDesktopHUD(testScore);
        rootNode.attachChild(scoreDisplay.getRootNode());
        
        pressEffects = new PressEffectManager();
        rootNode.attachChild(pressEffects.getRootNode());
        
        //Load the song before starting the new fretboard
        try {
            song = SongUtils.loadFromFile(new File("default.sng"));
        } catch (UnsupportedVersionException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (FileFormatException ex) {
            ex.printStackTrace();
        } catch (IncorrectVersionException ex) {
            ex.printStackTrace();
        }
        
        fretboard = new Fretboard(song);
        rootNode.attachChild(fretboard.getRootNode());
        
        //Add some notes
        Iterator<Note> noteIterator = song.getAllNotes().iterator();
        for(int i = 0; i < 10 && noteIterator.hasNext(); i++){
            fretboard.addNote(noteIterator.next());
        }
        
        KeyBindingManager.getKeyBindingManager().add("removeNode", KeyInput.KEY_BACKSLASH);
        input.addAction(new InputAction(){
            public void performAction(InputActionEvent evt) {
                LoggingSystem.getLoggingSystem().getLogger().info("Removing a note..");
                if(nextToRemove < 10){
                    Iterator<Note> noteIterator = song.getAllNotes().iterator();
                    for(int i = 0; i < nextToRemove && noteIterator.hasNext(); i++){
                        noteIterator.next();
                    }
                    Note removeNote = noteIterator.next();
                    nextToRemove++;
                    fretboard.removeNote(removeNote);
                }
                pressEffects.notePressed(0);
                pressEffects.notePressed(1);
                pressEffects.notePressed(2);
                pressEffects.notePressed(3);
                pressEffects.notePressed(4);
            }
        }, "removeNode", false);
        
        cam.setLocation(new Vector3f(FreetarSimpleGame.TRACK_WIDTH * (float) Song.TRACKS / 2.0f, -0.8f, 1.0f));
        cam.lookAt(new Vector3f(FreetarSimpleGame.TRACK_WIDTH * (float) Song.TRACKS / 2.0f, 0.8f, 0), new Vector3f(0,1,0));
        cam.setFrustumFar(FreetarSimpleGame.VISUAL_DISTANCE);
        cam.update();
    }
    
    public void simpleUpdate(){
        scoreDisplay.update(timer.getTimePerFrame());
        fretboard.update(timer.getTimePerFrame());
        pressEffects.update(timer.getTimePerFrame());
    }
    
    public void simpleRender(){
    }
    
    public static void main(String[] args){
        HUDTest app = new HUDTest();
        //app.setDialogBehaviour(ALWAYS_SHOW_PROPS_DIALOG);
        app.start();
    }
}
