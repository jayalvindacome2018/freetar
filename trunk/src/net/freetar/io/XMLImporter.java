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

import net.freetar.DuplicateNoteException;
import net.freetar.Song;
import net.freetar.SongProperties;
import net.freetar.TrackBasedSong;
import net.freetar.util.DebugHandler;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Anton
 */
public class XMLImporter implements Importer {
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.freetar.util.XMLImporter");
    
    public Song importSongFrom(File inputFile) throws IOException, FileFormatException{
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            //TODO throw some other kind of exception
            ex.printStackTrace();
            return null;
        }
        
        Document doc = null;
        try {
            doc = builder.parse(inputFile);
        } catch (IOException ex) {
            logger.warning("IOException created during XML parsing" + ex);
            throw ex;
        } catch (SAXException ex) {
            logger.warning("SAXException created during XML parsing: " + ex);
            throw new FileFormatException("SAXException created during XML parsing: " + ex);
        }
        
        Song newSong = new TrackBasedSong();
        //Parse the Song properties
        Element propertiesElement = (Element) doc.getElementsByTagName("Properties").item(0);
        if(propertiesElement == null){
            throw new FileFormatException("No <Properties> tag in XML");
        }
        
        Element versionElement = (Element) propertiesElement.getElementsByTagName("Version").item(0);
        if(versionElement == null || !"0.1".equals(getCharacterDataFromElement(versionElement))){
            return null;
        }
        
        Element titleElement = (Element) propertiesElement.getElementsByTagName("Title").item(0);
        Element artistElement = (Element) propertiesElement.getElementsByTagName("Artist").item(0);
        Element albumElement = (Element) propertiesElement.getElementsByTagName("Album").item(0);
        Element yearElement = (Element) propertiesElement.getElementsByTagName("Year").item(0);
        Element lengthElement = (Element) propertiesElement.getElementsByTagName("Length").item(0);
        Element musicFileNameElement = (Element) propertiesElement.getElementsByTagName("MusicFileName").item(0);
        Element musicDirectoryHintElement = (Element) propertiesElement.getElementsByTagName("MusicDirectoryHint").item(0);
        Element beatsPerSecondElement = (Element) propertiesElement.getElementsByTagName("BeatsPerSecond").item(0);
        Element beatOffsetElement = (Element) propertiesElement.getElementsByTagName("BeatOffset").item(0);
        Element allowableErrorTimeElement = (Element) propertiesElement.getElementsByTagName("AllowableErrorTime").item(0);
        Element songDifficultyElement = (Element) propertiesElement.getElementsByTagName("Difficulty").item(0);
        
        //This MUST be present (or no point in loading any further)
        SongProperties properties = newSong.getProperties();
        
        if(musicFileNameElement != null){
            properties.setMusicFileName(getCharacterDataFromElement(musicFileNameElement));
        }else{
            throw new FileFormatException("XML file does not contain <Song><Properties><MusicFileName> tag.");
        }
        try{
            //The rest of the elements are optional
            if(titleElement != null){
                properties.setTitle(getCharacterDataFromElement(titleElement));
            }
            if(artistElement != null){
                properties.setArtist(getCharacterDataFromElement(artistElement));
            }
            if(albumElement != null){
                properties.setAlbum(getCharacterDataFromElement(albumElement));
            }
            if(yearElement != null){
                properties.setYear(Integer.valueOf(getCharacterDataFromElement(yearElement)));
            }
            if(lengthElement != null){
                properties.setLength(Float.valueOf(getCharacterDataFromElement(lengthElement)));
            }
            if(musicDirectoryHintElement != null){
                properties.setMusicDirectoryHint(getCharacterDataFromElement(musicDirectoryHintElement));
            }
            if(beatsPerSecondElement != null){
                properties.setBeatsPerSecond(Float.valueOf(getCharacterDataFromElement(beatsPerSecondElement)));
            }
            if(beatOffsetElement != null){
                properties.setOffset(Float.valueOf(getCharacterDataFromElement(beatOffsetElement)));
            }
            if(allowableErrorTimeElement != null){
                properties.setAllowableErrorTime(Float.valueOf(getCharacterDataFromElement(allowableErrorTimeElement)));
            }
            if(songDifficultyElement != null){
                properties.setDifficulty(SongProperties.Difficulty.valueOf(getCharacterDataFromElement(songDifficultyElement)));
            }
        }catch(NumberFormatException ex){
            //TODO have this be thrown from each attempt to convert, not a general catch-all
            throw new FileFormatException("Number Format Exception: " + ex);
        }
        //Parse the Note Data
        Element dataElement = (Element) doc.getElementsByTagName("Data").item(0);
        NodeList dataNodes = dataElement.getElementsByTagName("Note");
        for(int i = 0; i < dataNodes.getLength(); i++){
            Element noteElement = (Element) dataNodes.item(i);
            float noteTime = Float.valueOf(noteElement.getAttribute("time"));
            float noteDuration = Float.valueOf(noteElement.getAttribute("duration"));
            int noteTrack = Integer.valueOf(noteElement.getAttribute("track"));
            Element hammerOnElement = (Element) noteElement.getElementsByTagName("HammerOnAllowed").item(0);
            boolean canHammerOn = hammerOnElement != null;
            logger.fine("Loaded NoteData - t:" + noteTime + " d:" + noteDuration + " t:" + noteTrack);
            try {
                
                newSong.addNote(newSong.createNote(noteTime, noteDuration, noteTrack, canHammerOn));
            } catch (DuplicateNoteException ex) {
                logger.warning("IGNORING UPLICATE NOTE - " + ex);
            }
        }
        
        return newSong;
    }
    
    
    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return null;
    }
}
