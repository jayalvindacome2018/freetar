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

import java.util.Collection;
import java.util.List;

/**
 * Song provides the interface for aggregating and interacting with Note objects.
 * @author Antonie Struyk
 */
public interface Song{
    /**
     * The number of 'tracks' that the Songs hold.
     */
    public static final int TRACKS = 5;
    /**
     * Default number of Beats Per Minute (resolution) of the song
     */
    public static final float DEFAULT_BPM = 120.0f * 8.0f;
    
    //Information Methods
    /**
     * Determines if the song has changes that have not been saved.
     * @return true if the song has unsaved changes, false otherwise
     */
    public boolean hasUnsavedChanges();
    
    //Playback Methods
    /**
     * Responsible to updating the song state due to a change in time. Provides a
     * common mechanism for updating all notes in a song.
     * @param currentTime the current time of music playback in seconds
     */
    public void updateForTimeChange(float currentTime);
    /**
     * Handles a 'strum' event at the indicated time. Updates the note state if
     * necessary and returns a value indicating if the strum was successfull.
     * @param buttonNumbers A list of the button numbers that were held when the strum occurred
     * @param currentTime The time in seconds that the strum occurs at
     * @return true if the strum was successfull. False if not.
     */
    public boolean strumNotes(List<Integer> buttonNumbers, float currentTime);
    /**
     * Strums a single note. Updates note states if appropriate and determines if
     * the strum was successfull.
     * @param buttonNumber the number of the button that was held when the strum occurred
     * @param currentTime the time that the strum ocurred
     * @return true if the strum was successfull, false otherwise
     */
    public boolean strumNote(int buttonNumber, float currentTime);
    /**
     * Handles the releasing of a note
     * @param buttonNumber The number of the button that was released
     * @param currentTime The time that the button was released
     */
    public void releaseNote(int buttonNumber, float currentTime);
    /**
     * Attempts to 'hammer on'  (or pull off) a note
     * @param currentTime the time of the hammer on
     * @param buttonNumber the button that the user is attempting to hammer on
     * @return true if the hammer on was successfull, false otherwise
     */
    public boolean tryHammerOn(float currentTime, int buttonNumber);
    /**
     * Resets all the notes in the song to their default states
     */
    public void resetSong();
    /**
     * Determines the notes that are currently pressable or active
     * @return a collection of notes that are currently in their ActiveState
     */
    public Collection<Note> getActiveNotes();
    
    //Editing Methods
    /**
     * Gets the SongProperties object for this song. Returns the actual
     * SongProperties object, not a copy.
     * @return the SongProperties object for this song
     */
    public SongProperties getProperties();
    /**
     * Assigns a new SongProperties object to this song
     * @param properties the SongProperties to assign to this object
     */
    public void setProperties(SongProperties properties);
    /**
     * Sets whether or not this song has unsaved changes. Generally not called
     * by outside classes, used for Notes to indicate when their positions have
     * changed.
     * @param hasUnsavedChanges use true to indicate the song should be saved before closing
     */
    public void setUnsavedChanges(boolean hasUnsavedChanges);
    
    //Note Editing Methods
    /**
     * Adds a note to the song.
     * @param n the note to add to the song
     * @throws net.freetar.DuplicateNoteException thrown when attempting to add a note at the same time on the
     * same track as an existing note.
     */
    public void addNote(Note n) throws DuplicateNoteException;
    /**
     * Removes a note from the Song
     * @param n The note to remove from the song
     */
    public void removeNote(Note n);
    //public void moveNoteToTime(Note n, float time);
    //public void moveNoteToTrack(Note n, int trackNumber);
    //public void setNoteDuration(Note n, float duration);
  
    //Note finding Methods
    /**
     * Counts the number of total notes in the song
     * @return the total number of notes in the song
     */
    public int totalNotes();
    /**
     * Determines if a note is in the song or not.
     * @param n the note to check for inclusion in the song
     * @return true if the note is in the song, false otherwise
     */
    public boolean containsNote(Note n);
    /**
     * Creates a collection of all the notes in the song
     * @return a collection containing all the notes in the song
     */
    public Collection<Note> getAllNotes();
    /**
     * Creates a collection containing all the notes for the specified button.
     * Performance of this method depends on implementation.
     * @param button the button number to get the list of notes from
     * @return a Colletion containing all the notes for the indicated button
     */
    public Collection<Note> getNotesForButton(int button);
    /**
     * Creates a collection containing all the notes which would be 'pressable' at the indicated time.
     * Does not check the actual NoteState.
     * @param timeInSeconds the time you are interested in
     * @param button the button that you are interested in
     * @return a Collection of notes that would be active at the indicated time for the indicated button
     */
    public Collection<Note> getNotesPressableAt(float timeInSeconds, int button);
    /**
     * Creates a collection of the notes which would be active during the specified
     * period.
     * @param startTimeInSeconds the start of the time to check
     * @param endTimeInSeconds the end of the time to check
     * @return a Collection of notes which would be active during the specified period
     */
    public Collection<Note> getNotesBetween(float startTimeInSeconds, float endTimeInSeconds);
    /**
     * Creates a collection of the notes which would be active during the specified
     * period for the indicated button only.
     * @param startTimeInSeconds the start of the time to check
     * @param endTimeInSeconds the end of the time to check
     * @param buttonNumber the button number to check
     * @return a Collection of notes which would be active during the specified period
     */
    public Collection<Note> getNotesBetween(float startTimeInSeconds, float endTimeInSeconds, int buttonNumber);
    
    //Note factory Methods
    /**
     * Creates a note. This should be used to create notes, as implementations
     * of Song and Note may refer to eachother. Does not add the note to the song.
     * Allows the song to function as a Note Factory.
     * @param time the time for the new note
     * @param duration the length in seconds that the new note should be held
     * @param buttonNumber the button that should trigger the new note
     * @param isHammerable whether or not the note should be hammerable
     * @return the new Note
     */
    public Note createNote(float time, float duration, int buttonNumber, boolean isHammerable);
    /**
     * Creates a note at the indicated time and for the indicated button that is not
     * hammerable and has 0 duration. A convinience method. Does not add
     * the note to the song.
     * @param time the time that the new note should be pressed
     * @param buttonNumber the button number for the new note
     * @return a new Note
     */
    public Note createNote(float time, int buttonNumber);
    
    /**
     * Registers a NoteChangeListener to be notified when notes from this song change
     * state
     * @param aListener the NoteChangeListener to register
     */
    public void addNoteChangeListener(NoteChangeListener aListener);
    /**
     * Removes a previously registered NoteChangeListener from the list of
     * listeners to notify of state changes
     * @param aListener the NoteChangeListener to remove (deregister)
     */
    public void removeNoteChangeListener(NoteChangeListener aListener);
    
    /**
     * Notifies all the registered NoteChangeListeners of the note-change event.
     * @param event the NoteEvent that should be passed along to all of the registered
     * NoteChangeListeners
     */
    public void notifyListenersOfNoteChange(NoteEvent event);
    //public void notifyListenersOfNoteError();
    
    //Sections?
    //public Section[] getSections();
    //public Section getSection(int sectionNumber);
    //public Collection<Section> getSectionsIntersecting(float startTime, float endTime);
}
