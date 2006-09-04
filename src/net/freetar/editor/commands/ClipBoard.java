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

package net.freetar.editor.commands;

import net.freetar.Note;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anton
 */
public class ClipBoard {
    private Collection<ClipBoardNote> notes;

    public ClipBoard(){
        notes = new ArrayList<ClipBoardNote>();
    }
    
    public void setClipBoardData(Collection<Note> notes){
        this.notes.clear();
        
        for(Note e : notes){
            ClipBoardNote cbNote = new ClipBoardNote();
            cbNote.time = e.getTime();
            cbNote.holdTime = e.getDuration();
            cbNote.buttonNumber = e.getButtonNumber();
            cbNote.hammerOnAllowed = e.isHammerOnAllowed();
            this.notes.add(cbNote);
        }
    }
    
    public Collection<ClipBoardNote> getClipBoardData(){
        return notes;
    }
    
    public void clearClipBoard(){
        notes.clear();
    }
    
    public class ClipBoardNote implements Comparable<ClipBoardNote>{
        private float time;
        private float holdTime;
        private int buttonNumber;
        private boolean hammerOnAllowed;

        public int compareTo(ClipBoard.ClipBoardNote o) {
            return Float.compare(time, o.time);
        }
        
        public float getTime(){
            return time;
        }
        
        public float getHoldTime(){
            return holdTime;
        }
        
        public int getButtonNumber(){
            return buttonNumber;
        }
        
        public boolean getHammerOnAllowed(){
            return hammerOnAllowed;
        }
    }
}
