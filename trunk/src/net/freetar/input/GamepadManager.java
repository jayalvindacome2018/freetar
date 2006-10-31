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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author Anton
 */
public class GamepadManager {
    private static final Logger logger = DebugHandler.getLogger("GamepadManager");

    private static final GamepadManager manager = new GamepadManager();
    private List<Gamepad> gamepads;
    private static int unidentified = 0;
    
    private GamepadManager() {
        //Builds a list of all the available gamepads on the system
        gamepads = new ArrayList<Gamepad>();
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        
        
        for(Controller c : controllers){
            try {
                gamepads.add(new Gamepad(c));
            } catch (ControllerNotSupportedException ex) {
                logger.warning("" + ex);
            }
        }
    }
    
    public List<Gamepad> getSupportedGamepads(){
        return gamepads;
    }
    
    public Gamepad getSupportedGamepad(String name) throws ControllerNotFoundException{
        for(Gamepad g : gamepads){
            if(g.getName().trim().equals(name.trim())){
                return g;
            }
        }
        throw new ControllerNotFoundException(name);
    }
    
    public static GamepadManager getGamepadManager(){
        return manager;
    }
    
    public static String getNewName() {
        unidentified++;
        return "Generic Gamepad "+unidentified;
    }
}
