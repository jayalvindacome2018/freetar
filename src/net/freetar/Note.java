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

import net.freetar.noteStates.NoteHoldState;
import net.freetar.noteStates.NoteMissedState;
import net.freetar.noteStates.NotePlayedState;
import net.freetar.noteStates.NoteState;
import net.freetar.noteStates.PrematureReleaseState;
import net.freetar.noteStates.PressableState;
import net.freetar.noteStates.WaitingState;

/**
 * This class represents the times and button numbers associated with the 'events'
 * in the song.
 *
 * TODO when notes are updated(), or pressed, they should transition
 * through as many states as needed for the current time. Not just
 * a single state transition. Do not count on update() being called
 * right away afterwords to move states.
 * @author Anton Struyk
 */
public interface Note extends Comparable<Note>{
    public final static NoteState WAITING_STATE = WaitingState.getInstance();
    public final static NoteState PRESSABLE_STATE = PressableState.getInstance();
    public final static NoteState MISSED_STATE = NoteMissedState.getInstance();
    public final static NoteState PLAYED_STATE = NotePlayedState.getInstance();
    public final static NoteState HOLDING_STATE = NoteHoldState.getInstance();
    
    //Information Methods
    public float getTime();
    public float getDuration();
    public int getButtonNumber();
    public int hashCode();
    public void setTime(float time) throws DuplicateNoteException, InvalidTimeException;
    public void setButtonNumber(int buttonNumber) throws DuplicateNoteException, InvalidTimeException;
    public void setDuration(float duration) throws DuplicateNoteException, InvalidDurationException;
    public void setHammerOnAllowed(boolean hammerAllowed);
    public boolean isHammerOnAllowed();
    
    //public void setSong(Song song);
    public Song getSong();
    
    //For getting/setting times of actual press (better way to do this?)
    public float getPressTime();    //The time the note was actually pressed at
    public void setPressTime(float pressTime); //The time the note was actually pressed at
    
    //Playback Related Methods
    public boolean isPressableAt(float timeInSeconds);
    public NoteState getState();
    public void setState(NoteState n);
    public abstract void resetState();
    public void updateState(float currentTime, float allowableErrorTime);
    public boolean buttonPressed(final float currentTime);
    public void buttonReleased(final float currentTime);
    public boolean hammeredOn(final float currentTime);
}
