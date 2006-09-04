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


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Anton
 */
public class GamepadConfigDialog extends JDialog {
    public static enum ResultType {OKAY_OPTION, CANCEL_OPTION};
    private GamepadConfigPanel configPanel;
    private ResultType result = ResultType.CANCEL_OPTION;
    private ButtonConfig newButtonConfig;
    
    /** Creates a new instance of ControllerConfigDialog */
    public GamepadConfigDialog(){
        this(null);
    }
    
    public GamepadConfigDialog(ButtonConfig defaultButtonConfig) {
        this.setTitle("Configure Controller");
        this.setLayout(new BorderLayout());
        configPanel = new GamepadConfigPanel();
        
        JButton okayButton = new JButton("OK");
        okayButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                okayButtonPressed();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                cancelButtonPressed();
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,3));
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(okayButton);
        buttonPanel.add(cancelButton);
        this.add(new JLabel("\n"), BorderLayout.NORTH);
        this.add(configPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        if(defaultButtonConfig != null){
            configPanel.setButtonConfig(defaultButtonConfig);
        }
        
        this.setModal(true);
        //this.setAlwaysOnTop(true);
        this.pack();
    }
    
    private void cancelButtonPressed(){
        this.dispose();
    }
    
    private void okayButtonPressed(){
        result = ResultType.OKAY_OPTION;
        this.dispose();
    }
    
    public ResultType getResult(){
        return result;
    }
    
    public ButtonConfig getButtonConfig(){
        return configPanel.getButtonConfig();
    }
    
    public ResultType displayDialog(){
        this.pack();
        this.center();
        this.setVisible(true);
        return this.getResult();
    }
    
    private void center(){
        int x, y;
        x = (Toolkit.getDefaultToolkit().getScreenSize().width - this.getWidth()) / 2;
        y = (Toolkit.getDefaultToolkit().getScreenSize().height - this.getHeight()) / 2;
        this.setLocation(x, y);
    }
    
    
    public void setButtonConfig(ButtonConfig aConfig){
        configPanel.setButtonConfig(aConfig);
    }
}
