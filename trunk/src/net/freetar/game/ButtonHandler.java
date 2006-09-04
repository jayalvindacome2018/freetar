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
import net.freetar.bgmusic.BackgroundMusic;
import net.freetar.input.ButtonConfig;
import net.freetar.input.ButtonEvent;
import net.freetar.util.DebugHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public class ButtonHandler {
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.freetar.game.ButtonHandler");
    
    private FingeringBoard fingeringBoard;
    private Song song;
    private BackgroundMusic music;
    private ButtonConfig buttonConfig;
    private Score score;
    
    private Note lastPressedNote = null;
    
    /** Creates a new instance of ButtonHandler */
    public ButtonHandler(ButtonConfig buttonConfig, Song song, FingeringBoard fingeringBoard, Score score, BackgroundMusic music) {
        this.buttonConfig = buttonConfig;
        this.song = song;
        this.fingeringBoard = fingeringBoard;
        this.score = score;
        this.music = music;
    }
    
    public void setLastPressedNote(Note note){
        this.lastPressedNote = note;
    }
    
    public void buttonActionTriggered(ButtonEvent event){
        final ButtonConfig.Action assignment = buttonConfig.getAssignmentFor(event.getButton());
        final float currentTime = music.getTimeInSeconds();
        if(assignment == null){
            return;
        }
        
        if(assignment == ButtonConfig.Action.PAUSE && event.getEventType() == ButtonEvent.EventType.BUTTON_PRESSED){
            try {
                music.pause();
            } catch (BackgroundMusic.MusicException ex) {
                ex.printStackTrace();
                logger.warning("Could not pause music: " + ex);
            }
            return;
        }
        
        //Activate/Deactivate the fingering board
        switch(event.getEventType()){
            case BUTTON_PRESSED:
                switch(assignment){
                    case TRACK_0:
                        fingeringBoard.activateButton(0);
                        break;
                    case TRACK_1:
                        fingeringBoard.activateButton(1);
                        break;
                    case TRACK_2:
                        fingeringBoard.activateButton(2);
                        break;
                    case TRACK_3:
                        fingeringBoard.activateButton(3);
                        break;
                    case TRACK_4:
                        fingeringBoard.activateButton(4);
                        break;
                }
                break;
            case BUTTON_RELEASED:
                switch(assignment){
                    case TRACK_0:
                        fingeringBoard.deactivateButton(0);
                        break;
                    case TRACK_1:
                        fingeringBoard.deactivateButton(1);
                        break;
                    case TRACK_2:
                        fingeringBoard.deactivateButton(2);
                        break;
                    case TRACK_3:
                        fingeringBoard.deactivateButton(3);
                        break;
                    case TRACK_4:
                        fingeringBoard.deactivateButton(4);
                        break;
                }
                break;
        }
        
        //If this buttonconfig doesn't require strum (and we're not in a dummy event)
        //Generate a dummy event to handle the 'strum' of the note
        //TODO Because of this system, buttonconfigs without strums can't do chords
        //TODO also, buttonconfigs without strum event still need to map a key to strum down
        if( !buttonConfig.isStrumRequired() &&
                assignment != ButtonConfig.Action.STRUM_DOWN &&
                assignment != ButtonConfig.Action.STRUM_UP){
            ButtonEvent dummyEvent = new ButtonEvent(event.getGamepad(), buttonConfig.getButtonFor(ButtonConfig.Action.STRUM_DOWN), ButtonEvent.EventType.BUTTON_PRESSED);
            buttonActionTriggered(dummyEvent);
            return;
        }
        
        //Handle the button press logic
        switch(event.getEventType()){
            case BUTTON_PRESSED:
                switch(assignment){
                    case TRACK_1:
                        if(shouldTryHammerOn(1)){
                            if(!song.tryHammerOn(currentTime, 1)){
                                lastPressedNote = null; //prevent more attempts?
                            }else{
                                score.hammeredNote();
                            }
                        }
                        break;
                    case TRACK_2:
                        if(shouldTryHammerOn(2)){
                            if(!song.tryHammerOn(currentTime, 2)){
                                lastPressedNote = null;
                            }else{
                                score.hammeredNote();
                            }
                        }
                        break;
                    case TRACK_3:
                        if(shouldTryHammerOn(3)){
                            if(!song.tryHammerOn(currentTime, 3)){
                                lastPressedNote = null;
                            }else{
                                score.hammeredNote();
                            }
                        }
                        break;
                    case TRACK_4:
                        if(shouldTryHammerOn(4)){
                            if(!song.tryHammerOn(currentTime, 4)){
                                lastPressedNote = null;
                            }else{
                                score.hammeredNote();
                            }
                        }
                        break;
                    case STRUM_UP:
                    case STRUM_DOWN:
                        if(buttonConfig.isStrumRequired()){
                            List<Integer> buttonsPressed = new ArrayList<Integer>();
                            //Check which buttons are down, and send the 'pressed' values
                            if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_0).isPressed()){
                                buttonsPressed.add(0);
                            }
                            if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_1).isPressed()){
                                buttonsPressed.add(1);
                            }
                            if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_2).isPressed()){
                                buttonsPressed.add(2);
                            }
                            if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_3).isPressed()){
                                buttonsPressed.add(3);
                            }
                            if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_4).isPressed()){
                                buttonsPressed.add(4);
                            }
                            
                            if(buttonsPressed.size() > 0){
                                if(!song.strumNotes(buttonsPressed, currentTime)){
                                    this.handleErrorPress();
                                }
                            }else{
                                //There were no notes pressed - Cause and Error Press
                                handleErrorPress();
                            }
                        }
                        break;
                    default:
                        //logger.warning("Unhandled button press for " + assignment);
                        break;
                }
                break;
            case BUTTON_RELEASED:
                switch(assignment){
                    case TRACK_0:
                        song.releaseNote(0, currentTime);
                        break;
                    case TRACK_1:
                        song.releaseNote(1, currentTime);
                        if(lastPressedNote != null &&
                                lastPressedNote.getButtonNumber() == 1 &&
                                buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_0).isPressed()){
                            if(song.tryHammerOn(currentTime, 0)){
                                score.hammeredNote();
                            }
                        }
                        break;
                    case TRACK_2:
                        song.releaseNote(2, currentTime);
                        if(lastPressedNote != null &&
                                lastPressedNote.getButtonNumber() == 2 &&
                                buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_1).isPressed()){
                            if(song.tryHammerOn(currentTime, 1)){
                                score.hammeredNote();
                            }
                        }
                        break;
                    case TRACK_3:
                        song.releaseNote(3, currentTime);
                        if(lastPressedNote != null &&
                                lastPressedNote.getButtonNumber() == 3 &&
                                buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_2).isPressed()){
                            if(song.tryHammerOn(currentTime, 2)){
                                score.hammeredNote();
                            }
                        }
                        break;
                    case TRACK_4:
                        song.releaseNote(4, currentTime);
                        if(lastPressedNote != null &&
                                lastPressedNote.getButtonNumber() == 4 &&
                                buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_3).isPressed()){
                            if(song.tryHammerOn(currentTime, 3)){
                                score.hammeredNote();
                            }
                        }
                        break;
                }
                break;
        }
    }
    
    private void handleErrorPress(){
        CoreSoundPlayer.playRandomErrorSound();
        music.beginErrorSilence();
    }
    
    private boolean shouldTryPullOff(int pullOffTrack){
        //If the last note pressed wasn't pressed correctly, return false
        if(lastPressedNote == null){
            return false;
        }
        
        //The last pressed note has to be ABOVE the pullOffTrack
        if(lastPressedNote.getButtonNumber() <= pullOffTrack){
            return false;
        }
        
        //None of the other buttons above the pullOffTrack should be pressed
        for(int i = pullOffTrack + 1; i < Song.TRACKS; i++){
            if(buttonConfig.isButtonPressedForTrack(i)){
                return false;
            }
        }
        
        //TODO redundant? - we already check above pullOffTrack
        //No other buttons between the last pressed note and the track we are trying should be pressed
        for(int i = pullOffTrack + 1; i < lastPressedNote.getButtonNumber(); i++){
            if(buttonConfig.isButtonPressedForTrack(i)){
                return false;
            }
        }
        
        //Check that the track we are trying to pull off to is actually pressed
        return buttonConfig.isButtonPressedForTrack(pullOffTrack);
    }
    
    private boolean shouldTryHammerOn(int hammerOnTrack){
        //If the last note wasn't pressed correctly, return false
        if(lastPressedNote == null){
            return false;
        }
        
        //The last pressed Note has to be BELOW the hammerOnTrack
        if(lastPressedNote.getButtonNumber() >= hammerOnTrack){
            return false;
        }
        
        //None of the other buttons between the last pressed note and the track we are trying should be pressed
        for(int i = lastPressedNote.getButtonNumber() + 1; i < hammerOnTrack; i++){
            if(buttonConfig.isButtonPressedForTrack(i)){
                return false;
            }
        }
        
        //None of the buttons above the track we are trying should be pressed
        for(int i = hammerOnTrack + 1; i < Song.TRACKS; i++){
            if(buttonConfig.isButtonPressedForTrack(i)){
                return false;
            }
        }
        //Check that the button for the last pressed note is still held
        return buttonConfig.isButtonPressedForTrack(lastPressedNote.getButtonNumber());
    }
    
}
