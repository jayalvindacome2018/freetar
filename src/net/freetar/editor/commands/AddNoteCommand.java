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
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public class AddNoteCommand extends UndoableCommand{
    private static final Logger logger = DebugHandler.getLogger("AddNoteCommand");


    private Song songToAddNotesTo;
    private Collection<Note> notesToAdd;
    private Collection<Note> notesRemoved = new ArrayList<Note>();

    /** Creates a new instance of AddNoteCommand */
    public AddNoteCommand(Collection<Note> notesToAdd, Song songToAddNotesTo) {
        this.notesToAdd = notesToAdd;
        this.songToAddNotesTo = songToAddNotesTo;
    }
    
    public AddNoteCommand(Note e, Song songToAddNotesTo){
        notesToAdd = new ArrayList<Note>();
        notesToAdd.add(e);
        this.songToAddNotesTo = songToAddNotesTo;
    }

    public void executeCommand() {
        for(Note e : notesToAdd){
            try{
                songToAddNotesTo.addNote(e);
            }catch(DuplicateNoteException ex){
                //Remove the existing note
                logger.warning("Duplicate Note Detected - Removing original note. " + ex);
                notesRemoved.add(ex.getExistingNote());
                songToAddNotesTo.removeNote(ex.getExistingNote());
            }
        }
    }

    public void undoCommand() {
        for(Note e : notesToAdd){
            songToAddNotesTo.removeNote(e);
        }
        for(Note e : notesRemoved){
            try {
                songToAddNotesTo.addNote(e);
            } catch (DuplicateNoteException ex) {
                logger.warning("Unable to replace place original note - " + ex);
            }
        }
    }
}
