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

package net.freetar.editor.commands;

import java.util.LinkedList;
import java.util.List;

/**
 * Combines a number of commands into a single command.
 * @author Anton
 */
public class MacroCommand implements Command{
    /**
     * An ordered list of the commands that will be carried out for this macro.
     * Commands are stored in the same order in which they were added (FIFO)
     */
    protected List<Command> commands;
    
    /**
     * Creates a new empty macro.
     */
    public MacroCommand() {
        commands = new LinkedList<Command>();
    }
    
    /**
     * Adds a new command to this macro.
     * @param c The command to add to this macro
     */
    public void addCommand(Command c){
        commands.add(c);
    }
    
    /**
     * Determines if the macro command is undoable or not
     * @return true if and only if all the commands in the macro are undoable
     */
    public boolean isUndoable() {
        for(Command c : commands){
            if(!c.isUndoable()){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calls the undo() method of each of the commands in the reverse order that they
     * are executed in the execute() method. If this macro return false from the
     * isUndoable() method, then no commands are undone.
     */
    public void undo() {
        if(this.isUndoable()){
            Object[] commandArray = commands.toArray();
            for(int i = commandArray.length - 1; i >= 0; i--){
                ((Command) commandArray[i]).undo();
            }
        }
    }
    
    /**
     * Calls the execute() methods of the commands in this macro in the order that
     * they were added to this macro.
     */
    public void execute() {
        for(Command c : commands){
            c.execute();
        }
    }
}
