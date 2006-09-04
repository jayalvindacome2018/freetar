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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.swing.Timer;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import net.freetar.SimpleNote;

/**
 * Abstraction of a gamepad (or any input device with buttons). Basically just a
 * wrapper of the JInput controller type with some different methods to make
 * polling and querying the status of a device easier.
 *
 * Create a Gamepad, register listeners, and call <code>startPolling()</code> to use.
 *
 * @author Anton Struyk
 */
public class GamepadPoller {
    public static final int POLL_TIME = 1;  //Time to wait between checking for changes
    private static final Logger logger = DebugHandler.getLogger("com.astruy.music.input.ControllerPoller");
    
    private Gamepad gamepad;
    private Timer pollTimer;
    private Map<Button, Boolean> pressedLastUpdate = new HashMap<Button, Boolean>();
    private List<GamepadButtonListener> observers;
    
    private GamepadPoller(){}
    
    public GamepadPoller(String gamepadName) throws
            ControllerNotFoundException, ControllerNotSupportedException {
        initForController(GamepadManager.getGamepadManager().getSupportedGamepad(gamepadName));
    }
    
    public GamepadPoller(Gamepad gamepad) throws
            ControllerNotSupportedException{
        initForController(gamepad);
    }
    
    private void initForController(Gamepad gamepad){
        this.gamepad = gamepad;
        this.observers = new ArrayList<GamepadButtonListener>();
        
        // Get the last state of all the components of the controller
        gamepad.poll();
        pressedLastUpdate.clear();
        for(Button currentButton : gamepad.getButtons()){
            pressedLastUpdate.put(currentButton, currentButton.isPressed());
        }
    }
    
    public synchronized void pollController() {
        // Recheck the states of the components of the watched controller
        gamepad.poll();
        for (Button currentButton : pressedLastUpdate.keySet()) {
            if (pressedLastUpdate.get(currentButton) != currentButton.isPressed()) {
                ButtonEvent.EventType eventType = null;
                if (currentButton.isPressed()){
                    eventType = ButtonEvent.EventType.BUTTON_PRESSED;
                }else{
                    eventType = ButtonEvent.EventType.BUTTON_RELEASED;
                }
                ButtonEvent evt = new ButtonEvent(
                        gamepad,
                        currentButton,
                        eventType);
                for(GamepadButtonListener observer : observers){
                    observer.buttonActionTriggered(evt);
                }
                pressedLastUpdate.put(currentButton, currentButton.isPressed());
            }
        }
    }
    
    public boolean isRunning() {
        return pollTimer.isRunning();
    }
    
    //Synchronized b/c of access by polling thread
    public void startPolling() {
        logger.info("Starting PollTimer");
        if(pollTimer == null){
            pollTimer = new Timer(POLL_TIME, new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    pollController();
                }
            });
            pollTimer.start();
        }else if(!pollTimer.isRunning()){
            pollTimer.start();
        }
    }
    
    public void stopPolling() {
        if(pollTimer != null && pollTimer.isRunning()){
            pollTimer.stop();
        }
        pressedLastUpdate.clear();
    }
    
    public void addButtonPressListener(GamepadButtonListener newObserver){
        if(!observers.contains(newObserver)){
            observers.add(newObserver);
        }
    }
    
    public void removeButtonPressListener(GamepadButtonListener observer){
        observers.remove(observer);
    }
    
    public List<GamepadButtonListener> getGamepadListeners(){
        return new ArrayList<GamepadButtonListener>(observers);
    }
    
    public String toString(){
        return "Poller watching Controller " + gamepad.getName();
    }
}
