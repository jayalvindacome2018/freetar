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

import net.freetar.noteStates.NoteState;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ImageIcon;

import net.freetar.Note;
import net.freetar.Song;

/**
 *
 * @author Temp
 */
public class TrackDisplayPanel extends JPanel {
    protected static final long serialVersionUID = 1;               //Unique ID for this
    protected static final float DEFAULT_VIEW_AHEAD_TIME = 3;       //Time in seconds to show 'ahead' of the current time
    protected static final float DEFAULT_VIEW_BEHIND_TIME = 3;      //Time in seconds to show 'behind' the current time
    
    protected static final Color WAITING_COLOR = Color.GRAY;
    protected static final Color PRESSABLE_COLOR = Color.ORANGE;
    protected static final Color MISSED_COLOR = Color.RED;
    protected static final Color PLAYED_COLOR = Color.GREEN;
    protected static final Color HOLDING_COLOR = Color.BLUE;
    
    protected float currentTime = -1;           //The time to render the song at
    protected int buttonNumber;                 //The button number of this track
    protected Song song;                        //The song to draw notes from
    protected float zoomLevel = 1.0f;           //The percentage of zoom
    protected Collection<Note> selectedNotes;         //A list of notes that are selected in this track
    protected Color buttonDrawColor;            //The color to draw the buttons (so different tracks can be different colors)
    protected boolean drawTimes = false;        //Flag to indicate whether to draw a timestamp at each 'beat'
    protected boolean drawNoteLines = false;    //Draw the red 'note placement' line
    protected boolean drawSnapLines = true;     //Draw the little 'snap to' lines
    protected boolean drawSecondLines = true;   //Draw the lines each second
    protected boolean buttonIsDown = false;       //Display the track as if the button were pressed
    protected boolean strumIsPressed = false;     //Display the track as if the strum button were pressed
    protected boolean drawErrorBars = false;      //Draw the 'error bars' on the notes
    protected boolean drawPressTimes = false;     //Draw a line if the note was pressed at the pressTime
    
    /** Creates a new instance of SongDisplayPanel */
    public TrackDisplayPanel() {
        super();
        
        //Setup instance variables
        currentTime = 0;
        song = null;
        selectedNotes = new ArrayList<Note>();
        
        //Set the background color
        this.setBackground(Color.WHITE);
    }
    
    public int getButtonNumber(){
        return buttonNumber;
    }
    
    public void setButtonNumber(int buttonNumber){
        this.buttonNumber = buttonNumber;
    }
    
    public void setButtonDrawColor(Color c){
        this.buttonDrawColor = c;
    }
    
    public void drawTimes(boolean drawTimes){
        this.drawTimes = drawTimes;
        this.repaint();
    }
    
    public void setZoom(float zoomFactor){
        zoomLevel = zoomFactor;
        if(zoomLevel < 0.001f) zoomLevel = 0.001f;
        if(zoomLevel > 10.0f) zoomLevel = 10.0f;
        this.repaint();
    }
    
    public float getZoom(){
        return zoomLevel;
    }
    
    public void setSong(Song aSong){
        this.song = aSong;
        this.repaint();
    }
    
    public void setTime(float aTime) {
        this.currentTime = aTime;
        this.repaint();
    }
    
    public float getViewBehindTime(){
        return DEFAULT_VIEW_BEHIND_TIME / zoomLevel;
    }
    
    public float getViewAheadTime(){
        return DEFAULT_VIEW_AHEAD_TIME / zoomLevel;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        //Dont do anything if there isn't a song loaded
        if(song == null){
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0,0,this.getWidth(), this.getHeight());
            return;
        }
        
        //Calculate some constants
        final float secondFractionTime = currentTime - (int) currentTime;
        final float viewAheadTime = getViewAheadTime();
        final float viewBehindTime = getViewBehindTime();
        final float beatLength = song.getProperties().getBeatLength();
        final int currentTimeX = getXOfTime(currentTime);
        float barStartTime = currentTime - viewBehindTime;
        final int height = this.getHeight();
        final int width = this.getWidth();
        
        if(barStartTime < 0){
            barStartTime = 0;
        }
        float barEndTime = currentTime + viewAheadTime;
        if(song.getProperties().getLength() != 0 && barEndTime > song.getProperties().getLength()){
            barEndTime = song.getProperties().getLength();
        }
        final float totalBarTime = viewBehindTime + viewAheadTime;
        
        //Find the 'number' of the first and last snap bars to draw
        final int firstBeatBarToDraw = (int) (barStartTime / beatLength);
        final int lastBeatBarToDraw = (int) (barEndTime / beatLength);
        
        //Draw all the BEAT bars
        for(int i = firstBeatBarToDraw; i <= lastBeatBarToDraw; i++){
            final float currentLineTime = i * beatLength;
            final int currentLineX = getXOfTime(i * beatLength);
            
            if(drawSnapLines){
                g.setColor(Color.LIGHT_GRAY);
                if(i % 2 == 0){
                    g.drawLine(currentLineX,
                            this.getHeight() - this.getHeight() / 8,
                            currentLineX,
                            this.getHeight());
                }else{
                    g.drawLine(currentLineX,
                            this.getHeight() - this.getHeight() / 4,
                            currentLineX,
                            this.getHeight());
                }
            }
            g.setColor(Color.LIGHT_GRAY);
            
            //Draw the timestamp, centered vertically and horizontally on the second
            if(drawTimes && currentLineTime % 1 == 0){
                final int min = (int) (currentLineTime / 60);
                final int sec = (int) (currentLineTime % 60);
                final String timeStamp = min + ":" + sec;
                g.drawString(timeStamp + "",
                        currentLineX - g.getFontMetrics().stringWidth(timeStamp) / 2,
                        this.getHeight() / 2 + g.getFontMetrics().getAscent() / 2);
            }
        }
        //Draw the second lines
        if(this.drawSecondLines){
            //Draw the 'Second' lines
            g.setColor(Color.BLUE);
            for(int i = (int) barStartTime;  i < (int) barEndTime + 1; i++){
                final int xCoord = getXOfTime(i);
                g.drawLine(xCoord, 0, xCoord, this.getHeight());
            }
        }
        
        // Draw the notes
        if (song != null) {
            Collection<Note> notes = song.getNotesForButton(buttonNumber);
            for(Note e : notes){
                drawNote(e, g);
            }
        }
        
        //Draw the line at the current time
        g.setColor(Color.RED);
        g.drawLine(currentTimeX, 0, currentTimeX, this.getHeight());
        
        //Draw the button if it is being held down
        final float secondSize = (float) this.getWidth() / viewAheadTime + viewBehindTime;
        final int drawBoxSize = (int) ((beatLength * secondSize / 2) * 1.5f);
        if(buttonIsDown){
            g.setColor(buttonDrawColor);
            g.fillRect(currentTimeX - drawBoxSize / 2,
                    this.getHeight() / 2 - drawBoxSize / 2,
                    drawBoxSize,
                    drawBoxSize);
            if(strumIsPressed){
                g.setColor(Color.BLACK);
                g.drawRect(currentTimeX - drawBoxSize / 2,
                        this.getHeight()  / 2 - drawBoxSize / 2,
                        drawBoxSize,
                        drawBoxSize);
            }
        }
        
        //Determine if there are any off-screen selected notes, and draw
        //a 'hint' if there are...
        boolean selectedBeforeViewTime = false;
        boolean selectedAfterViewTime = false;
        for(Iterator<Note> i = selectedNotes.iterator(); i.hasNext() && (!selectedBeforeViewTime || !selectedAfterViewTime);){
            final Note n = i.next();
            if(n.getTime() > currentTime + viewAheadTime){
                selectedAfterViewTime = true;
            }
            if(n.getTime() + n.getDuration() < currentTime - viewBehindTime){
                selectedBeforeViewTime = true;
            }
        }
        
        g.setColor(Color.MAGENTA);
        final int arrowWidth = 10;
        if(selectedBeforeViewTime){
            int[] xCoords = {0, arrowWidth, arrowWidth};
            int[] yCoords = {height / 2, 0, height};
            g.fillPolygon(xCoords, yCoords, 3);
        }
        if(selectedAfterViewTime){
            int[] xCoords = {width, width-arrowWidth, width-arrowWidth};
            int[] yCoords = {height / 2, 0, height};
            g.fillPolygon(xCoords, yCoords, 3);
        }
    }
    
    /**
     * Computes the x-coordinate of the specified time in the panel
     *
     * @param time
     *            the time to compute the coordinate for
     * @return the x-coordinate of the time
     */
    public int getXOfTime(float time) {
        /*
        //EASY TO READ
        final float barStartTime = currentTime - getViewBehindTime();
        final float barEndTime = currentTime + getViewAheadTime();
        final float percent = (time - barStartTime) / (barEndTime - barStartTime);
        return (int) (percent * (float) this.getWidth());
         */
        
        //Optimized
        return (int) (((time - currentTime + getViewBehindTime()) / (getViewAheadTime() + getViewBehindTime())) * (float) this.getWidth());
    }
    
    public float getTimeOf(int x) {
        final float percent = (float) x / (float) this.getWidth();
        final float barLengthTime = getViewAheadTime() + getViewBehindTime();
        final float barStartTime = currentTime - getViewBehindTime();
        return barStartTime + barLengthTime * percent;
    }
    
    public Song getSong(){
        return song;
    }
    
    protected void drawNote(Note e, Graphics g) {
        final float noteStartTime = e.getTime();
        final float noteEndTime = noteStartTime + e.getDuration();
        final float viewBehindTime = getViewBehindTime();
        final float viewAheadTime = getViewAheadTime();
        final int height = this.getHeight();
        final int width = this.getWidth();
        
        if (noteEndTime >= currentTime - viewBehindTime &&
                noteStartTime <= currentTime + viewAheadTime) {
            final int startTimeX = getXOfTime(noteStartTime);
            final int stopTimeX = getXOfTime(noteEndTime);
            final float closeEnoughTime = song.getProperties().getAllowableErrorTime() / 2.0f;
            final float secondSize = (float) width / viewAheadTime + viewBehindTime;
            int selectSize = (int) (song.getProperties().getBeatLength() * secondSize / 2);
            
            //Draw the 'press to activate' line in the state color
            Color stateColor = null;
            final NoteState noteState = e.getState();
            if(noteState == Note.WAITING_STATE){
                stateColor = WAITING_COLOR;
            }else if (noteState == Note.PRESSABLE_STATE){
                selectSize *= 2f;
                stateColor = PRESSABLE_COLOR;
            }else if(noteState == Note.MISSED_STATE){
                stateColor = MISSED_COLOR;
            }else if(noteState == Note.PLAYED_STATE){
                stateColor = PLAYED_COLOR;
            }else if(noteState == Note.HOLDING_STATE){
                stateColor = HOLDING_COLOR;
            }
            
            
            //Increase the size (if it hasn't been already increased) if the note
            //would be in the 'playable' state
            if(currentTime >= (noteStartTime - closeEnoughTime) &&
                    currentTime <= (noteEndTime + closeEnoughTime)  &&
                    noteState != Note.PRESSABLE_STATE){
                selectSize *= 1.5f;
            }
            
            //Draw the 'Error Bars'
            g.setColor(Color.GRAY);
            if(this.drawErrorBars){
                g.fillRect(getXOfTime(noteStartTime - closeEnoughTime),
                        height / 2 - ((int) (selectSize * 0.75f) / 2),
                        (int) (closeEnoughTime * secondSize),
                        (int) (selectSize * 0.75f));
            }
            
            //Draw the 'hold bar'
            g.setColor(stateColor);
            if(noteState == Note.WAITING_STATE || noteState == Note.PRESSABLE_STATE){
                g.setColor(buttonDrawColor);
            }
            g.fillRect(startTimeX,
                    height / 2 - selectSize / 3,
                    stopTimeX - startTimeX,
                    selectSize  * 2 / 3);
            
            //Draw the note itself
            //Color depends on whether the note is selected
            if(selectedNotes.contains(e)){
                g.setColor(Color.MAGENTA);
            }
            g.fillRect(startTimeX - selectSize / 2,
                    height / 2 - selectSize / 2,
                    selectSize,
                    selectSize);
            
            //Draw the black outline of the notes
            g.setColor(Color.BLACK);
            g.drawRect(startTimeX - selectSize / 2,
                    height / 2 - selectSize / 2,
                    selectSize,
                    selectSize);
            
            //Draw the 'precise' note lines
            if(drawNoteLines){
                g.setColor(Color.RED);
                g.drawLine(startTimeX, 0, startTimeX, height);
            }
            
            //Draw the Line for the button press time
            if(drawPressTimes && noteState == Note.PLAYED_STATE){
                g.setColor(Color.BLACK);
                final int pressTimeX = getXOfTime(e.getPressTime());
                g.drawLine(pressTimeX, 0, pressTimeX, height);
            }
            
            //Draw the Box around the note in the color indicating its state
            g.setColor(stateColor);
            g.drawRect(startTimeX - selectSize / 2,
                    height / 2 - selectSize / 2,
                    selectSize,
                    selectSize);
            
            if(e.isHammerOnAllowed()){
                final int buffer = selectSize / 2;
                g.setColor(Color.BLACK);
                g.drawRect(startTimeX - selectSize / 2 - 1,
                    height / 2 - selectSize / 2 - 1,
                    selectSize + 2,
                    selectSize + 2);
            }
        }
    }
    
    public void selectNote(Note e){
        if(!selectedNotes.contains(e)){
            selectedNotes.add(e);
        }
        this.repaint();
    }
    
    public Collection<Note> getSelectedNotes(){
        return new ArrayList<Note>(selectedNotes);
    }
    
    public void clearSelectedNotes(){
        selectedNotes.clear();
        this.repaint();
    }
    
    public Note getNoteAtLocation(int x, int y){
        if(song == null){
            return null;
        }
        
        float clickTime = getTimeOf(x);
        float closeEnoughTime = song.getProperties().getBeatLength() / 2.0f;
        for(Note evt : song.getNotesForButton(buttonNumber)){
            float noteStartTime = evt.getTime();
            float noteEndTime = evt.getTime() + evt.getDuration();
            if(clickTime >= noteStartTime - closeEnoughTime && clickTime <= noteEndTime + closeEnoughTime){
                return evt;
            }
        }
        
        return null;
    }
    
    public void drawNoteLines(boolean isEnabled){
        this.drawNoteLines = isEnabled;
        this.repaint();
    }
    
    public boolean isDrawNoteLinesEnabled(){
        return drawNoteLines;
    }
    
    public void drawTimeStamp(boolean isEnabled){
        this.drawTimes = isEnabled;
    }
    
    public boolean isDrawTimeStampEnabled(){
        return drawTimes;
    }
    
    public void deselectNote(Note e){
        selectedNotes.remove(e);
        this.repaint();
    }
    
    public void setButtonPressed(boolean isDown){
        buttonIsDown = isDown;
        this.repaint();
    }
    
    public void setStrumPressed(boolean isStrumPressed){
        this.strumIsPressed = isStrumPressed;
        this.repaint();
    }
    
    public void setDrawErrorBars(boolean drawErrorBars){
        this.drawErrorBars = drawErrorBars;
        repaint();
    }
    
    public void setDrawPressTimes(boolean drawPressTimes){
        this.drawPressTimes = drawPressTimes;
        repaint();
    }
}
