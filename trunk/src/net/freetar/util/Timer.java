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

public class Timer {
	public static enum TimerState {STOPPED, RUNNING, PAUSED};
	private long startTime = 0;
	private long millisecondsSoFar = 0;
	private TimerState currentState;
        private float timeFactor = 1.0f;
	
	public Timer(){
		currentState = TimerState.STOPPED;
	}
	
	public void start(){
		currentState = TimerState.RUNNING;
		startTime = System.currentTimeMillis();
		millisecondsSoFar = 0;
	}
	
	public void pause(){
		if(currentState == TimerState.RUNNING){
			updateTimeSoFar();
			currentState = TimerState.PAUSED;
		}
	}
	
	public void resume(){
		if(currentState == TimerState.PAUSED){
			currentState = TimerState.RUNNING;
			startTime = System.currentTimeMillis();
		}
	}
	
	public void stop(){
		updateTimeSoFar();
		currentState = TimerState.STOPPED;
	}
	
	public float getTimeInSeconds(){
		updateTimeSoFar();
		return millisecondsSoFar / 1000f;
	}
	
	public TimerState getState(){
		return currentState;
	}
	
	public void reset(){
		currentState = TimerState.STOPPED;
		millisecondsSoFar = 0;
	}
	
	public void setTimerTime(float seconds){
		this.millisecondsSoFar = (long) (seconds * 1000f);
	}
        
        public void setTimeFactor(float timeFactor){
            this.timeFactor = timeFactor;
        }
	
	private void updateTimeSoFar(){
		if(currentState == TimerState.RUNNING){
			long currentMillis = System.currentTimeMillis();
			millisecondsSoFar += timeFactor * (currentMillis - startTime);
			startTime = currentMillis;
		}
	}
}
