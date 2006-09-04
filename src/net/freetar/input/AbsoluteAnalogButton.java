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

/**
 *
 * @author Anton
 */
public class AbsoluteAnalogButton extends AbstractButton{
    private enum Value{
        POSITIVE(0.5f, 1.0f),
        NEGATIVE(-0.5f, -1.0f);
        
        private float min;
        private float max;
        private Value(float min, float max){
            if(min < max){
                this.min = min;
                this.max = max;
            }else{
                this.min = max;
                this.max = min;
            }
        }
        private boolean isInRange(float value){
            return (value >= min) && (value <= max);
        }
    }
    
    private Value value;
    
    public AbsoluteAnalogButton(Component axis, Value value){
        super(axis);
        this.value = value;
    }
    
    public boolean isPressed(){
        return value.isInRange(myComponent.getPollData());
    }
    
    public String getName(){
        return myComponent.getName() + " " + value;
    }
    
    public static Button[] getButtonsFrom(Component component) throws InvalidComponentTypeException{
        if(!(component.getIdentifier() instanceof Component.Identifier.Axis)){
            throw new InvalidComponentTypeException(component, "Component is not an an Axis.");
        }
        
        Button[] returnButtons = new Button[Value.values().length];
        for(int i = 0; i < Value.values().length; i++){
            returnButtons[i] = new AbsoluteAnalogButton(component, Value.values()[i]);
        }
        return returnButtons;
    }

    public String typeString() {
        return "AbsoluteAnalogButton";
    }

    public String valueString() {
        return value + "";
    }
}
