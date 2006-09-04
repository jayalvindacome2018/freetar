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
import net.java.games.input.Component.Identifier;
/**
 *
 * @author Anton
 */
public class POVButton extends AbstractButton{
    private enum Direction{
        UP (Component.POV.UP),
        DOWN (Component.POV.DOWN),
        LEFT (Component.POV.LEFT),
        RIGHT (Component.POV.RIGHT),
        UP_LEFT (Component.POV.UP_LEFT),
        DOWN_LEFT (Component.POV.DOWN_LEFT), 
        UP_RIGHT (Component.POV.UP_RIGHT),
        DOWN_RIGHT (Component.POV.DOWN_RIGHT);
        
        private Direction(float directionValue){
            this.directionValue = directionValue;
        }
        
        public float getDirectionValue(){
            return directionValue;
        }
        
        private float directionValue;
    }
    
    private Direction direction;
    
    /** Creates a new instance of POVButton */
    public POVButton(Component component, Direction direction) {
        super(component);
        this.direction = direction;
    }
    
    public String getName(){
        return myComponent.getName() + " " + direction;
    }
    
    public boolean isPressed(){
        return myComponent.getPollData() == direction.getDirectionValue();
    }
    
    public static Button[] getButtonsFrom(Component component) throws InvalidComponentTypeException {
        if(component.getIdentifier() !=  Component.Identifier.Axis.POV){
            throw new InvalidComponentTypeException(component, "Component is not a POV");
        }
        Button[] returnButtons = new Button[Direction.values().length];
        for(int i = 0; i < Direction.values().length; i++){
            returnButtons[i] = new POVButton(component, Direction.values()[i]);
        }
        return returnButtons;
    }
}
