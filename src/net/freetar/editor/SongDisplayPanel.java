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

package net.freetar.editor;

import net.freetar.util.DebugHandler;
import net.freetar.util.SongUtils;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.util.Collection;
import java.util.ArrayList;
import net.freetar.Song;
import net.freetar.Note;

/**
 *
 * @author Anton
 */
public class SongDisplayPanel extends JPanel{
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.music.editor.SongDisplayPanel");

    public static final Color[] TRACK_COLORS = {
        new Color(0,200,0),
        Color.RED,
        Color.YELLOW,
        Color.BLUE,
        Color.ORANGE
    };
    
    private TrackDisplayPanel[] tracks;             //Tracks are always in order 0 = green
    private TimeDisplayPanel timeDisplayPanel;      //Displays the timestamp
    private Song song;                              //The song this display panel is showing
    private float currentTime;                      //The current time TODO - NECESSARY?
    private JPanel labelPanel;                      //Holds the 'Track 0' .... labels
    private JPanel trackDisplayPanel;               //Holds the tracks and the timeDisplaypanel
    private Collection<SongPanListener> listeners;  //Listeners for pan-changes
    private boolean greenOnBottom;                  //True if track 0 (green) should be on the bottom of the trackDisplayPanel
    
    /** Creates a new instance of SongDisplayPanel */
    public SongDisplayPanel() {
        logger.setLevel(Level.WARNING);
        
        tracks = new TrackDisplayPanel[Song.TRACKS];
        listeners = new ArrayList<SongPanListener>();
        
        this.setLayout(new BorderLayout());
        
        labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(Song.TRACKS + 1, 1, 0, 0));
        trackDisplayPanel = new JPanel();
        trackDisplayPanel.setLayout(new GridLayout(Song.TRACKS + 1, 1, 0, 0));
        
        this.add(labelPanel, BorderLayout.WEST);
        this.add(trackDisplayPanel, BorderLayout.CENTER);
        SelectionListener sl = new SelectionListener();
        
        for(int i = Song.TRACKS - 1; i >= 0; i--){
            //Create the new panel
            TrackDisplayPanel newPanel = new TrackDisplayPanel();
            newPanel.setButtonDrawColor(TRACK_COLORS[i % TRACK_COLORS.length]);
            newPanel.setButtonNumber(i);
            
            //Add the new panel to the list of panels
            tracks[i] = newPanel;
            tracks[i].addMouseListener(sl);
            tracks[i].addMouseMotionListener(sl);
            /*
            //Add the label and the new panel to the frames
            labelPanel.add(new JLabel("Track " + i + "  "));
            trackDisplayPanel.add(newPanel);*/
        }
        
        timeDisplayPanel = new TimeDisplayPanel();
        
        //labelPanel.add(new JLabel(" "));
        //trackDisplayPanel.add(timeDisplayPanel);
        greenOnBottom = true;
        reorderTrackDisplayPanels(greenOnBottom);
        
        currentTime = 0;
    }
    
    public void reorderTrackDisplayPanels(boolean greenOnBottom){
        this.greenOnBottom = greenOnBottom;
        //Remove all the components that may already be in the editor
        labelPanel.removeAll();
        trackDisplayPanel.removeAll();
        
        //Re-add them in the indicated order
        if(greenOnBottom){
            for(int i = tracks.length -1; i >= 0; i--){
                labelPanel.add(new JLabel("Track " + i));
                trackDisplayPanel.add(tracks[i]);
            }
        }else{
            for(int i = 0; i < tracks.length; i++){
                labelPanel.add(new JLabel("Track " + i));
                trackDisplayPanel.add(tracks[i]);
            }
        }
        //Add the labelPanel and the time Display panel
        labelPanel.add(new JLabel("Time"));
        trackDisplayPanel.add(timeDisplayPanel);
        
        trackDisplayPanel.validate();
        labelPanel.validate();
    }
    
    public boolean isGreenOnBottom(){
        return greenOnBottom;
    }
    
    public void addSongPanListener(SongPanListener aListener){
        if(!listeners.contains(aListener)){
            listeners.add(aListener);
        }
    }
    
    public void removeSongEditListener(SongPanListener aListener){
        listeners.remove(aListener);
    }
    
    public void setCurrentTime(float currentTime){
        this.currentTime = currentTime;
        for(TrackDisplayPanel panel : tracks){
            panel.setTime(currentTime);
        }
        timeDisplayPanel.setTime(currentTime);
    }
    
    public void setSong(Song s){
        this.song = s;
        for(TrackDisplayPanel panel : tracks){
            panel.setSong(song);
        }
        timeDisplayPanel.setSong(song);
    }
    
    public void increaseZoom(){
        for(TrackDisplayPanel panel : tracks){
            panel.setZoom(panel.getZoom() * 2);
        }
        timeDisplayPanel.setZoom(timeDisplayPanel.getZoom() * 2);
    };
    
    public void decreaseZoom(){
        for(TrackDisplayPanel panel : tracks){
            panel.setZoom(panel.getZoom() * 0.5f);
        }
        timeDisplayPanel.setZoom(timeDisplayPanel.getZoom() * 0.5f);
    }
    
    public float getZoom(){
        return tracks[0].getZoom();
    }
    
    public void resetZoom(){
        for(TrackDisplayPanel panel : tracks){
            panel.setZoom(1.0f);
        }
        timeDisplayPanel.setZoom(1.0f);
    }
    
    public float getSnapTime(){
        return song.getProperties().getBeatLength();
    }
    
    public Collection<Note> getSelectedNotes(){
        HashSet<Note> selectedNotes = new HashSet<Note>();
        for(TrackDisplayPanel panel : tracks){
            selectedNotes.addAll(panel.getSelectedNotes());
        }
        return selectedNotes;
    }
    
    public boolean isSelected(Note e){
        return tracks[e.getButtonNumber()].getSelectedNotes().contains(e);
    }
    
    public void clearSelectedNotes(){
        for(TrackDisplayPanel panel: tracks){
            panel.clearSelectedNotes();
        }
    }
    
    public void selectNotes(Collection<Note> notesToSelect){
        for(Note e : notesToSelect){
            if(e.getButtonNumber() >= 0 && e.getButtonNumber() < tracks.length)
                tracks[e.getButtonNumber()].selectNote(e);
        }
    }
    
    public void setDrawNoteLines(boolean isEnabled){
        for(TrackDisplayPanel panel:tracks){
            panel.drawNoteLines(isEnabled);
        }
    }
    
    public void pressedNote(int buttonNumber){
        tracks[buttonNumber].setButtonPressed(true);
    }
    
    public void releasedNote(int buttonNumber){
        tracks[buttonNumber].setButtonPressed(false);
    }
    
    public void setStrumPressed(boolean isStrumPressed){
        for(TrackDisplayPanel panel : tracks){
            panel.setStrumPressed(isStrumPressed);
        }
    }
    
    public void setTrackBackgrounds(Color backgroundColor){
        for(int i = 0; i < Song.TRACKS; i ++){
            if(i % 2 == 0){
                tracks[i].setBackground(backgroundColor);
            }
        }
    }
    
    public void setDrawErrorBars(boolean drawErrorBars){
        for(int i = 0; i < Song.TRACKS; i++){
            tracks[i].setDrawErrorBars(drawErrorBars);
        }
    }
    
    public void setDrawPressTimes(boolean drawPressTimes){
        for(int i = 0; i < Song.TRACKS; i++){
            tracks[i].setDrawPressTimes(drawPressTimes);
        }
        
    }
    
    protected class SelectionListener implements MouseListener, MouseMotionListener{
        private int mouseDownCoord = -1;    //The X coordinate of the 'pan' press
        
        //Value of -1 indicates that the button is not pressed
        
        public void mouseClicked(MouseEvent mouseEvent) {
            Note clickedNote = null;
            
            //Go through the panels and determine which one was clicked on
            for(TrackDisplayPanel currentPanel : tracks){
                if(mouseEvent.getSource() == currentPanel){
                    logger.info("Clicked inside area for track " + currentPanel.getButtonNumber());
                    clickedNote = currentPanel.getNoteAtLocation(mouseEvent.getX(), mouseEvent.getY());
                }
            }
            
            if(clickedNote == null){
                if(!mouseEvent.isControlDown() && !mouseEvent.isShiftDown()){
                    deselectNotes();
                }
            }else{
                if(mouseEvent.isControlDown()){
                    if(isSelected(clickedNote)){
                        logger.info("Deselecting note: " + clickedNote);
                        deselectNote(clickedNote);
                    }else{
                        logger.info("Selecting NOTE: " + clickedNote);
                        selectNote(clickedNote);
                    }
                }else if(mouseEvent.isShiftDown()){
                    if(getSelectedNotes().size() > 0){
                        Note lastSelectedNote = null;
                        //HACK - set the last selected note to the note at the end of the list
                        for(Note n : getSelectedNotes()){
                            lastSelectedNote = n;
                        }
                        Collection<Note> toSelect = song.getNotesBetween(lastSelectedNote.getTime(), clickedNote.getTime());
                        //remove the notes that aren't on the button numbers between the first and last selected notes
                        /*Collection<Note> toRemove = new ArrayList<Note>();
                        for(Note n : toSelect){
                            if(n.getButtonNumber() > Math.max(clickedNote.getButtonNumber(), lastSelectedNote.getButtonNumber()) ||
                                    n.getButtonNumber() < Math.min(clickedNote.getButtonNumber(), lastSelectedNote.getButtonNumber())){
                                toRemove.add(n);
                            }
                        }
                        toSelect.removeAll(toRemove);
                         */
                        for(Note n : toSelect){
                            selectNote(n);
                        }
                    }else{
                        selectNote(clickedNote);
                    }
                }else{
                    //If control isn't down, and user clicked on a note
                    logger.info("Clicked on NOTE: " + clickedNote);
                    deselectNotes();
                    selectNote(clickedNote);
                }
            }
        }
        
        private void selectNote(Note e){
            tracks[e.getButtonNumber()].selectNote(e);
        }
        
        private void deselectNote(Note e){
            tracks[e.getButtonNumber()].deselectNote(e);
        }
        
        private void deselectNotes(){
            for(TrackDisplayPanel panel : tracks){
                panel.clearSelectedNotes();
            }
        }
        
        public void mousePressed(MouseEvent mouseEvent) {
            if(mouseEvent.getButton() == MouseEvent.BUTTON3 && song != null){
                logger.info("Mouse down at coord: " + mouseEvent.getX());
                mouseDownCoord = mouseEvent.getX();
            }
        }
        
        public void mouseReleased(MouseEvent mouseEvent) {
            if(mouseEvent.getButton() == MouseEvent.BUTTON3){
                mouseDownCoord = -1;
            }
        }
        
        public void mouseEntered(MouseEvent mouseEvent) {
        }
        
        public void mouseExited(MouseEvent mouseEvent) {
        }
        
        public void mouseDragged(MouseEvent mouseEvent) {
            if(mouseDownCoord != -1){
                logger.fine("Mouse Dragged to: " + mouseEvent.getX());
                int currentMouseCoord = mouseEvent.getX();
                if(mouseDownCoord - currentMouseCoord != 0){
                    float timeShift = snap(tracks[0].getTimeOf(mouseDownCoord)) - snap(tracks[0].getTimeOf(currentMouseCoord));
                    float newCurrentTime = currentTime + timeShift;
                    if(timeShift != 0){
                        logger.fine("Shifting time: " + timeShift + " to: " + newCurrentTime);
                        setCurrentTime(snap(newCurrentTime));
                        //Inform the listeners of the change
                        for(SongPanListener l : listeners){
                            l.timeChanged(currentTime);
                        }
                    }
                    mouseDownCoord = mouseEvent.getX();
                }
            }
        }
        
        public void mouseMoved(MouseEvent mouseEvent) {
        }
        
        private float snap(float time){
            return SongUtils.snapTimeToNearest(time, getSnapTime());
        }
    }
    
}
