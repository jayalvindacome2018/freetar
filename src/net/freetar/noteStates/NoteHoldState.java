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

package net.freetar.noteStates;

import net.freetar.Note;

public class NoteHoldState extends NoteState {
    private static NoteHoldState instance = new NoteHoldState();
    
    public static NoteHoldState getInstance() {
        return instance;
    }
    
    private NoteHoldState() {
    }
    
    public void buttonReleased(float currentTime, int buttonNumber, Note e) {
        if(buttonNumber == e.getButtonNumber()){
            e.setState(PrematureReleaseState.getInstance());
        }
    }
    
    public boolean buttonPressed(float currentTime, int buttonNumber, Note e){
        //Don't have to do anything here?
        return false;
    }
    
    public void updateTime(float currentTime, Note e, float allowableErrorTime) {
        // If enough time has passed while still holding the note
        if (currentTime > e.getTime() + e.getDuration()) {
            e.setState(NotePlayedState.getInstance());
        }
    }
    
    public String toString() {
        return "Hold Note State";
    }

    public boolean hammerOn(float currentTime, int buttonNumber, Note n) {
        //Dont have to do anything here...
        return false;
    }
}
