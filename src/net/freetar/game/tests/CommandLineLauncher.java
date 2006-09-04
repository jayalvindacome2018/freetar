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

package net.freetar.game.tests;

import com.jme.system.GameSettings;
import com.jme.system.PropertiesIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import net.freetar.Song;
import net.freetar.bgmusic.BackgroundMusic;
import net.freetar.bgmusic.FakeMusic;
import net.freetar.game.FreetarGame;
import net.freetar.game.FreetarSimpleGame;
import net.freetar.input.ButtonConfig;
import net.freetar.input.ControllerNotFoundException;
import net.freetar.input.ControllerNotSupportedException;
import net.freetar.io.FileFormatException;
import net.freetar.io.IncorrectVersionException;
import net.freetar.util.SongUtils;
import net.freetar.util.UnsupportedVersionException;

/**
 *
 * @author Anton
 */
public class CommandLineLauncher {
    
    //Usage:
    //CommandLineLauncher   <songFileName> <musicFileString> <controllerFileName>
    public static void main(String[] args){
        
        File songFile = new File("default.sng");
        File musicFile = new File("default.mp3");
        File configFile = new File("controller.ini");

        Song song = null;
        try {
            song = SongUtils.loadFromFile(songFile);
        } catch (UnsupportedVersionException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (FileFormatException ex) {
            ex.printStackTrace();
        } catch (IncorrectVersionException ex) {
            ex.printStackTrace();
        }
        
        BackgroundMusic music = null;
        try {
            music = BackgroundMusic.loadMusicFrom(musicFile);
        } catch (BackgroundMusic.MusicException ex) {
            ex.printStackTrace();
        }
        
        ButtonConfig buttonConfig = null;
        BufferedReader in =  null;
        try{
            in = new BufferedReader(new FileReader(configFile));
            buttonConfig = ButtonConfig.createButtonConfigFrom(in);
        }catch(IOException ex){
            ex.printStackTrace();
        }catch(ControllerNotFoundException ex){
            ex.printStackTrace();
        }catch(ControllerNotSupportedException ex){
            ex.printStackTrace();
        } finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        try {
            //PreferencesGameSettings settings = new PreferencesGameSettings();
            FreetarGame.run(song, music, buttonConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
