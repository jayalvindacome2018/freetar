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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;

/**
 *
 * @author Anton
 */
public class ExceptionDisplayDialog extends JDialog{
    private JButton okButton;
    private ExceptionDisplayPanel exceptionDisplayPanel;
    
    /** Creates a new instance of ExceptionDisplayDialog */
    public ExceptionDisplayDialog(){
        this.getContentPane().setLayout(new BorderLayout());
        
        okButton = new JButton("OK");
        
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt){
                okButtonClicked();
            }
        });
        
        this.getContentPane().add(okButton, BorderLayout.PAGE_END);
        
        exceptionDisplayPanel = new ExceptionDisplayPanel();
        this.getContentPane().add(exceptionDisplayPanel, BorderLayout.CENTER);
        
        this.setModal(true);
        this.pack();
    }
    
    public void displayDialog(){
        this.setVisible(true);
    }
    
    public void setException(Exception ex, String message){
        exceptionDisplayPanel.setException(ex, message);
    }
    
    public void setException(Exception ex){
        setException(ex, "No Message Set");
    }
    
    private void okButtonClicked(){
        this.setVisible(false);
    }
    
}
