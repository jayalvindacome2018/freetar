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
import net.freetar.Song;

public class WaitingState extends NoteState {
    private static WaitingState instance = new WaitingState();
    
    public static WaitingState getInstance() {
        return instance;
    }
    
    //Keep constructor private
    private WaitingState() {}
    
    public void updateTime(float currentTime, Note e, float allowableErrorTime) {
        // If its time to activate the note
        if (currentTime >= e.getTime() - allowableErrorTime / 2.0f) {
            e.setState(PressableState.getInstance());
        }
    }
    
    public String toString() {
        return "Waiting State";
    }
    
    @Override
            public boolean buttonPressed(float currentTime, int buttonNumber, Note e) {return false;}
    
    @Override
            public void buttonReleased(float currentTime, int buttonNumber, Note e) {}
    
            public boolean hammerOn(float currentTime, int buttonNumber, Note n) {return false;}
}
