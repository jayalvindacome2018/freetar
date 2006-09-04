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

package net.freetar;

import net.freetar.noteStates.NoteState;
import net.freetar.noteStates.WaitingState;
import net.freetar.util.DebugHandler;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public abstract class AbstractNote implements Note{
    private static final Logger logger = DebugHandler.getLogger("SimpleNote");
    
    protected NoteState state;      //The state of this note
    protected Song song;            //The song this note belongs to
    protected int buttonNumber;     //The button number of this note
    protected float pressTime;      //The time that this note was pressed. If the note is not in a Finalstate - this value is unspecified
    protected boolean hammerOnAllowed = false;
    
    protected AbstractNote(int buttonNumber, Song song){
        state = WaitingState.getInstance();
        this.buttonNumber = buttonNumber;
        this.song = song;
    }
    
    public NoteState getState(){
        return state;
    }
    
    public boolean buttonPressed(float currentTime) {
        return this.state.buttonPressed(currentTime, buttonNumber, this);
    }
    
    public void buttonReleased(float currentTime) {
        this.state.buttonReleased(currentTime, buttonNumber, this);
    }
    
    public boolean hammeredOn(float currentTime){
       return this.state.hammerOn(currentTime, buttonNumber, this);
    }
    
    public void setState(NoteState newState){
        song.notifyListenersOfNoteChange(new NoteEvent(this, state, newState));
        logger.fine("Note " + this + " is changing from "+ getState() + " to " + newState);
        state = newState;
    }
    
    public void updateState(float currentTime, float allowableErrorTime) {
        state.updateTime(currentTime, this, allowableErrorTime);
    }
    
    public void resetState(){
        setState(WaitingState.getInstance());
    }
    
    public int getButtonNumber(){
        return buttonNumber;
    }
    
    public int compareTo(Note p) {
        if (this.getTime() < p.getTime())
            return -1;
        if (this.getTime() > p.getTime())
            return 1;
        return 0;
    }
    
    public String toString() {
        return "[Note t:" + this.getTime() + ", d: " + this.getDuration() + ", buttonNumber: " + this.getButtonNumber() + "]";
    }
    
    public float getPressTime(){
        return pressTime;
    }
    
    public void setPressTime(float pressTime){
        this.pressTime = pressTime;
    }
    
    public Song getSong(){
        return song;
    }
    
    public boolean isHammerOnAllowed(){
        return hammerOnAllowed;
    }
    
    public void setHammerOnAllowed(boolean hammerOnAllowed){
        this.hammerOnAllowed = hammerOnAllowed;
    }
}
