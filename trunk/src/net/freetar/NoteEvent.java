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

import net.freetar.noteStates.NoteMissedState;
import net.freetar.noteStates.NoteState;

/**
 *
 * @author Anton
 */
public class NoteEvent {
    private Note note;
    private NoteState oldState;
    private NoteState newState;
    
    /** Creates a new instance of NoteEvent */
    public NoteEvent(Note note, NoteState oldState, NoteState newState) {
        this.note = note;
        this.oldState = oldState;
        this.newState = newState;
    }
    
    public Note getNote(){
        return note;
    }
    
    public NoteState getOldState(){
        return oldState;
    }
    
    public NoteState getNewState(){
        return newState;
    }
    
    public String toString(){
        return "NoteEvent " + note + " changing FROM " + oldState + " TO " + newState;
    }
}
