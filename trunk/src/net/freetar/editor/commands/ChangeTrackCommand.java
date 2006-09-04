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
import net.freetar.InvalidButtonException;
import net.freetar.InvalidTimeException;
import net.freetar.Note;
import net.freetar.Song;
import java.util.Collection;

/**
 *
 * @author Temp
 */
public class ChangeTrackCommand extends UndoableCommand{
    private Collection<Note> notesToChange;
    private int increment;
    
    /** Creates a new instance of ChangeTrackCommand */
    public ChangeTrackCommand(Collection<Note> notesToChange, int increment) {
        this.notesToChange = notesToChange;
        this.increment = increment;
    }
    
    public void undoCommand() {
        for(Note e : notesToChange){
            try {
                e.setButtonNumber(e.getButtonNumber() - increment);
            } catch (DuplicateNoteException ex) {
                ex.printStackTrace();
            } catch (InvalidTimeException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void executeCommand() throws InvalidButtonException {
        //Check that we can actually move the notes whithout going
        //outside the number of tracks
        for(Note e : notesToChange){
            if(e.getButtonNumber() + increment >= Song.TRACKS ||
                    e.getButtonNumber() + increment < 0){
                throw new InvalidButtonException(e.getButtonNumber() + increment);
            }
        }
        if(increment > 0){
            for (int i = Song.TRACKS - 1; i >= 0; i--){
                for(Note e : notesToChange){
                    if(e.getButtonNumber() == i){
                        try {
                            e.setButtonNumber(e.getButtonNumber() + increment);
                        } catch (DuplicateNoteException ex) {
                            //ex.printStackTrace();
                        } catch (InvalidTimeException ex) {
                            //ex.printStackTrace();
                        }
                    }
                }
            }
        }else{
            for (int i = 0; i < Song.TRACKS; i++){
                for(Note e : notesToChange){
                    if(e.getButtonNumber() == i){
                        try {
                            e.setButtonNumber(e.getButtonNumber() + increment);
                        } catch (DuplicateNoteException ex) {
                            //ex.printStackTrace();
                        } catch (InvalidTimeException ex) {
                            //ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
}
