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

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.BillboardNode;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jmex.awt.swingui.JMEDesktop;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.JmeText;
import com.jmex.font3d.Text3D;
import com.jmex.font3d.TextFactory;
import com.jmex.font3d.effects.Font3DBorder;
import com.jmex.font3d.effects.Font3DGradient;
import com.sun.org.apache.bcel.internal.generic.MULTIANEWARRAY;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.freetar.game.tests.HUDPanel;
import net.freetar.util.SongUtils;

/**
 *
 * @author Anton
 */
public class JMEDesktopHUD implements GameHUD{
    private static final String LABEL_FONT_NAME = "fonts/textFont.ttf";
    private static final String NUMBER_FONT_NAME = "fonts/numberFont.ttf";
    
    private static final float LABEL_SIZE = 0.3f;
    private static final float SCORE_SIZE = 0.2f;
    private static final float SMALL_LABEL_SIZE = 0.2f;
    
    private Score myScore;
    
    private Node myRootNode; 
    private JMEDesktop jmeDesktop;
    private HUDPanel hudPanel;
    
    /** Creates a new instance of HeadsUpDisplay */
    public JMEDesktopHUD(Score myScore) {
        this.myScore = myScore;
        this.myRootNode = new Node("ScoreDisplayRootNode");
        
        final int width = DisplaySystem.getDisplaySystem().getWidth();
        final int height = DisplaySystem.getDisplaySystem().getHeight();
        
        jmeDesktop = new JMEDesktop("JMEDesktop", width, height, false, null);
        jmeDesktop.setLightCombineMode(LightState.OFF);
        myRootNode.attachChild(jmeDesktop);
        myRootNode.getLocalTranslation().z = 0.1f;
        
        //myRootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        init();
        
        myRootNode.updateRenderState();
        myRootNode.updateGeometricState(0, true);
    }
    
    private void init(){
        createSwingStuff();
        fullScreen();
    }
    
    protected void createSwingStuff() {
        final JDesktopPane desktopPane = jmeDesktop.getJDesktop();
        desktopPane.removeAll();
        
        desktopPane.setLayout(new BorderLayout());
                
        hudPanel = new HUDPanel();
        hudPanel.setOpaque(false);
        desktopPane.add(hudPanel, BorderLayout.CENTER);
        
        desktopPane.repaint();
        desktopPane.revalidate();
    }
    
    private void fullScreen(){
        final DisplaySystem display = DisplaySystem.getDisplaySystem();
        
        myRootNode.getLocalRotation().set( 0, 0, 0, 1 );
        myRootNode.getLocalTranslation().set( display.getWidth() / 2, display.getHeight() / 2, 0 );
        myRootNode.getLocalScale().set( 1, 1, 1 );
        myRootNode.setRenderQueueMode( Renderer.QUEUE_ORTHO );
    }
    
    private void perspective() {
        myRootNode.getLocalRotation().fromAngleNormalAxis( -0.7f, new Vector3f( 1, 0, 0 ) );
        myRootNode.setLocalScale( 24f / jmeDesktop.getJDesktop().getWidth() );
        myRootNode.getLocalTranslation().set( 0, 0, 0 );
        myRootNode.setRenderQueueMode( Renderer.QUEUE_TRANSPARENT );
    }
    
    public void setTime(float timeInSeconds){
        final int minutes = (int) timeInSeconds / 60;
        final int seconds = (int) timeInSeconds;
        final int millis = SongUtils.convertToMillis(timeInSeconds - seconds);

        Runnable updateTextRunnable = new Runnable(){
            public void run(){
                hudPanel.setTimeText(minutes + ":" + seconds + ":" + millis);
            }
        };
        SwingUtilities.invokeLater(updateTextRunnable);
        
    }
    
    public void update(float timePerFrame){
        //hudPanel.setScoreText(myScore.getScore() + "");
        //hudPanel.setStreakText(myScore.getStreak() + "");
        //hudPanel.setMultText(myScore.getMultiplierLevel() + "");
        //hudPanel.setTimeText("--:--:--");
    }
    
    public Node getRootNode(){
        return myRootNode;
    }
}
