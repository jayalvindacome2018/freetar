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

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.system.DisplaySystem;
import com.jmex.font2d.Font2D;
import com.jmex.font2d.Text2D;
import java.awt.Font;
import net.freetar.util.SongUtils;

/**
 *
 * @author Anton
 */
public class Simple2DHud implements GameHUD {
    private Score score;
    private Node rootNode;
    
    private Text2D scoreText;
    private Text2D multText;
    private Text2D streakText;
    
    private Text2D timeText;
    
    /** Creates a new instance of Simple2DHud */
    public Simple2DHud(Score score) {
        this.score = score;
        
        rootNode = new Node("SimpleHUDRootNode");
        rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        
        setupHUDText();
        
        rootNode.updateGeometricState(0, true);
        rootNode.updateRenderState();
    }
    
    private void setupHUDText(){
        final Font2D labelFont = new Font2D("fonts/labelFont.png");
        final Font2D numberFont = new Font2D("fonts/numberFont.png");
        
        //Create the labels
        Text2D scoreLabel = labelFont.createText("SCORE", 10, Font.BOLD);
        Text2D multLabel = labelFont.createText("Mult", 10, Font.BOLD);
        Text2D streakLabel = labelFont.createText("Streak", 10, Font.BOLD);
        Text2D timeLabel = labelFont.createText("Time", 10, Font.BOLD);
        
        scoreText = numberFont.createText("0", 10, Font.BOLD);
        streakText = numberFont.createText("0", 10, Font.BOLD);
        multText = numberFont.createText("1 x", 10, Font.BOLD);
        timeText = numberFont.createText("0:0", 10, Font.BOLD);
        
        //Colors
        scoreLabel.setTextColor(ColorRGBA.red);
        multLabel.setTextColor(ColorRGBA.red);
        streakLabel.setTextColor(ColorRGBA.red);
        timeLabel.setTextColor(ColorRGBA.red);
        
        //Position Everything
        final DisplaySystem display = DisplaySystem.getDisplaySystem();
        final int halfWidth = display.getWidth() / 2;
        final int halfHeight = display.getHeight() / 2;
        
        scoreLabel.getLocalTranslation().set(   //Centered on top of screen
                halfWidth - scoreLabel.getWidth() / 2,
                display.getHeight() - scoreLabel.getHeight(),
                0
                );
        scoreText.getLocalTranslation().set(    //Centered below scoreLabel
                halfWidth - scoreText.getWidth() / 2,
                scoreLabel.getLocalTranslation().y  - scoreText.getHeight(),
                0);
        
        multLabel.getLocalTranslation().set(    //RIght-top corner
                display.getWidth() - multLabel.getWidth(),
                display.getHeight() - multLabel.getHeight(),
                0);
        multText.getLocalTranslation().set(     //Right-top corner below multLabel
                display.getWidth() - multText.getWidth(),
                multLabel.getLocalTranslation().y - multText.getHeight(),
                0);
        
        streakLabel.getLocalTranslation().set(  //Left-top corner
                0,
                display.getHeight() - streakLabel.getHeight(),
                0);
        streakText.getLocalTranslation().set(   //Left-edge below streakLabel
                0,
                streakLabel.getLocalTranslation().y - streakText.getHeight(),
                0);
        
        timeLabel.getLocalTranslation().set(    //Centered ABOVE timeText location
                halfWidth - timeLabel.getWidth() / 2,
                timeText.getHeight(),
                0);
        timeText.getLocalTranslation().set(     //Centered at bottom of screen
                halfWidth - timeText.getWidth() / 2,
                0,
                0);
        
        //Add to scenegraph
        rootNode.attachChild(scoreLabel);
        rootNode.attachChild(multLabel);
        rootNode.attachChild(streakLabel);
        rootNode.attachChild(timeLabel);
        
        rootNode.attachChild(scoreText);
        rootNode.attachChild(streakText);
        rootNode.attachChild(multText);
        rootNode.attachChild(timeText);
    }
    
    public void setTime(float timeInSeconds) {
        final int minutes = (int) timeInSeconds / 60;
        final int seconds = (int) timeInSeconds % 60;
        timeText.setText(minutes + ":" + seconds);
        
        //Re-center
        timeText.getLocalTranslation().set(     //Centered at bottom of screen
                DisplaySystem.getDisplaySystem().getWidth() / 2 - timeText.getWidth() / 2,
                0,
                0);
        timeText.updateGeometricState(0, true);
    }
    
    public Node getRootNode() {
        return rootNode;
    }
    
    public void update(float timePerFrame) {
        streakText.setText("" + score.getStreak());
        scoreText.setText("" + score.getScore());
        multText.setText("" + score.getMultiplierLevel());
        
        scoreText.getLocalTranslation().x = DisplaySystem.getDisplaySystem().getWidth() / 2 - scoreText.getWidth() / 2;
        multText.getLocalTranslation().x = DisplaySystem.getDisplaySystem().getWidth() - multText.getWidth();
        streakText.getLocalTranslation().x = 0;
        
        scoreText.updateGeometricState(0, true);
        multText.updateGeometricState(0, true);
        streakText.updateGeometricState(0, true);
    }
    
}
