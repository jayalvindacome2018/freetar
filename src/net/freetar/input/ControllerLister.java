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
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;

/**
 *
 * @author Anton
 */
public class ControllerLister {
    
    public static void main(String[] args){
        ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
        Controller[] controllers = ce.getControllers();
        for(Controller controller : controllers){
            printController (controller);
        }
    }
    
    public static void printController(Controller c){
        printController(c, 0);
    }
    
    public static void printController(Controller controller, int indent){
        printString("Controller: " + controller, indent);
        printString("Name: " + controller.getName(), indent + 1);
        printString("Type: " + controller.getType(), indent + 1);
        
        printString("Listing Components: ", indent + 1);
        Component[] components = controller.getComponents();
        for(Component component : components){
            printComponent(component, indent + 2);
        }
        printString("Has Sub-Controllers: " + ((controller.getControllers().length != 0) ? "Yes" : "No"), indent + 1);
        Controller[] subControllers = controller.getControllers();
        for(Controller subController : subControllers){
            printController(subController, indent + 2);
        }
    }
    
    public static void printComponent(Component c, int indent){
        printString("Component: " + c, indent);
        printString("Name: " + c.getName(), indent + 1);
        printString("Identifier: " + c.getIdentifier(), indent + 2);
        printString("Identifier Name: " + c.getIdentifier().getName(), indent + 3);
    }
    
    
    public static void printString(String s){
        printString(s,0);
    }
    
    public static void printString(String s, int indent){
        for(int i = 0; i < indent; i++){
            System.out.print("  ");
        }
        System.out.println(s);
    }
}
