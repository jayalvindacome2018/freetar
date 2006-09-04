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
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public class DeleteNoteCommand extends UndoableCommand{
    private static final Logger logger = DebugHandler.getLogger("UndoableCommand");


    private Collection<Note> notesToDelete;
    private Song songToDeleteFrom;
    
    public DeleteNoteCommand(Collection<Note> notesToDelete, Song songToDeleteFrom){
        this.notesToDelete = notesToDelete;
        this.songToDeleteFrom = songToDeleteFrom;
    }
    
    public DeleteNoteCommand(Note noteToDelete, Song songToDeleteFrom){
        this.notesToDelete = new HashSet<Note>();
        notesToDelete.add(noteToDelete);
        this.songToDeleteFrom = songToDeleteFrom;
    }

    public void executeCommand() {
        for(Note n : notesToDelete){
            songToDeleteFrom.removeNote(n);
        }
    }

    public void undoCommand() {
        for(Note n : notesToDelete){
            try {
                songToDeleteFrom.addNote(n);
            } catch (DuplicateNoteException ex) {
                logger.warning("Unable to undo deletion of note " + ex.getDuplicateNote());
                ex.printStackTrace();
            }
        }
    }
    
}
