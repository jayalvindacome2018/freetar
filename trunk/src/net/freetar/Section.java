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
import java.util.HashSet;

/**
 *
 * @author Anton
 */
public class Section {
    private float beatsPerSecond = 120.0f / 60.0f;
    private float offset = 0;
    private int beatsPerMeasure = 8;
    private float startTime = 0;
    private float endTime = 0;
    private Track[] tracks;

    public Section() {
        tracks = new Track[Song.TRACKS];
        for(int i = 0; i < tracks.length; i++){
            tracks[i] = new Track();
        }
    }
    
    public void setBeatsPerSecond(float beatsPerSecond){
        this.beatsPerSecond = beatsPerSecond;
    }
    public float getBeatsPerSecond(){
        return beatsPerSecond;
    }
    
    public void setOffset(float offset){
        this.offset = offset;
    }
    public float getOffset(){
        return offset;
    }
    
    public void setBeatsPerMeasure(int beatsPerMeasure){
        this.beatsPerMeasure = beatsPerMeasure;
    }
    public int getBeatsPerMeasure(){
        return beatsPerMeasure;
    }
    
    public void addNote(Note n) throws DuplicateNoteException{
        tracks[n.getButtonNumber()].addNote(n);
    }
    
    public void removeNote(Note n){
        tracks[n.getButtonNumber()].removeNote(n);
    }
    
    public Collection<Note> getAllNotes(){
        HashSet<Note> notes = new HashSet<Note>();
        for(Track t : tracks){
            notes.addAll(t.getNotes());
        }
        return notes;
    }
    
    public int totalNotes(){
        int sum = 0;
        for(Track t : tracks){
            sum += t.getNumberOfNotes();
        }
        return sum;
    }
    
    public Collection<Note> getNotesBetween(float startTime, float endTime, int buttonNumber){
        return tracks[buttonNumber].getNotesBetween(startTime, endTime);
    }
    
    public void buttonPressed(final int buttonNumber, final float currentTime){
        tracks[buttonNumber].buttonPressed(currentTime);
    }
    
    public void buttonReleased(final int buttonNumber, final float currentTime){
        tracks[buttonNumber].buttonReleased(currentTime);
    }
    
    public void updateForTimeChange(final float currentTime, final float allowableError){
        for(Track t: tracks){
            t.updateForTimeChange(currentTime, allowableError);
        }
    }
    
    public void resetNotes(){
        for(Track t : tracks){
            t.resetNotes();
        }
    }
    
    public boolean containsNote(Note n){
        return tracks[n.getButtonNumber()].containsNote(n);
    }
}
