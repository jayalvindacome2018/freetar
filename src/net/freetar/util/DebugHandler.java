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

package net.freetar.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anton
 */
public class DebugHandler{
    private static DebugHandler instance;
    private Handler debugFileHandler;
    
    /** Creates a new instance of DebugHandler */
    public DebugHandler() {
        try{
            debugFileHandler = new FileHandler("debuglog.xml");
            debugFileHandler.setLevel(Level.ALL);
        }catch(IOException ex){
            //DO SOMETHING? Or simply fail....
        }
    }
    
    public static DebugHandler getInstance(){
        if(instance != null){
            return instance;
        }
        
        instance = new DebugHandler();
        return instance;
    }
    
    public Handler getDebugFileHandler(){
        return debugFileHandler;
    }
    
    public static Logger getLogger(String loggerName){
        Logger returnLogger = Logger.getLogger(loggerName);
        returnLogger.addHandler(getInstance().getDebugFileHandler());
        return returnLogger;
    }
    
    public static void logException(Logger logger, Exception ex){
        logger.warning("Exception: " + ex.getMessage());
        String stackString = "";
        for(StackTraceElement ste : ex.getStackTrace()){
            stackString += ste.toString() + "\n";
        }
        logger.warning("Stacktrace:\n" + stackString);
    }
}
