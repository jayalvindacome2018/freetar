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

import net.freetar.noteStates.FinalState;
import net.freetar.noteStates.PressableState;
import net.freetar.util.SongUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Anton
 */
public abstract class AbstractSong implements Song {
    protected SongProperties properties = new SongProperties();
    protected List<NoteChangeListener> noteChangeListeners = new ArrayList<NoteChangeListener>();
    protected Collection<Note> activeNotes = new HashSet<Note>();
    
    public float getBeatLength(){
        return 1.0f / properties.getBeatsPerSecond();
    }
    
    public int getLengthInMillis() {
        return SongUtils.convertToMillis(properties.getLength());
    }
    
    public Note createNote(float timeInSeconds, int buttonNumber){
        return createNote(timeInSeconds, 0, buttonNumber, false);
    }
    
    public boolean hasUnsavedChanges(){
        return properties.isDirty();
    }
    
    public void setUnsavedChanges(boolean hasUnsavedChanges){
        properties.setDirty(hasUnsavedChanges);
    }
    
    public SongProperties getProperties() {
        return properties;
    }
    
    public void setProperties(SongProperties properties) {
        this.properties = properties;
    }
    
    public void addNoteChangeListener(NoteChangeListener aListener){
        if(!noteChangeListeners.contains(aListener)){
            noteChangeListeners.add(aListener);
        }
    }
    
    public void removeNoteChangeListener(NoteChangeListener aListener){
        noteChangeListeners.remove(aListener);
    }
    
    public void notifyListenersOfNoteChange(NoteEvent event){
        //TODO refactor NoteState into an enum? so this is faster (no instanceof)
        //OR refactor this to have a shitload of if's
        if(event.getNewState() == PressableState.getInstance()){
            activeNotes.add(event.getNote());
        }else{
            activeNotes.remove(event.getNote());
        }
        //Notify listeners of change
        for(NoteChangeListener listener : noteChangeListeners){
            listener.noteChangedState(event);
        }
    }
    
    public Collection<Note> getActiveNotes(){
        return activeNotes;
    }
}
