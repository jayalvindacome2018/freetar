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
 *
 * @author Anton
 */
public interface Song{
    public static final int TRACKS = 5;
    public static final float DEFAULT_BPM = 120.0f * 8.0f;
    
    //Information Methods
    public boolean hasUnsavedChanges();
    
    //Playback Methods
    public void updateForTimeChange(float currentTime);
    public boolean strumNotes(List<Integer> buttonNumbers, float currentTime);
    public boolean strumNote(int buttonNumber, float currentTime);
    public void releaseNote(int buttonNumber, float currentTime);
    public boolean tryHammerOn(float currentTime, int buttonNumber);
    public void resetSong();
    public Collection<Note> getActiveNotes();
    
    //Editing Methods
    public SongProperties getProperties();
    public void setProperties(SongProperties properties);
    public void setUnsavedChanges(boolean hasUnsavedChanges);
    
    //Note Editing Methods
    public void addNote(Note n) throws DuplicateNoteException;
    public void removeNote(Note n);
    //public void moveNoteToTime(Note n, float time);
    //public void moveNoteToTrack(Note n, int trackNumber);
    //public void setNoteDuration(Note n, float duration);
  
    //Note finding Methods
    public int totalNotes();
    public boolean containsNote(Note n);
    public Collection<Note> getAllNotes();
    public Collection<Note> getNotesForButton(int button);
    public Collection<Note> getNotesPressableAt(float timeInSeconds, int button);
    public Collection<Note> getNotesBetween(float startTimeInSeconds, float endTimeInSeconds);
    public Collection<Note> getNotesBetween(float startTimeInSeconds, float endTimeInSeconds, int buttonNumber);
    
    //Note factory Methods
    public Note createNote(float time, float duration, int buttonNumber, boolean isHammerable);
    public Note createNote(float time, int buttonNumber);
    
    public void addNoteChangeListener(NoteChangeListener aListener);
    public void removeNoteChangeListener(NoteChangeListener aListener);
    
    public void notifyListenersOfNoteChange(NoteEvent event);
    //public void notifyListenersOfNoteError();
    
    //Sections?
    //public Section[] getSections();
    //public Section getSection(int sectionNumber);
    //public Collection<Section> getSectionsIntersecting(float startTime, float endTime);
}
