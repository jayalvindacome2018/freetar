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

import net.freetar.util.DebugHandler;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public abstract class UndoableCommand implements Command{
    private static final Logger logger = DebugHandler.getLogger("UndoableCommand");

    protected boolean executedOK = true;
    
    public boolean isUndoable(){
        return true;
    }
    
    public void execute(){
        try{
            executeCommand();
        }catch(Exception ex){
            executedOK = false;
            logger.warning("Unable to execute command - " + ex);
            //ex.printStackTrace();
        }
    }
    
    public void undo(){
        if(executedOK){
            try {
                undoCommand();
            } catch (Exception ex) {
                logger.warning("Unable to undo command - " + ex);
                //ex.printStackTrace();
            }
        }
    }
    
    protected abstract void executeCommand() throws Exception;
    protected abstract void undoCommand() throws Exception;
}
