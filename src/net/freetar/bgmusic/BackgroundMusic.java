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

import net.freetar.util.DebugHandler;
import net.freetar.util.FileUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 * A common interface for the methods required to play the background music in
 * the game.
 *
 * TODO add callback for song finished playing
 * TODO add methods to query song status e.g. isFinished()
 *
 * @author Anton Struyk
 */
public abstract class BackgroundMusic {
    public static class MusicException extends Exception{
        private static final long serialVersionUID = 1;
        public MusicException(){
            super();
        }
        public MusicException(String message){
            super(message);
        }
    }
    
    public static BackgroundMusic loadMusicFrom(File f) throws MusicException {
        String ext = FileUtils.getExtension(f);
        if(ext == null){
            throw new MusicException("Unable to Determine Filetype - No Extension");
        }
        if(ext.equalsIgnoreCase("midi") ||
                ext.equalsIgnoreCase("mid")){
            return new MidiMusic(f);
        }else if(ext.equalsIgnoreCase("MP3") ||
                ext.equalsIgnoreCase("OGG") ||
                ext.equalsIgnoreCase("WAV") ||
                ext.equalsIgnoreCase("WMA") ||
                ext.equalsIgnoreCase("FLAC")){
            return new FMODMusic(f);
        }
        throw new MusicException("FILETYPE NOT SUPPORTED");
    }
    
    public static enum MusicState {PAUSED, PLAYING, STOPPED, FINISHED};
    
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.music.BackgroundMusic");
    private static final long serialVersionUID = 1;
    private static final int CALLBACK_MILLISECONDS = 1;
    
    protected MusicState state;                         //The current state of the music
    private Collection<MusicListener> musicListeners;   //Will be notified of state-change events
    private Timer callbackTimer;                        //The thread used to control update callbacks
    
    protected BackgroundMusic(){
        state = MusicState.STOPPED;
        musicListeners = new ArrayList<MusicListener>();
        callbackTimer = new Timer(CALLBACK_MILLISECONDS, new ActionListener(){
            public void actionPerformed(ActionEvent e){
                notifyListenersOfTimeChange();
            }
        });
        callbackTimer.stop();
    }
    
    /**
     * Returns the total playtime time of the song
     *
     * @return the total playtime of the song
     */
    public abstract float getLength();
    
    /**
     * Finds the play-time that has elapsed so far in the song. Time where the
     * song is paused does not count.
     *
     * @return the time that has elapsed so far in the song
     */
    public abstract float getTimeInSeconds();
    
    /**
     * Pauses the playback of the background music. The song remains paused
     * until the 'resume' method is called.
     */
    public void pause() throws MusicException{
        if(state == MusicState.PLAYING){
            pauseMusic();
            //Notify the listeners that the song has been paused
            changeState(MusicState.PAUSED);
        }else if(state == MusicState.PAUSED){
            resume();
        }else if(state == MusicState.STOPPED){
            play();
        }
    }
    
    protected abstract void pauseMusic();
    
    /**
     * Begins playback of the background music. If the music is already playing, nothing happens.
     */
    public void play() throws MusicException{
        if(state == MusicState.PAUSED){
            resumeMusic();
            changeState(MusicState.PLAYING);
        }else{
            if(state != MusicState.PLAYING){
                changeState(MusicState.PLAYING);
            }
            stopMusic();
            playMusic();
        }
    }
    
    protected abstract void playMusic() throws MusicException;
    
    /**
     * Resumes play from the paused state. If the song is STOPPED then nothing
     * happens.
     */
    public void resume(){
        if(state == MusicState.PAUSED){
            resumeMusic();
            changeState(MusicState.PLAYING);
        }
    }
    
    protected abstract void resumeMusic();
    
    /**
     * Skips the song to the indicated time and continues playing (if unpaused).
     * If the song is stopped or paused, playback will resume at the new time.
     *
     * @param time
     *            the time to skip to
     */
    public void skipTo(float time) throws MusicException{
        skipMusic(time);
        notifyListenersOfTimeChange();
    };
    
    protected abstract void skipMusic(float time) throws MusicException;
    
    /**
     * Stops playback of the background music. If play is pressed again
     * afterwords, the song will restart from the begining.
     */
    public void stop(){
        if(state != MusicState.STOPPED){
            stopMusic();
            changeState(MusicState.STOPPED);
        }
    }
    
    protected abstract void stopMusic();
    
    public void addMusicListener(MusicListener ml){
        if(!musicListeners.contains(ml)){
            musicListeners.add(ml);
        }
    }
    
    public void removeMusicListener(MusicListener ml){
        musicListeners.remove(ml);
    }
    
    protected void changeState(MusicState newState){
        MusicState lastState = state;
        if(lastState != newState){
            logger.finest("Changing state change from " + lastState + " to " + newState);
            state = newState;
            
            if(newState == MusicState.PLAYING){
                callbackTimer.start();
            }else if(newState == MusicState.STOPPED ||
                    newState == MusicState.PAUSED ||
                    newState == MusicState.FINISHED){
                //Stop the timer
                callbackTimer.stop();
                //Squeeze in any last minute time-check
                notifyListenersOfTimeChange();
                
            }
            
            logger.finest("Notifying listeners of state-change");
            for(MusicListener ml : musicListeners){
                ml.stateChanged(lastState, newState);
            }
            
        }else{
            logger.finest("Attempting to change " + lastState + " to " + newState + " - Already in final state.");
        }
    }
    
    protected void notifyListenersOfTimeChange(){
        logger.finest("Notifying Music Listeners of time change");
        for(MusicListener ml : musicListeners){
            ml.musicTimeChanged(this.getTimeInSeconds());
        }
    }
    
    public void setTempoFactor(float factor){
        setMusicTempoFactor(factor);
        
        for(MusicListener ml : musicListeners){
            ml.tempoChanged(factor);
        }
    }
    
    protected abstract void setMusicTempoFactor(float factor);
    
    public MusicState getState(){
        return state;
    }
    
    public void beginErrorSilence(){}
    public void endErrorSilence(){}
    
    public void cleanup(){}
    public void init(){}
}
