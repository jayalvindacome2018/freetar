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

package net.freetar.bgmusic;

import net.freetar.bgmusic.BackgroundMusic.MusicException;
import net.freetar.bgmusic.BackgroundMusic.MusicState;
import net.freetar.util.DebugHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

/**
 *
 * @author Temp
 */
public class MidiMusic extends BackgroundMusic {
    private static final float MICROSECONDS_IN_SECOND = 1000000;
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.freetar.MidiMusic");
    
    private Sequence midiSequence;
    private Sequencer sequencer;
    private long pauseTime;
    
    public MidiMusic(URL midiLocation) throws MusicException{
        if(midiLocation == null){
            throw new MusicException("NULL passed in as URL for MIDI");
        }
        
        try {
            midiSequence = MidiSystem.getSequence(midiLocation);
        } catch (MalformedURLException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Cannot load MIDI from Malformed URL:" + midiLocation);
        } catch (InvalidMidiDataException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Cannot load MIDI from " + midiLocation + ": Invalid MIDI Data");
        } catch (IOException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Cannot load MIDI from " + midiLocation + ": I/O Exception");
        }
        
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(midiSequence);
        } catch (MidiUnavailableException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("MIDI sequencer unavailable");
        } catch (InvalidMidiDataException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Invalid MIDI data");
        }
    }
    
    public MidiMusic(File midiLocation) throws MusicException{
        if(!midiLocation.exists()){
            throw new MusicException("File '" + midiLocation + "' does not exist. Cannot load music");
        }
        
        try {
            midiSequence = MidiSystem.getSequence(midiLocation);
        } catch (MalformedURLException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Cannot load MIDI from Malformed URL:" + midiLocation);
        } catch (InvalidMidiDataException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Cannot load MIDI from " + midiLocation + ": Invalid MIDI Data");
        } catch (IOException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Cannot load MIDI from " + midiLocation + ": I/O Exception");
        }
        
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(midiSequence);
        } catch (MidiUnavailableException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("MIDI sequencer unavailable");
        } catch (InvalidMidiDataException ex) {
            DebugHandler.logException(logger, ex);
            throw new MusicException("Invalid MIDI data");
        }
    }
    
    public void playMusic() throws MusicException{
        if(sequencer.isOpen()){
            sequencer.start();
        }else{
            throw new MusicException("MIDI Sequencer Closed - Cannot play Music");
        }
    }
    
    public void stopMusic() {
        sequencer.stop();
        sequencer.setMicrosecondPosition(0);
    }
    
    public void pauseMusic() {
        pauseTime = sequencer.getMicrosecondPosition();
        sequencer.stop();
    }
    
    public void resumeMusic(){
        sequencer.setMicrosecondPosition(pauseTime);
        sequencer.start();
    }
    
    public void skipMusic(float seconds) {
        boolean resumeAfterSkip = this.state == MusicState.PLAYING;
        this.pauseMusic();
        long newMicrosecondPosition = (long) (seconds *  MICROSECONDS_IN_SECOND);
        if(newMicrosecondPosition >= sequencer.getMicrosecondLength() - 1000){
            sequencer.setMicrosecondPosition(sequencer.getMicrosecondLength());
            resumeAfterSkip = false;
        }else if(newMicrosecondPosition <= 0){
            sequencer.setMicrosecondPosition(0);
        }else{
            sequencer.setMicrosecondPosition((long) (seconds * 1000000f));
        }
        
        //Update the paused time if necessary so that it will be
        //resumed correctly
        pauseTime = sequencer.getMicrosecondPosition();
        if(resumeAfterSkip){
            this.resumeMusic();
        }
    }
    
    public long getTimeInMicroseconds() {
        return sequencer.getMicrosecondPosition();
    }
    
    public float getTimeInSeconds() {
        return ((float) getTimeInMicroseconds()) / 1000000f;
    }
    
    public float getLength(){
        return ((float) sequencer.getMicrosecondLength() / 1000000f);
    }
    
    public void setMusicTempoFactor(float factor){
        sequencer.setTempoFactor(factor);
    }
}
