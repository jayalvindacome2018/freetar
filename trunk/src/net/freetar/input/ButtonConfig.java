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

package net.freetar.input;

import net.freetar.util.DebugHandler;
import net.freetar.util.FileUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import net.java.games.input.Component;
import net.java.games.input.Controller;

public class ButtonConfig{
    public static final Logger logger = DebugHandler.getLogger("com.astruyk.music.input.ButtonConfig");
    
    public static enum Action {
        PAUSE,
        SPECIAL,
        TRACK_0,
        TRACK_1,
        TRACK_2,
        TRACK_3,
        TRACK_4,
        STRUM_UP,
        STRUM_DOWN
    };
    private boolean strumRequired;
    
    private Gamepad gamepad;
    private Map<Action, Button> map;  //Mapping of the Action->its assigned button
    private boolean lefty;
    
    public ButtonConfig(Gamepad gamepad) {
        this.gamepad = gamepad;
        this.strumRequired = true;
        map = new EnumMap<Action, Button>(Action.class);
        lefty=false;
    }
    
    public void assign(Button button, Action action) {
        map.put(action, button);
    }
    
    public void setStrumRequired(boolean requireStrum){
        this.strumRequired = requireStrum;
    }
    
    public boolean isStrumRequired(){
        return strumRequired;
    }
    
    public Button getButtonFor(Action assignment) {
        return map.get(assignment);
    }
    
    public Gamepad getGamepad(){
        return gamepad;
    }
    
    public Action getAssignmentFor(Button button){
        for(Action b : map.keySet()){
            if(map.get(b) == button){
                return b;
            }
        }
        return null;
    }
    
    public String toString() {
        return "Button Configuration " + map;
    }
    
    public static ButtonConfig createButtonConfigFrom(BufferedReader in) throws IOException, ControllerNotFoundException, ControllerNotSupportedException{
        HashMap<String, String> properties = new HashMap<String, String>();
        
        String readLine = null;
        while((readLine = FileUtils.readLine(in)) != null){
            String key = readLine.split(":=")[0];
            String value = readLine.split(":=")[1];
            properties.put(key, value);
        }
        
        //Read The Name Of The Controller
        String gamepadName = properties.get("gamepadName");
        ButtonConfig returnConfig = null;
        try {
            Gamepad gamepad = GamepadManager.getGamepadManager().getSupportedGamepad(gamepadName);
            returnConfig = new ButtonConfig(gamepad);
        } catch (ControllerNotFoundException ex) {
            throw ex;
        }
        returnConfig.setStrumRequired(Boolean.valueOf(properties.get("requireStrum")));
        
        for(Action currentAction : Action.values()){
            String buttonName = properties.get(currentAction + "");
            boolean componentFound = false;
            for(Button currentButton : returnConfig.getGamepad().getButtons()){
                if(currentButton.getName().equalsIgnoreCase(buttonName)){
                    returnConfig.assign(currentButton, currentAction);
                    componentFound = true;
                    break;
                }
            }
            
            if(!componentFound){
                throw new ControllerNotFoundException(gamepadName + " with subcomponent " + buttonName);
            }
        }
        
        return returnConfig;
    }
    
    public void saveButtonConfigTo(BufferedWriter out) throws IOException{
        FileUtils.writeLine("//Button Configuration", out);
        FileUtils.writeLine("gamepadName:=" + gamepad.getName(), out);
        FileUtils.writeLine("requireStrum:=" + isStrumRequired() + "", out);
        for(Action action : Action.values()){
            FileUtils.writeLine(action + ":=" + getButtonFor(action).getName(), out);
        }
    }
    
    public boolean isButtonPressedForTrack(int buttonNumber){
        return this.getButtonFor(getActionForTrack(buttonNumber)).isPressed();
    }
    
    public Action getActionForTrack(int buttonNumber){
        if(buttonNumber == 0){
            return Action.TRACK_0;
        }else if(buttonNumber == 1){
            return Action.TRACK_1;
        }else if(buttonNumber == 2){
            return Action.TRACK_2;
        }else if(buttonNumber == 3){
            return Action.TRACK_3;
        }else if(buttonNumber == 4){
            return Action.TRACK_4;
        }
        return null;
    }
    
    public void setLefty(boolean val) {
        this.lefty = val;
    }
    
    public boolean getLefty() {
        return this.lefty;
    }
}
