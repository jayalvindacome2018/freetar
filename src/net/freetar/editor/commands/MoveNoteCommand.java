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
import net.freetar.InvalidTimeException;
import net.freetar.Note;
import net.freetar.Song;

/**
 *
 * @author Anton
 */
public class MoveNoteCommand extends UndoableCommand{
    private Note noteToMove;
    private float timeToMoveNotes;
    
    /** Creates a new instance of MoveCommand */
    public MoveNoteCommand(Note noteToMove, float timeToMoveNotes) {
        this.noteToMove = noteToMove;
        this.timeToMoveNotes = timeToMoveNotes;
    }
    
    public void executeCommand() throws DuplicateNoteException, InvalidTimeException{
        noteToMove.setTime(noteToMove.getTime() + timeToMoveNotes);
    }
    
    public void undoCommand() throws DuplicateNoteException, InvalidTimeException{
        noteToMove.setTime(noteToMove.getTime() - timeToMoveNotes);
    }
    
}
