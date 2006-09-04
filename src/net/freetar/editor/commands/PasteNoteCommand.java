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

import net.freetar.SimpleNote;
import net.freetar.DuplicateNoteException;
import net.freetar.Note;
import net.freetar.Song;
import net.freetar.editor.SongDisplayPanel;
import net.freetar.util.DebugHandler;
import net.freetar.util.SongUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import net.freetar.editor.commands.ClipBoard.ClipBoardNote;
/**
 *
 * @author Anton
 */
public class PasteNoteCommand extends UndoableCommand{
    private static final Logger logger = DebugHandler.getLogger("PasteNoteCommand");

    private Collection<Note> notesToPaste;
    private Song songToPasteInto;
    
    /** Creates a new instance of PasteNoteCommand */
    public PasteNoteCommand(ClipBoard clipboard, Song songToPasteInto, float firstNoteTime, SongDisplayPanel panel) {
        this.songToPasteInto = songToPasteInto;
        //Snap the first note (and only the first note)
        firstNoteTime = SongUtils.snapTimeToNearest(firstNoteTime, panel.getSnapTime());
        //Create the list of new notes at the correct timings
        notesToPaste = new ArrayList<Note>();
        
        if(clipboard.getClipBoardData() != null && clipboard.getClipBoardData().size() > 0){
            //TODO make clipboard return an ordered list by default, so we can skip the 'sorting'
            List<ClipBoardNote> clipBoardNotes = new ArrayList<ClipBoardNote>(clipboard.getClipBoardData());
            Collections.sort(clipBoardNotes);
            
            //Calculate the time-shift required for each note
            final float originalFirstNoteTime = clipBoardNotes.get(0).getTime();
            final float noteTimeShift = firstNoteTime - originalFirstNoteTime;
            
            //Create a copy of the note, with the correct time shift
            final float songLength = songToPasteInto.getProperties().getLength();
            for(ClipBoardNote e : clipBoardNotes){
                if(e.getTime() > 0 && e.getTime() + noteTimeShift <= songLength){
                    Note newNote = songToPasteInto.createNote(
                            e.getTime() + noteTimeShift,
                            e.getHoldTime(),
                            e.getButtonNumber(),
                            e.getHammerOnAllowed());
                    notesToPaste.add(newNote);
                }
            }
        }
    }
    
    public void undoCommand() {
        for(Note e : notesToPaste){
            songToPasteInto.removeNote(e);
        }
    }
    
    public void executeCommand() {
        for(Note e : notesToPaste){
            try {
                songToPasteInto.addNote(e);
            } catch (DuplicateNoteException ex) {
                logger.warning("Attempted to paste note on top of existing note: \n" + ex);
            }
        }
    }
    
    public Collection<Note> getPastedNotes(){
        return notesToPaste;
    }
}
