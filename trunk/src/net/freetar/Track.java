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
import net.freetar.noteStates.PressableState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Anton
 */
public class Track {
    protected Collection<Note> notes;
    
    public Track(){
        notes = new HashSet<Note>();
    }
    
    public void addNote(Note n) throws DuplicateNoteException{
        Note conflictingNote = getNoteAtTime(n.getTime());
        if(conflictingNote != null){
            throw new DuplicateNoteException(n, conflictingNote);
        }
        notes.add(n);
    }
    
    public void removeNote(Note n){
        notes.remove(n);
    }
    
    public List<Note> getNotes(){
        List<Note> allNotes = new ArrayList<Note>(notes);
        return allNotes;
    }
    
    public Note getNoteAtTime(float timeInSeconds){
        for(Note n : notes){
            if(timeInSeconds >= n.getTime() && timeInSeconds <= n.getTime() + n.getDuration()){
                return n;
            }
        }
        return null;
    }
    
    public Collection<Note> getPressableNotesAt(float timeInSeconds){
        List<Note> returnNotes = new ArrayList<Note>();
        for(Note n : notes){
            if(n.isPressableAt(timeInSeconds)){
                returnNotes.add(n);
            }
        }
        return returnNotes;
    }
    
    public Collection<Note> getNotesBetween(float startTime, float endTime){
        List<Note> returnNotes = new ArrayList<Note>();
        for(Note n : notes){
            if(n.getTime() >= startTime && n.getTime() <= endTime){
                returnNotes.add(n);
            }
        }
        return returnNotes;
    }
    
    public int getNumberOfNotes(){
        return notes.size();
    }
    
    public void updateForTimeChange(float currentTime, float allowableErrorTime){
        for(Note n : notes){
            n.updateState(currentTime, allowableErrorTime);
        }
    }
    
    public boolean buttonPressed(float currentTime){
        boolean success = false;
        for(Note n : notes){
            NoteState beforeState = n.getState();
            success = n.buttonPressed(currentTime);
            if(n.getState() != beforeState){
                break;
            }
        }
        return success;
    }
    
    public void buttonReleased(float currentTime){
        for(Note n : notes){
            n.buttonReleased(currentTime);
        }
    }
    
    public void resetNotes(){
        for(Note n : notes){
            n.resetState();
        }
    }
    
    public boolean containsNote(Note n){
        return notes.contains(n);
    }
}
