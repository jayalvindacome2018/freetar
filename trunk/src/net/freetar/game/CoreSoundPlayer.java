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

package net.freetar.game;

import net.freetar.util.DebugHandler;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Anton
 */
public class CoreSoundPlayer {
    private Logger logger = DebugHandler.getLogger("com.astruyk.freetar.game.SoundManager");
    private Map<String, Clip> loadedSounds;
    
    private static CoreSoundPlayer instance = new CoreSoundPlayer();
    
    /** Creates a new instance of SoundManager */
    private CoreSoundPlayer() {
        loadedSounds = new HashMap<String, Clip>();
    }
    
    public void loadSound(URL soundFile, String soundName){
        if(soundFile != null){
            AudioInputStream audioStream = null;
            try {
                audioStream = AudioSystem.getAudioInputStream(soundFile);
                DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());
                Clip soundClip = (Clip) AudioSystem.getLine(info);
                soundClip.open(audioStream);
                
                loadedSounds.put(soundName, soundClip);
            } catch (Exception ex){
                ex.printStackTrace();
            } finally {
                if(audioStream != null){
                    try {
                        audioStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
        }
    }
    
    public void playSound(String soundName){
        if(loadedSounds.containsKey(soundName)){
            Clip clip = loadedSounds.get(soundName);
            if(clip.isActive()){
                clip.stop();
            }
            clip.setMicrosecondPosition(0);
            clip.start();
        }else{
            logger.warning("Sound '" + soundName + "' not played - not yet loaded.");
        }
    }
    
    public static CoreSoundPlayer getInstance(){
        return instance;
    }
    
    //TODO fix this hack in a nicer way - don't hard-code the sound numbers in a place where they are not loaded
    public static void playRandomErrorSound(){
        Random r = new Random();
        int randomIndex = (int) (r.nextFloat() * 5f);
        CoreSoundPlayer.getInstance().playSound("MissedNote" + randomIndex);
    }
}
