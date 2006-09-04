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

import net.freetar.DuplicateNoteException;
import net.freetar.Note;
import net.freetar.Song;
import net.freetar.TrackBasedSong;
import net.freetar.io.Exporter;
import net.freetar.io.FileFormatException;
import net.freetar.io.Importer;
import net.freetar.io.IncorrectVersionException;
import net.freetar.io.XMLExporter;
import net.freetar.io.XMLImporter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.ProgressMonitor;

/**
 *
 * @author Temp
 */
public class SongUtils {
    public static final Logger logger = DebugHandler.getLogger("com.astruyk.util.SongUtils");
    static{
        logger.addHandler(DebugHandler.getInstance().getDebugFileHandler());
    }
    public static float snapTimeToNearest(float timeToSnap, float snapIncrementSize){
        return snapTimeToNearest(timeToSnap, snapIncrementSize, 0);
    }
    
    public static float snapTimeToNearest(float timeToSnap, float snapIncrementSize, float offset){
        //TODO implement the offset!!!
        final int numberOfFullSnaps = (int) (timeToSnap / snapIncrementSize);
        final float timeRemaining = timeToSnap - snapIncrementSize * numberOfFullSnaps;
        if(timeRemaining > snapIncrementSize / 2){
            return (numberOfFullSnaps + 1) * snapIncrementSize;
        }else{
            return numberOfFullSnaps * snapIncrementSize;
        }
    }
    
    public static int convertToMillis(float timeInSeconds){
        return (int) (timeInSeconds * 1000f);
    }
    
    public static float convertToSeconds(int timeInMillis){
        return ((float) timeInMillis) / 1000f;
    }
    
    public static Song loadFromFile(File inputFile)
    throws IOException, IncorrectVersionException, UnsupportedVersionException, FileFormatException{
        Importer importer = new XMLImporter();
        return importer.importSongFrom(inputFile);
    }
    
    public static void saveToFile(File outputFile, Song songToSave)
    throws IOException{
        Exporter x = new XMLExporter();
        x.exportTo(outputFile, songToSave);
    }
    
    protected static void writeLine(String line, BufferedWriter out) throws IOException{
        FileUtils.writeLine(line, out);
    }
    
    protected static String readLine(BufferedReader in) throws IOException {
        return FileUtils.readLine(in);
    }
}
