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
package net.freetar;

/**
 *
 * @author Anton
 */
public class SongProperties {
    public enum Difficulty{
        NOT_SET,
        EASY,
        MEDIUM,
        HARD,
        EXPERT;
    };
    
    private boolean isDirty = false;    //If there are unsaved changes, dirty = true
    
    private String title = "No Title Set";
    private String artist = "No Artist Set";
    private String album = "No Album Set";
    private int year = 0;
    private String author = "No Author Set";
    private float beatsPerSecond = 16.0f;
    private float offset = 0.0f;
    private float allowableErrorTime = 0.25f;
    private Difficulty difficulty = Difficulty.NOT_SET;
    //private float hammerOnTime = 0.25f;
    //private float pullOffTime = 0.25f;
    private String musicFileName = "";
    private String musicDirectoryHint = "";
    private float length = 0.0f;
    
    public boolean isDirty(){
        return isDirty;
    }
    public void setDirty(boolean isDirty){
        this.isDirty = isDirty;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    
    public void setArtist(String artist){
        this.artist = artist;
    }
    public String getArtist(){
        return artist;
    }
    
    public void setAlbum(String album){
        this.album = album;
    }
    public String getAlbum(){
        return album;
    }
    
    public void setYear(int year){
        this.year = year;
    }
    public int getYear(){
        return year;
    }
    
    public void setAuthor(String author){
        this.author = author;
    }
    public String getAuthor(){
        return author;
    }
    
    public void setBeatsPerSecond(float beatsPerSecond){
        this.beatsPerSecond = beatsPerSecond;
    }
    public float getBeatsPerSecond(){
        return this.beatsPerSecond;
    }
    public float getBeatLength(){
        return 1.0f / beatsPerSecond;
    }
    
    public void setOffset(float offset){
        this.offset = offset;
    }
    public float getOffset(){
        return offset;
    }
    
    public void setAllowableErrorTime(float allowableErrorTime){
        this.allowableErrorTime = allowableErrorTime;
    }
    public float getAllowableErrorTime(){
        return allowableErrorTime;
    }
    
    public void setMusicFileName(String fileName){
        this.musicFileName = fileName;
    }
    public String getMusicFileName(){
        return musicFileName;
    }
    
    public void setMusicDirectoryHint(String musicDirectoryHint){
        this.musicDirectoryHint = musicDirectoryHint;
    }
    public String getMusicDirectoryHint(){
        return musicDirectoryHint;
    }
    
    public void setLength(float length){
        this.length = length;
    }
    public float getLength(){
        return length;
    }
    
    /*public void setHammerOnTime(float hammerOnTime){
        this.hammerOnTime = hammerOnTime;
    }
    public float getHammerOnTime(){
        return hammerOnTime;
    }
    
    public void setPullOffTime(float pullOffTime){
        this.pullOffTime = pullOffTime;
    }
    public float getPullOffTime(){
        return pullOffTime;
    }*/
    
    public void setDifficulty(Difficulty difficulty){
        this.difficulty = difficulty;
    }
    public Difficulty getDifficulty(){
        return difficulty;
    }
}
