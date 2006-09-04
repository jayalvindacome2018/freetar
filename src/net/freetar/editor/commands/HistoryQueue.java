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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Anton
 */
public class HistoryQueue {
    private LinkedList<Command> undoableCommands;   //List of undoable commands (undo history) in order
    private LinkedList<Command> redoableCommands;   //List of re-doable commands (redo history) in order
    private static final int MAX_STEPS = 30;
    
    public HistoryQueue() {
        undoableCommands = new LinkedList<Command>();
        redoableCommands = new LinkedList<Command>();
    }
    
    public void addToHistory(Command commandToAdd){
        if(commandToAdd.isUndoable()){
            redoableCommands.clear();
            undoableCommands.addLast(commandToAdd);
            //If the history is too long, remove the first (oldest) element
            if(undoableCommands.size() > MAX_STEPS){
                undoableCommands.removeFirst();
            }
        }
    }
    
    public void stepForward(){
        if(canStepForward()){
            Command redoCommand = redoableCommands.removeLast();
            redoCommand.execute();
            addToHistory(redoCommand);
        }
    }
    
    public void stepBackward(){
        if(canStepBackward()){
            Command undoCommand = undoableCommands.removeLast();
            undoCommand.undo();
            redoableCommands.addLast(undoCommand);
            //We don't need to check the redo-history size, because we
            //can only re-do un-done actions, so the size of the redo will never
            //be greater than the maximum size of the undo history
        }
    }
    
    public boolean canStepForward(){
        return redoableCommands.size() > 0;
    }
    
    public boolean canStepBackward(){
        return undoableCommands.size() > 0;
    }
    
    public void clearHistory(){
        undoableCommands.clear();
        redoableCommands.clear();
    }
}
