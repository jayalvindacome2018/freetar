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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author Anton
 */
public class Gamepad {
    private static final Logger logger = DebugHandler.getLogger("Gamepad");

    private Controller controller;
    private Button[] buttons;
    
    public Gamepad(Controller controller)
    throws ControllerNotSupportedException {
        initForController(controller);
    }
    
    private void initForController(Controller controller)
    throws ControllerNotSupportedException{
        if(controller == null){
            throw new ControllerNotSupportedException(controller, "NULL Controller");
        }
        this.controller = controller;
        
        //Get someplace to store the buttons
        List<Button> supportedButtons = new ArrayList<Button>();
        logger.info("Checking Components of Controller - " + controller);
        //Go through the controller and get all the components that are supported
        Component[] components = controller.getComponents();
        for(Component currentComponent : components){
            if(currentComponent.getIdentifier() instanceof Component.Identifier.Button){
                Button newButton = new NormalButton(currentComponent);
                logger.info("Component : " + currentComponent + " is a BUTTON. Adding new button - " + newButton + " - to " + this);
                supportedButtons.add(newButton);
            }else if(currentComponent.getIdentifier() instanceof Component.Identifier.Key){
                Button newButton = new KeyButton(currentComponent);
                logger.info("Component " + currentComponent + " is a KEY. Adding new button - " + newButton + " - to " + this);
                supportedButtons.add(newButton);
            }else if(currentComponent.getIdentifier() instanceof Component.Identifier.Axis){
                logger.info("Component " + currentComponent + " is an axis.");
                if(currentComponent.getIdentifier() == Component.Identifier.Axis.POV){
                    Button[] newButtons = POVButton.getButtonsFrom(currentComponent);
                    logger.info("Component " + currentComponent + " is a POV. Adding new buttons - " + newButtons + " - to " + this);
                    for(Button b : newButtons){
                        supportedButtons.add(b);
                    }
                }else if(!currentComponent.isRelative() && currentComponent.isAnalog()){
                    Button[] newButtons = AbsoluteAnalogButton.getButtonsFrom(currentComponent);
                    logger.info("Component " + currentComponent + " is a an ABSOLUTE ANALOG axis. Adding new buttons - " + newButtons + " - to " + this);
                    for(Button b : newButtons){
                        supportedButtons.add(b);
                    }
                }else{
                    logger.info("Component " + currentComponent + " is an unsupported Axis type.");
                }
            }else{
                logger.info("Component " + currentComponent + " of another type.\n" +
                        "(ID: " + currentComponent.getIdentifier() + ", class: " + currentComponent.getIdentifier().getClass());
            }
        }
        //Check that there's enough buttons for all the game's actions
        if(supportedButtons.size() < ButtonConfig.Action.values().length){
            logger.warning("Controller " + controller + " has too few buttons (" + supportedButtons.size() + ")");
            throw new ControllerNotSupportedException(controller, "Too Few Supported Buttons");
        }
        
        this.buttons = new Button[supportedButtons.size()];
        for(int i = 0; i < supportedButtons.size(); i++){
            this.buttons[i] = supportedButtons.get(i);
        }
    }
    
    public Button[] getButtons(){
        return buttons;
    }
    
    public String getName(){
        return controller.getName();
    }
    
    public void poll(){
        try{
            controller.poll();
        }catch(NullPointerException ex){
            logger.severe("ERROR POLLING DEVICE");
        }
    }
    
    public String toString(){
        return getName();
    }
}
