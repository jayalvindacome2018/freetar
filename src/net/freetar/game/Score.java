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
package net.freetar.game;

/**
 *
 * @author Anton
 */
public class Score {
    public static final int BASE_NOTE_VALUE = 10;
    public static final int HAMMER_BONUS = 5;
    public static final int HOLD_BONUS = 10;
    public static final float ROCK_MODE_MULTIPLIER = 2.0f;
    
    public static final int NUMBER_OF_NOTES_PER_MULT_LEVEL = 10;
    public static final int MULTIPLIER_MAX = 4;
    
    private int score = 0;
    private int streak = 0;
    private int hitNotes = 0;
    private int missedNotes = 0;
    
    private float multiplier = 1;
    private float powerMeter = 0;
    private float rockMeter = 0;
    
    private boolean rockModeEnabled = false;
    
    /** Creates a new instance of ScoreManager */
    public Score() {
        reset();
    }
    
    public void reset(){
        score = 0;
        streak = 0;
        hitNotes = 0;
        missedNotes = 0;
        multiplier = 1;
        powerMeter = 0;
        rockMeter = 0;
    }
    
    public void setRockModeEnabled(boolean rockModeEnabled){
        this.rockModeEnabled = rockModeEnabled;
    }
    
    public int getScore(){
        return score;
    }
    
    public int getStreak(){
        return streak;
    }
    
    public int getHitNotes(){
        return hitNotes;
    }
    
    public int getMissedNote(){
        return missedNotes;
    }
    
    public float getPowerMeter(){
        return powerMeter;
    }
    
    public float getRockMeter(){
        return rockMeter;
    }
    
    public void hitNote(){
        score += BASE_NOTE_VALUE * getMultiplierLevel();
        streak++;
        hitNotes++;
        increaseMultiplier();
    }
    
    public void hammeredNote(){
        //hitNote();
        score += HAMMER_BONUS;
    }
    
    public void missedNote(){
        streak = 0;
        multiplier = 1;
        missedNotes++;
    }
    
    public int getMultiplierLevel(){
        return (int) multiplier;
    }
    
    private void increaseMultiplier(){
        if(multiplier < MULTIPLIER_MAX){
            multiplier += 1.0f / (float) NUMBER_OF_NOTES_PER_MULT_LEVEL;
        }
    }
    
    public void holdingNote(float timeSinceLastUpdate){
        score += HOLD_BONUS * timeSinceLastUpdate;
    }
}
