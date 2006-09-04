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

package net.freetar.input;


import net.freetar.util.DebugHandler;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import net.java.games.input.Component;
import net.java.games.input.Controller;

/**
 *
 * @author Anton
 */
public class ButtonDetectDialog extends JDialog implements GamepadButtonListener{
    public static enum ResultType {OKAY_OPTION, CANCEL_OPTION};
    private static final Logger logger = DebugHandler.getLogger("ButtonDetectDialog");
    
    protected Button selectedButton = null;
    protected GamepadPoller poller = null;
    protected ButtonDetectPanel displayPanel = null;
    protected ResultType result = ResultType.CANCEL_OPTION;
    
    /** Creates a new instance of ButtonDetectDialog */
    public ButtonDetectDialog(String actionName, Gamepad gamepad) throws
            ControllerNotSupportedException {
        try{
            poller = new GamepadPoller(gamepad);
            poller.addButtonPressListener(this);
            poller.startPolling();
        }catch(ControllerNotSupportedException ex){
            logger.warning("Controller Not Supported - Dialog Creation Aborted");
            throw ex;
        }
        
        this.setLayout(new BorderLayout());
        
        displayPanel = new ButtonDetectPanel();
        JButton okayButton = new JButton("Okay");
        JButton cancelButton = new JButton("Cancel");
        
        okayButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                okayButtonPressed();
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                cancelButtonPressed();
            }
        });
        
        this.addWindowListener(new WindowListener(){
            public void windowClosing(WindowEvent e) {
                logger.info("Window Closing - Stopping Poller");
                poller.stopPolling();
                poller = null;
            }
            
            public void windowOpened(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
        });
        
        
        this.getContentPane().add(displayPanel, BorderLayout.NORTH);
        this.getContentPane().add(okayButton, BorderLayout.CENTER);
        this.getContentPane().add(cancelButton, BorderLayout.SOUTH);
        
        displayPanel.setActionName(actionName);
        
        this.setModal(true);
        this.pack();
        
        logger.setLevel(Level.WARNING);
    }
    
    public ResultType getResult(){
        return result;
    }
    
    private void cancelButtonPressed(){
        this.result = ResultType.CANCEL_OPTION;
        poller.stopPolling();
        poller = null;
        this.dispose();
    }
    
    private void okayButtonPressed(){
        this.result = ResultType.OKAY_OPTION;
        poller.stopPolling();
        poller = null;
        this.dispose();
    }
    
    public Button getButton(){
        return selectedButton;
    }
    
    public void buttonActionTriggered(ButtonEvent event) {
        logger.info("Handling Event: " + event);
        displayPanel.setButtonName("BUTTON: " + event.getButton());
        selectedButton = event.getButton();
    }
    
}
