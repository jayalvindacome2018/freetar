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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Anton
 */
public class ChangeNoteHammerCommand extends UndoableCommand{
    private Map<Note, Boolean> originalStates;
    private boolean hammerOnAllowed;
    
    /** Creates a new instance of ChangeNoteHammerCommand */
    public ChangeNoteHammerCommand(Collection<Note> notesToChange, boolean hammerOnAllowed) {
        this.hammerOnAllowed = hammerOnAllowed;
        originalStates = new HashMap<Note, Boolean>();

        //Just stored right now to build keyset (prevents having another collection)
        for(Note n : notesToChange){
            originalStates.put(n, n.isHammerOnAllowed());
        }
    }
    
    protected void executeCommand() throws Exception {
        for(Note n : originalStates.keySet()){
            //Update the states (in case they changed between creation and execution)
            originalStates.put(n, n.isHammerOnAllowed());
            //Set the new note states
            n.setHammerOnAllowed(hammerOnAllowed);
        }
    }
    
    protected void undoCommand() throws Exception {
        for(Note n : originalStates.keySet()){
            n.setHammerOnAllowed(originalStates.get(n));
        }
    }
    
}
