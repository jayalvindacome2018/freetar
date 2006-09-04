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
import java.util.Collection;

/**
 *
 * @author Anton
 */
public class CopyNoteCommand implements Command{
    private Collection<Note> notesToCopy;
    private ClipBoard clipboard;
    
    /** Creates a new instance of CopyNoteCommand */
    public CopyNoteCommand(Collection<Note> notesToCopy, ClipBoard clipboard) {
        this.clipboard = clipboard;
        this.notesToCopy = notesToCopy;
    }

    public boolean isUndoable() {
        return false;
    }

    public void undo() {
    }

    public void execute() {
        clipboard.setClipBoardData(notesToCopy);
    }
}
