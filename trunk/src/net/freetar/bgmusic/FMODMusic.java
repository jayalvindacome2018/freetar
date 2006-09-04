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
import net.freetar.util.SongUtils;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import java.util.logging.Logger;

import org.jouvieje.FmodEx.Channel;
import org.jouvieje.FmodEx.ChannelGroup;
import org.jouvieje.FmodEx.DSP;
import org.jouvieje.FmodEx.Defines.FMOD_INITFLAGS;
import org.jouvieje.FmodEx.Defines.INIT_MODES;
import org.jouvieje.FmodEx.Defines.VERSIONS;
import org.jouvieje.FmodEx.Defines.FMOD_TIMEUNIT;
import org.jouvieje.FmodEx.Defines.FMOD_MODE;
import org.jouvieje.FmodEx.Enumerations.FMOD_CHANNELINDEX;
import org.jouvieje.FmodEx.Enumerations.FMOD_DSP_CHORUS;
import org.jouvieje.FmodEx.Enumerations.FMOD_DSP_PITCHSHIFT;
import org.jouvieje.FmodEx.Enumerations.FMOD_DSP_TYPE;
import org.jouvieje.FmodEx.Enumerations.FMOD_RESULT;
import org.jouvieje.FmodEx.Exceptions.InitException;
import org.jouvieje.FmodEx.FmodEx;
import org.jouvieje.FmodEx.Init;
import org.jouvieje.FmodEx.Misc.BufferUtils;
import org.jouvieje.FmodEx.Sound;


/**
 *
 * @author Anton
 */
public class FMODMusic extends BackgroundMusic {
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.music.bgmusic.FMODMusic");
    
    private org.jouvieje.FmodEx.System system = null;
    private float originalFrequency = -1;
    
    private Sound sound = null;
    private Channel channel = null;
    private DSP dspPitchShift = null;
    private DSP muffleDSP = null;
    
    /** Creates a new instance of FMODMusic */
    public FMODMusic(File f) throws MusicException{
        try{
            initFMOD(f.getAbsolutePath());
        }catch(FmodException e){
            throw new MusicException("FMOD Failed to inialize correctly. Error: " + e.getMessage());
        }
    }
    
    public float getLength() {
        if(sound.isNull()){
            logger.warning("FMOD sound object is null");
            return -1;
        }
        
        //Create a buffer to store the result
        ByteBuffer timeBuffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);
        //Get the time in MS from FMOD
        FMOD_RESULT result = sound.getLength(timeBuffer.asIntBuffer(), FMOD_TIMEUNIT.FMOD_TIMEUNIT_MS);
        //Make sure nothing went wrong from FMOD
        try{
            if(result != FMOD_RESULT.FMOD_OK && result != FMOD_RESULT.FMOD_ERR_INVALID_HANDLE){
                errorCheck(result);
            }
        }catch(FmodException e){
            handleException(e);
            return -1;
        }
        //Read the time back from the buffer and convert to seconds
        return ((float) timeBuffer.getInt(0)) / 1000f;
    }
    
    public float getTimeInSeconds() {
        if(channel.isNull()){
            logger.warning("FMOD channel is null");
            return -1;
        }
        
        ByteBuffer timeBuffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);
        FMOD_RESULT result = channel.getPosition(timeBuffer.asIntBuffer(), FMOD_TIMEUNIT.FMOD_TIMEUNIT_MS);
        
        //Make sure nothing went wrong from FMOD
        try{
            if(result != FMOD_RESULT.FMOD_OK && result != FMOD_RESULT.FMOD_ERR_INVALID_HANDLE){
                errorCheck(result);
            }
        }catch(FmodException e){
            handleException(e);
            return -1;
        }
        //Read the time back from the buffer and convert to seconds
        return ((float) timeBuffer.getInt(0)) / 1000f;
    }
    
    protected void pauseMusic() {
        if(!channel.isNull()){
            FMOD_RESULT result = channel.setPaused(true);
            try{
                errorCheck(result);
            }catch(FmodException e){
                handleException(e);
                return;
            }
        }
    }
    
    protected void playMusic() throws BackgroundMusic.MusicException {
        if(!isChannelPlaying()){
            logger.warning("Attempted to play sound that was not playing! Restarting Channel");
            FMOD_RESULT result = system.playSound(FMOD_CHANNELINDEX.FMOD_CHANNEL_FREE, sound, false, channel);
            try{
                errorCheck(result);
            }catch(FmodException e){
                handleException(e);
            }
            return;
        }
        
        if(this.state == MusicState.PAUSED || state == MusicState.STOPPED){
            logger.fine("PLAY: resuming from pause OR stop");
            resumeMusic();
            return;
        }else{
            logger.fine("PLAY: skipping to start and replaying");
            skipTo(0);
            resumeMusic();
        }
    }
    
    protected void resumeMusic() {
        if(!isChannelPlaying()){
            logger.warning("Attempted to play sound that was not playing! Restarting Channel");
            FMOD_RESULT result = system.playSound(FMOD_CHANNELINDEX.FMOD_CHANNEL_FREE, sound, false, channel);
            try{
                errorCheck(result);
            }catch(FmodException e){
                handleException(e);
            }
            return;
        }
        
        
        if(!channel.isNull()){
            FMOD_RESULT result = channel.setPaused(false);
            try{
                errorCheck(result);
            }catch(FmodException e){
                logger.severe(e.getMessage());
            }
        }else{
            logger.warning("called on NULL channel");
        }
    }
    
    /**
     *Returns true only if the current sound is PLAYING (ie. not run out of data)
     */
    private boolean isChannelPlaying(){
        if(channel.isNull()){
            logger.warning("called on NULL channel");
            return false;
        }
        ByteBuffer buffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);
        FMOD_RESULT result = channel.isPlaying(buffer);
        try{
            if((result != FMOD_RESULT.FMOD_OK) && (result != FMOD_RESULT.FMOD_ERR_INVALID_HANDLE)){
                errorCheck(result);
            }
        }catch(FmodException e){
            handleException(e);
            return false;
        }
        return buffer.get(0) != 0;
    }
    /*
    private boolean isChannelPaused(){
        if(channel.isNull()){
            logger.warning("isPaused: called on NULL channel");
            return false;
        }
        ByteBuffer buffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);
        FMOD_RESULT result = channel.getPaused(buffer);
        try{
            if((result != FMOD_RESULT.FMOD_OK) && (result != FMOD_RESULT.FMOD_ERR_INVALID_HANDLE)){
                errorCheck(result);
            }
        }catch(FmodException e){
            handleException(e);
            return false;
        }
        return buffer.get(0) != 0;
    }
     **/
    
    protected void skipMusic(float time) throws BackgroundMusic.MusicException {
        if(time < 0 || time > getLength()){
            return;
        }
        if(!channel.isNull()){
            int milliseconds = SongUtils.convertToMillis(time);
            if(time <= getLength()){
                logger.fine("Skipping Song to: " + time);
                FMOD_RESULT result = channel.setPosition(milliseconds, FMOD_TIMEUNIT.FMOD_TIMEUNIT_MS);
                try{
                    errorCheck(result);
                }catch(FmodException e){
                    handleException(e);
                }
            }
        }
    }
    
    protected void stopMusic() {
        pauseMusic();
        try{
            skipMusic(0);
        }catch(MusicException e){
            e.printStackTrace();
            logger.severe("Exception surpressed while stopping music: " + e.getMessage());
        }
    }
    
    protected void setMusicTempoFactor(float factor) {
        logger.info("Setting Sound Frequency to: " + originalFrequency * factor);
        try{
            FMOD_RESULT result = channel.setFrequency(originalFrequency * factor);
            errorCheck(result);
            
            result = dspPitchShift.setParameter(0, 1 / factor);  //Parameter 0 is the DSP_PITCHSHIFT parameter
            errorCheck(result);
            
            if(factor != 1.0f){
                dspPitchShift.setBypass(false);
                dspPitchShift.setActive(true);
            }else{
                dspPitchShift.setBypass(true);
                dspPitchShift.setActive(false);
            }
            
            
            result = dspPitchShift.setActive(true);
            errorCheck(result);
        }catch(FmodException e){
            handleException(e);
        }
    }
    
    private void errorCheck(FMOD_RESULT result) throws FmodException{
        if(result != FMOD_RESULT.FMOD_OK) {
            logger.severe("FMOD error! (" + result.asInt() + ") " + FmodEx.FMOD_ErrorString(result));
            throw new FmodException(result);
        }
    }
    
    public void initFMOD(String fileName) throws MusicException, FmodException{
        try {
            Init.loadLibraries(INIT_MODES.INIT_FMOD_EX);
        } catch(InitException e) {
            e.printStackTrace();
            logger.severe("NativeFmodEx error! " + e.getMessage());
            throw new MusicException("Error Loading FMOD libraries.");
        }
        
        /*
         * Checking NativeFmodEx version
         */
        if(VERSIONS.NATIVEFMODEX_LIBRARY_VERSION != VERSIONS.NATIVEFMODEX_JAR_VERSION) {
            logger.severe("Error!  NativeFmodEx library version (" +
                    VERSIONS.NATIVEFMODEX_LIBRARY_VERSION +
                    ") is different to jar version (" +
                    VERSIONS.NATIVEFMODEX_JAR_VERSION + ")\n");
            throw new MusicException("FMOD Error - Your .DLL version does not match the .JAR version");
        }
        
        /*==================================================*/
        system = new org.jouvieje.FmodEx.System();
        sound = new Sound();
        channel = new Channel();
        
        FMOD_RESULT result;
        int version;
        
        ByteBuffer buffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);
        
        //Create a System object and initialize.
        result = FmodEx.System_Create(system);
        errorCheck(result);
        
        //Check the version of this FMOD versus the library supplied
        result = system.getVersion(buffer.asIntBuffer());
        errorCheck(result);
        version = buffer.getInt(0);
        
        if(version < VERSIONS.FMOD_VERSION) {
            logger.severe("Error!  You are using an old version of FMOD " +
                    version + " This program requires " + VERSIONS.FMOD_VERSION);
            throw new MusicException("FMOD Error - Old version of FMOD detected. This program requires " + VERSIONS.FMOD_VERSION);
        }
        
        //Init the FMOD System
        result = system.init(1, FMOD_INITFLAGS.FMOD_INIT_NORMAL, null);
        errorCheck(result);
        
        //Setup the pitch-shifting DSP
        dspPitchShift = new DSP();
        result = system.createDSPByType(FMOD_DSP_TYPE.FMOD_DSP_TYPE_PITCHSHIFT, dspPitchShift);
        errorCheck(result);
        result = dspPitchShift.setParameter(0, 1.0f);   //Parameter 0 is the DSP_PITCHSHIFT parameter
        errorCheck(result);
        //dspPitchShift.setActive(false);
        dspPitchShift.setBypass(true);
        result = system.addDSP(dspPitchShift);
        errorCheck(result);
        
        //Set up the muffling DSP
        muffleDSP = new DSP();
        result = system.createDSPByType(FMOD_DSP_TYPE.FMOD_DSP_TYPE_LOWPASS, muffleDSP);
        errorCheck(result);
        result = muffleDSP.setParameter(0, 500);
        errorCheck(result);
        //muffleDSP.setActive(false);
        muffleDSP.setBypass(true);
        result = system.addDSP(muffleDSP);
        errorCheck(result);
        
        //Load the stream to play
        result = system.createStream(fileName, FMOD_MODE.FMOD_SOFTWARE | FMOD_MODE.FMOD_LOOP_OFF | FMOD_MODE.FMOD_2D | FMOD_MODE.FMOD_ACCURATETIME, null, sound);
        errorCheck(result);
        
        
        //Play the sound
        result = system.playSound(FMOD_CHANNELINDEX.FMOD_CHANNEL_FREE, sound, false, channel);
        errorCheck(result);
        pauseMusic();
        
        //Get the original frequecy of the file to play back
        FloatBuffer frequencyBuffer = BufferUtils.newFloatBuffer(BufferUtils.SIZEOF_FLOAT);
        result = channel.getFrequency(frequencyBuffer);
        try{
            errorCheck(result);
        }catch(FmodException e){
            handleException(e);
            throw e;
        }
        originalFrequency = frequencyBuffer.get();
    }
    
    public void cleanup(){
        try{
            FMOD_RESULT result = sound.release();
            errorCheck(result);
            result = system.close();
            errorCheck(result);
            result = system.release();
            errorCheck(result);
        }catch(FmodException ex){
            handleException(ex);
            return;
        }
    }
    
    private class FmodException extends Exception{
        public FmodException(FMOD_RESULT result){
            super("FMOD Error #" + result.asInt() + " - " + FmodEx.FMOD_ErrorString(result));
        }
    }
    
    private void handleException(Exception e){
        e.printStackTrace();
        logger.severe(e.getMessage());
    }
    
    public void beginErrorSilence(){
        try{
            FMOD_RESULT result = muffleDSP.setBypass(false);
            errorCheck(result);
        }catch(FmodException ex){
            handleException(ex);
        }
        
    }
    
    public void endErrorSilence(){
        //muffleDSP.setBypass(true);
        try{
            FMOD_RESULT result = muffleDSP.setBypass(true);
            errorCheck(result);
        }catch(FmodException ex){
            handleException(ex);
        }
    }
}
