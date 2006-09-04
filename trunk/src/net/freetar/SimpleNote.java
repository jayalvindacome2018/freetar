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
import net.freetar.util.SongUtils;
import java.util.Collection;
import java.util.logging.Logger;

/*
 * PlayableNote.java
 *
 * Created on January 1, 2006, 9:38 PM
 *
 */
public class SimpleNote extends AbstractNote {
    protected static final Logger logger = DebugHandler.getLogger("SimpleNote");
    
    protected float time;
    protected float duration;
    
    public SimpleNote(float time, float duration, int buttonNumber, Song song) {
        super(buttonNumber, song);
        this.time = time;
        this.duration = duration;
    }
    
    public float getTime() {
        return time;
    }
    
    public float getDuration() {
        return duration;
    }
    
    public int hashCode(){
        return SongUtils.convertToMillis(time);
    }
    
    public boolean equals(Object o){
        if(!(o instanceof SimpleNote)){
            return false;
        }
        SimpleNote other = (SimpleNote) o;
        return other.getTime() == this.getTime() && other.getButtonNumber() == this.getButtonNumber();
    }
    
    public boolean isPressableAt(float timeInSeconds){
        return (timeInSeconds >= this.getTime() - song.getProperties().getAllowableErrorTime()
        && timeInSeconds <= this.getTime() + song.getProperties().getAllowableErrorTime());
    }
    
    public void setTime(float newTime) throws DuplicateNoteException, InvalidTimeException{
        if(!song.containsNote(this)) return;
        
        if(newTime < 0 || newTime > song.getProperties().getLength()){
            throw new InvalidTimeException(newTime);
        }
        
        Collection<Note> conflictingNotes = song.getNotesBetween(newTime, newTime + duration, buttonNumber);
        conflictingNotes.remove(this);
        if(conflictingNotes.size() == 0){
            song.removeNote(this);
            this.time = newTime;
            song.addNote(this);
        }else{
            throw new DuplicateNoteException(this, conflictingNotes.iterator().next());
        }
    }
    
    public void setDuration(float newDuration) throws DuplicateNoteException, InvalidDurationException{
        if(!song.containsNote(this)) return;
        
        if(newDuration < 0 || newDuration + time > song.getProperties().getLength()){
            throw new InvalidDurationException(newDuration, this);
        }
        
        Collection<Note> conflictingNotes = song.getNotesBetween(time, time + newDuration, buttonNumber);
        conflictingNotes.remove(this);
        if(conflictingNotes.size() == 0){
            song.removeNote(this);
            this.duration = newDuration;
            song.addNote(this);
        }else{
            throw new DuplicateNoteException(this, conflictingNotes.iterator().next());
        }
    }
    
    public void setButtonNumber(int newButtonNumber) throws DuplicateNoteException, InvalidButtonException{
        if(!song.containsNote(this)) return;
        
        if(newButtonNumber > Song.TRACKS || newButtonNumber < 0){
            throw new InvalidButtonException(newButtonNumber);
        }
        
        Collection<Note> conflictingNotes = song.getNotesBetween(time, time + duration, newButtonNumber);
        if(conflictingNotes.size() == 0){
            song.removeNote(this);
            this.buttonNumber = newButtonNumber;
            song.addNote(this);
        }else{
            conflictingNotes.remove(this);
            throw new DuplicateNoteException(this, conflictingNotes.iterator().next());
        }
    }
    
}
