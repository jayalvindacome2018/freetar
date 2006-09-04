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

import net.freetar.DuplicateNoteException;
import net.freetar.Note;
import net.freetar.Song;
import net.freetar.util.DebugHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public class ChangeSongBPSCommand extends UndoableCommand{
    private static final Logger logger = DebugHandler.getLogger("ChangeSongBPSCommand");


    private ArrayList<NoteData> originalNotes;
    private float originalBPS;
    private float newBPS;
    private Song song;
    
    /** Creates a new instance of ChangeSongBPSCommand */
    public ChangeSongBPSCommand(float newBPS, Song songToChange) {
        this.newBPS = newBPS;
        this.song = songToChange;
        
        //Save original BPS
        originalBPS = songToChange.getProperties().getBeatsPerSecond();
        
        //Save basic information on the original note placement
        originalNotes = new ArrayList<NoteData>();
        for(Note n : songToChange.getAllNotes()){
            originalNotes.add(new NoteData(n));
        }
    }
    
    public void undoCommand() {
        //Remove all the notes in the song
        Collection<Note> allNotes = song.getAllNotes();
        for(Note n : allNotes){
            song.removeNote(n);
        }
        
        //Revert the BPS
        song.getProperties().setBeatsPerSecond(originalBPS);
        
        //Re-add the original notes
        for(NoteData n : originalNotes){
            try{
            song.addNote(song.createNote(n.time, n.duration, n.trackNumber, n.hammerOnAllowed));
            }catch(DuplicateNoteException ex){
                logger.warning("Should not see this - Song reverting to original state. Ignored duplicate note: " + ex);
            }
        }
    }
    
    public void executeCommand() {
        //We need to re-add all the notes so that they are snapped to the
        //correct times
        
        //Remove all notes
        Collection<Note> allNotes = song.getAllNotes();
        for(Note n : allNotes){
            song.removeNote(n);
        }
        
        //Set the new BPS
        song.getProperties().setBeatsPerSecond(newBPS);
        
        //Try to add all the notes back in
        for(Note n : allNotes){
            try{
                song.addNote(n);
            }catch(DuplicateNoteException ex){
                logger.warning("Duplicate Note Ignored: " + ex);
            }
        }
    }
    
    //Internal class for storing the noteData temporarily
    private class NoteData{
        public float time;
        public float duration;
        private int trackNumber;
        private boolean hammerOnAllowed;
        
        public NoteData(Note n){
            this.time = n.getTime();
            this.duration = n.getDuration();
            this.trackNumber = n.getButtonNumber();
            this.hammerOnAllowed = n.isHammerOnAllowed();
        }
    }
}