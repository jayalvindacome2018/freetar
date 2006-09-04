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

import net.java.games.input.Component;
import net.java.games.input.Controller;

/**
 *
 * @author Anton
 */
public class ButtonEvent {
    public enum EventType {
        BUTTON_RELEASED,
        BUTTON_PRESSED
    };
    
    private long eventTime;
    private EventType type;
    private Button button;
    private Gamepad gamepad;
    
    /** Creates a new instance of ButtonEvent */
    public ButtonEvent(Gamepad gamepad, Button button, EventType type) {
        eventTime = System.currentTimeMillis();
        this.gamepad = gamepad;
        this.button = button;
        this.type = type;
    }
    
    public Button getButton(){
        return button;
    }
    
    public Gamepad getGamepad(){
        return gamepad;
    }
    
    public long getTime(){
        return eventTime;
    }
    
    public EventType getEventType(){
        return type;
    }
    
    public String toString(){
        return "ControllerChangedEvent (" + gamepad + ", " + button + ", " + type + ")";
    }
}
