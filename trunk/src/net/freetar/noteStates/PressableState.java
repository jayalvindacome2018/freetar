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

public class PressableState extends NoteState {
        private static PressableState instance = new PressableState();
        
        public static PressableState getInstance() {
            return instance;
        }
        
        private PressableState() {
        }
        
        public boolean buttonPressed(float currentTime, int buttonNumber, Note e) {
            if(buttonNumber == e.getButtonNumber()){
                e.setPressTime(currentTime);
                if (e.getDuration() == 0) {
                    e.setState(NotePlayedState.getInstance());
                } else {
                    e.setState(NoteHoldState.getInstance());
                }
                return true;
            }
            return false;
        }
        
        public void buttonReleased(float currentTime, int buttonNumber, Note e){
        }
        
        public void updateTime(float currentTime, Note e, float allowableErrorTime) {
            // If we missed the time allowed for pressing this button,
            // then we missed the button press
            if (currentTime > e.getTime() + allowableErrorTime / 2.0f) {
                e.setState(NoteMissedState.getInstance());
            }
        }
        
        public String toString() {
            return "Pressable State";
        }   

    public boolean hammerOn(float currentTime, int buttonNumber, Note n) {
        if(n.isHammerOnAllowed()){
           if(buttonNumber == n.getButtonNumber()){
                n.setPressTime(currentTime);
                if (n.getDuration() == 0) {
                    n.setState(HammeredState.getInstance());
                } else {
                    n.setState(NoteHoldState.getInstance());
                }
                return true;
            }
        }
        return false;
    }
 }
