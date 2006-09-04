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
import net.freetar.noteStates.HammeredState;
import net.freetar.noteStates.NoteMissedState;
import net.freetar.noteStates.PressableState;
import net.freetar.util.DebugHandler;
import net.freetar.util.SongUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public class TrackBasedSong extends AbstractSong {
    private static final Logger logger = DebugHandler.getLogger("TrackBasedSong");
    
    protected final Track[] tracks = new Track[Song.TRACKS];
    
    public TrackBasedSong(){
        super();
        for(int i = 0; i < Song.TRACKS; i++){
            tracks[i] = new Track();
        }
    }
    
    public int totalNotes() {
        int returnValue = 0;
        for(Track t : tracks){
            returnValue += t.getNumberOfNotes();
        }
        return returnValue;
    }
    
    public void updateForTimeChange(float currentTime) {
        for(Track t: tracks){
            t.updateForTimeChange(currentTime, properties.getAllowableErrorTime());
        }
    }
    
    public boolean strumNotes(List<Integer> pressedButtonNumbers, float currentTime){
        //Create a list of the notes that are pressable at this time
        List<Note> pressableNotes = new ArrayList<Note>();
        for(int i = 0; i < Song.TRACKS; i++){
            Collection<Note> notes = tracks[i].getPressableNotesAt(currentTime);
            for(Note n : notes){
                if(n.getState() == PressableState.getInstance() || n.getState() == HammeredState.getInstance()){
                    pressableNotes.add(n);
                }
            }
        }
        
        //If there are no pressable notes, and there is at least one button pressed then FAIL
        if(pressableNotes.size() == 0 && pressedButtonNumbers.size() > 0){
            return false;
        }
        
        //Find the earliest time of the notes that are pressable (ie. first chord/note)
        float firstTime = Float.MAX_VALUE;
        for(Note n : pressableNotes){
            if(n.getTime() < firstTime){
                firstTime = n.getTime();
            }
        }
        
        //Remove all the notes from pressableNotes that are not at the same time (ie. not in same chord)
        for(Iterator<Note> i = pressableNotes.iterator(); i.hasNext(); ){
            Note n = i.next();
            if(n.getTime() != firstTime){
                i.remove();
            }
        }
        
        //Find the track number of the lowest note
        int lowestNote = Integer.MAX_VALUE;
        for(Note n : pressableNotes){
            if(n.getButtonNumber() < lowestNote){
                lowestNote = n.getButtonNumber();
            }
        }
        
        //Check to see that all notes have a corresponding button pressed
        boolean allNotesHaveButtonPressed = true;
        for(Note n : pressableNotes){
            if(!pressedButtonNumbers.contains(n.getButtonNumber())){
                allNotesHaveButtonPressed = false;
            }
        }
        
        //Check to see that all buttons pressed have a corresponding note
        boolean allButtonsPressedHaveNote = true;
        for(int buttonNumber : pressedButtonNumbers){
            if(buttonNumber >= lowestNote){
                //Search through the buttonNumbers that are above the lowest note only
                boolean foundCorrespondingNote = false;
                for(Note n : pressableNotes){
                    if(n.getButtonNumber() == buttonNumber){
                        foundCorrespondingNote = true;
                        break;
                    }
                }
                if(!foundCorrespondingNote){
                    allButtonsPressedHaveNote = false;
                    break;
                }
            }
        }
        
        if(!allButtonsPressedHaveNote || !allNotesHaveButtonPressed){
            //If either of the above 2 cases are FALSE, then all of the notes should
            //be 'Errored''
            for(Note n : pressableNotes){
                n.setState(NoteMissedState.getInstance());
            }
            
            //Notify the listeners that there was an error with that press
            return false;
        }else{
            //otherwise simply pass on the press to the buttons
            for(Note n: pressableNotes){
                if(!n.buttonPressed(currentTime)){
                    //NOTE! a failure here leaves the notes in an unspecified state!
                    //i.e. Not ALL notes have passed or failed!
                    //TODO make sure all notes are in same state before fail
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean strumNote(int buttonNumber, float currentTime) {
        return tracks[buttonNumber].buttonPressed(currentTime);
    }
    
    public void releaseNote(int buttonNumber, float currentTime) {
        tracks[buttonNumber].buttonReleased(currentTime);
    }
    
    public void resetSong() {
        for(Track t : tracks){
            t.resetNotes();
        }
        activeNotes.clear();
    }
    
    public void addNote(Note n) throws DuplicateNoteException{
        if(n == null){
            logger.warning("Null passed to addNote() doing nothing");
            return;
        }
        if(n.getTime() > 0 && n.getTime() + n.getDuration() < this.properties.getLength()){
            tracks[n.getButtonNumber()].addNote(n);
            this.properties.setDirty(true);
        }else{
            logger.warning("Time out of range - not adding.");
        }
    }
    
    public void removeNote(Note n) {
        tracks[n.getButtonNumber()].removeNote(n);
        this.properties.setDirty(true);
    }
    
    public List<Note> getAllNotes() {
        List<Note> allNotes = new ArrayList<Note>();
        for(Track t : tracks){
            allNotes.addAll(t.getNotes());
        }
        Collections.sort(allNotes);
        return allNotes;
    }
    
    public Collection<Note> getNotesForButton(int button) {
        return tracks[button].getNotes();
    }
    
    public Collection<Note> getNotesPressableAt(float timeInSeconds, int button) {
        return tracks[button].getPressableNotesAt(timeInSeconds);
    }
    
    public Collection<Note> getNotesBetween(float startTimeInSeconds, float endTimeInSeconds) {
        if(startTimeInSeconds > endTimeInSeconds) return getNotesBetween(endTimeInSeconds, startTimeInSeconds);
        
        List<Note> notes = new ArrayList<Note>();
        for(Track t : tracks){
            notes.addAll(t.getNotesBetween(startTimeInSeconds, endTimeInSeconds));
        }
        Collections.sort(notes);
        return notes;
    }
    
    public Collection<Note> getNotesBetween(float startTimeInSeconds, float endTimeInSeconds, int buttonNumber) {
        if(startTimeInSeconds > endTimeInSeconds) return getNotesBetween(endTimeInSeconds, startTimeInSeconds);
        return tracks[buttonNumber].getNotesBetween(startTimeInSeconds, endTimeInSeconds);
    }
    
    public Note createNote(float timeInSeconds, float noteHoldTime, int buttonNumber, boolean hammerOnAllowed) {
        final float snappedTime = SongUtils.snapTimeToNearest(timeInSeconds, this.getBeatLength());
        final float snappedHoldTime = SongUtils.snapTimeToNearest(noteHoldTime, this.getBeatLength());
        Note newNote = new SimpleNote(snappedTime, snappedHoldTime, buttonNumber, this);
        newNote.setHammerOnAllowed(hammerOnAllowed);
        return newNote;
    }
    
    public boolean containsNote(Note n){
        return tracks[n.getButtonNumber()].containsNote(n);
    }
    
    public boolean tryHammerOn(float currentTime, int buttonNumber) {
        Collection<Note> pressableNotes = getNotesPressableAt(currentTime, buttonNumber);
        if(pressableNotes.size() > 0){
            //Find the earliest time of the notes that are pressable (ie. first chord/note)
            float firstTime = Float.MAX_VALUE;
            for(Note n : pressableNotes){
                if(n.getTime() < firstTime){
                    firstTime = n.getTime();
                }
            }
            
            //Remove all the notes from pressableNotes that are not at the same time (ie. not in same chord)
            for(Iterator<Note> i = pressableNotes.iterator(); i.hasNext(); ){
                Note n = i.next();
                if(n.getTime() != firstTime){
                    i.remove();
                }
            }
            
            Note noteAtCurrentTime = pressableNotes.iterator().next();   //First element (arbitrary)
            if(noteAtCurrentTime != null &&                                     //Has to be at least 1 note
                    pressableNotes.size() == 1 &&                               //Can only be a single note (no chords)
                    noteAtCurrentTime.isHammerOnAllowed() &&                    //Has to be hammerable
                    noteAtCurrentTime.getButtonNumber() == buttonNumber &&      //Has to be the right button number
                    ! (noteAtCurrentTime.getState() instanceof FinalState)){    //Can't be already final
                return noteAtCurrentTime.hammeredOn(currentTime);
            }
        }
        return false;
    }
    
}
