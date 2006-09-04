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

package net.freetar.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.freetar.Note;
import net.freetar.Song;
import net.freetar.SongProperties;

/**
 *
 * @author Anton
 */
public class XMLExporter implements Exporter {
    private static final String indentString = "    ";
    BufferedWriter out = null;
    
    public void exportTo(File file, Song song)
    throws IOException {
        if(out != null) out.close();
        out = null;
        
        try{
            out = new BufferedWriter(new FileWriter(file));
            
            //Write the XML Header
            writeString("<?xml version=\"1.0\"?>", 0);
            //Open Song Tag
            writeString("<Song>", 0);
            
            //Write basic song properties
            SongProperties properties = song.getProperties();
            
            writeString("<Properties>", 1);
            writeString("<Version>0.1</Version>", 2);
            writeString("<Title>" + properties.getTitle() + "</Title>", 2);
            writeString("<Artist>" + properties.getArtist() + "</Artist>", 2);
            writeString("<Album>" + properties.getAlbum() + "</Album>", 2);
            writeString("<Year>" + properties.getYear() + "</Year>", 2);
            writeString("<BeatsPerSecond>" + properties.getBeatsPerSecond() + "</BeatsPerSecond>", 2);
            writeString("<BeatOffset>" + properties.getOffset() + "</BeatOffset>", 2);
            //writeString("<HammerOnTime>" + properties.getHammerOnTime() + "</HammerOnTime>", 2);
            //writeString("<PullOffTime>" + properties.getPullOffTime() + "</PullOffTime>", 2);
            if(properties.getDifficulty() != SongProperties.Difficulty.NOT_SET){
                writeString("<Difficulty>" + properties.getDifficulty() + "</Difficulty>", 2);
            }
            writeString("<AllowableErrorTime>" + properties.getAllowableErrorTime() + "</AllowableErrorTime>", 2);
            writeString("<Length>" + properties.getLength() + "</Length>", 2);
            writeString("<MusicFileName>" + properties.getMusicFileName() + "</MusicFileName>", 2);
            writeString("<MusicDirectoryHint>" + properties.getMusicDirectoryHint() + "</MusicDirectoryHint>", 2);
            writeString("</Properties>", 1);
            out.newLine();
            
            //Write Note Data
            writeString("<Data>", 1);
            for(Note n : song.getAllNotes()){
                writeString("<Note time=\"" + n.getTime() + "\" " +
                        "duration=\"" + n.getDuration() + "\" " +
                        "track=\"" + n.getButtonNumber() + "\" " +
                        ">", 2);
                if(n.isHammerOnAllowed()){
                    writeString("<HammerOnAllowed />", 3);
                }
                writeString("</Note>", 2);
            }
            writeString("</Data>", 1);
            
            //Close Song Tag
            writeString("</Song>", 0);
            song.setUnsavedChanges(false);
        }catch(IOException ex){
            throw ex;
        }finally{
            if(out != null ){
                out.flush();
                out.close();
            }
            out = null;
        }
    }
    
    private void writeString(String toWrite, int indent)
    throws IOException{
        for(int i = 0; i < indent; i++){
            toWrite = indentString + toWrite;
        }
        out.write(toWrite);
        out.newLine();
    }
}
