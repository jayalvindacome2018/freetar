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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author  Anton
 */
public class GamepadConfigPanel extends javax.swing.JPanel {
    private static final Logger logger = Logger.getLogger("com.astruyk.music.input.ControllerConfigPanel");
    static{
        logger.addHandler(DebugHandler.getInstance().getDebugFileHandler());
    }
    /** Creates new form ControllerConfigPanel */
    public GamepadConfigPanel() {
        initComponents();
        initSetButtons();
        /*
        ActionListener[] listeners = controllerComboBox.getActionListeners();
        for(ActionListener al : listeners){
            controllerComboBox.removeActionListener(al);
        }
         */
        for(Gamepad g : GamepadManager.getGamepadManager().getSupportedGamepads()){
            controllerComboBox.addItem(g);
        }
        /*
        for(ActionListener al : listeners){
            controllerComboBox.addActionListener(al);
        }*/
    }
    
    private void initSetButtons(){
        setStartButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Start Button", startButtonComboBox);
            }
        });
        
        setSpecialButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Special Button", specialButtonComboBox);
            }
        });
        
        setNote4.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Play Note 4", note4ComboBox);
            }
        });
        
        setNote3.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Play Note 3", note3ComboBox);
            }
        });
        
        setNote2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Play Note 2", note2ComboBox);
            }
        });
        
        setNote1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Play Note 1", note1ComboBox);
            }
        });
        
        setNote0.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Play Note 0", note0ComboBox);
            }
        });
        
        setStrumUp.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Strum Up", strumUpComboBox);
            }
        });
        
        setStrumDown.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                askUserForComponent("Strum Down", strumDownComboBox);
            }
        });
        
    }
    
    private void askUserForComponent(String actionName, JComboBox comboBoxToSet){
        try{
            ButtonDetectDialog dialog = new ButtonDetectDialog("Test", (Gamepad) controllerComboBox.getSelectedItem());
            dialog.setVisible(true);
            if(dialog.getResult() == ButtonDetectDialog.ResultType.OKAY_OPTION){
                if(dialog.getButton() != null){
                    comboBoxToSet.setSelectedItem(dialog.getButton());
                }
            }
        }catch(ControllerNotSupportedException ex){
            //Should never see this, user can't select an unsupported controller
            displayErrorMessage("Should never see me: User selected unsupported controller");
        }
    }
    
    private void displayErrorMessage(String message){
        JOptionPane.showMessageDialog(this, message, "Warning!", JOptionPane.WARNING_MESSAGE);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        controllerComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        startButtonComboBox = new javax.swing.JComboBox();
        specialButtonComboBox = new javax.swing.JComboBox();
        note4ComboBox = new javax.swing.JComboBox();
        note3ComboBox = new javax.swing.JComboBox();
        note2ComboBox = new javax.swing.JComboBox();
        note1ComboBox = new javax.swing.JComboBox();
        note0ComboBox = new javax.swing.JComboBox();
        strumUpComboBox = new javax.swing.JComboBox();
        setStartButton = new javax.swing.JButton();
        setSpecialButton = new javax.swing.JButton();
        setNote4 = new javax.swing.JButton();
        setNote3 = new javax.swing.JButton();
        setNote2 = new javax.swing.JButton();
        setNote1 = new javax.swing.JButton();
        setNote0 = new javax.swing.JButton();
        setStrumUp = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        strumDownComboBox = new javax.swing.JComboBox();
        setStrumDown = new javax.swing.JButton();
        requireStrumCheckBox = new javax.swing.JCheckBox();

        jLabel1.setText("Controller Name");

        controllerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controllerComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Button Assignments"));
        jLabel2.setText("Start Button");

        jLabel3.setText("Special Button");

        jLabel4.setText("Play Note 4");

        jLabel5.setText("Play Note 3");

        jLabel6.setText("Play Note 2");

        jLabel7.setText("Play Note 1");

        jLabel8.setText("Play Note 0");

        jLabel9.setText("Strum Up");

        startButtonComboBox.setEnabled(false);

        specialButtonComboBox.setEnabled(false);

        note4ComboBox.setEnabled(false);

        note3ComboBox.setEnabled(false);

        note2ComboBox.setEnabled(false);

        note1ComboBox.setEnabled(false);

        note0ComboBox.setEnabled(false);

        strumUpComboBox.setEnabled(false);

        setStartButton.setText("Set");
        setStartButton.setEnabled(false);

        setSpecialButton.setText("Set");
        setSpecialButton.setEnabled(false);

        setNote4.setText("Set");
        setNote4.setEnabled(false);

        setNote3.setText("Set");
        setNote3.setEnabled(false);

        setNote2.setText("Set");
        setNote2.setEnabled(false);

        setNote1.setText("Set");
        setNote1.setEnabled(false);

        setNote0.setText("Set");
        setNote0.setEnabled(false);

        setStrumUp.setText("Set");
        setStrumUp.setEnabled(false);

        jLabel10.setText("Strum Down");

        strumDownComboBox.setEnabled(false);

        setStrumDown.setText("Set");
        setStrumDown.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel5)
                    .add(jLabel6)
                    .add(jLabel7)
                    .add(jLabel8)
                    .add(jLabel9)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(strumDownComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, note0ComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, note1ComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, note3ComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, startButtonComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, specialButtonComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, note4ComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, note2ComboBox, 0, 203, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, strumUpComboBox, 0, 203, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(setStartButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setSpecialButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setNote4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setNote3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setNote2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setNote1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setNote0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setStrumUp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(setStrumDown, 0, 0, Short.MAX_VALUE))
                .add(34, 34, 34))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(startButtonComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setStartButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(specialButtonComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setSpecialButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(note4ComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setNote4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(note3ComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setNote3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(note2ComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setNote2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(note1ComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setNote1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(note0ComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setNote0))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(strumUpComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setStrumUp))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(strumDownComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(setStrumDown)))
        );

        requireStrumCheckBox.setSelected(true);
        requireStrumCheckBox.setText("Require Strum To Play Notes");
        requireStrumCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireStrumCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(controllerComboBox, 0, 299, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, requireStrumCheckBox)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(controllerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(requireStrumCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void controllerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controllerComboBoxActionPerformed
        logger.info("User Selected " + controllerComboBox.getSelectedItem());
        Gamepad controller = (Gamepad) controllerComboBox.getSelectedItem();
        
        //Enabled all the button selectors
        addButtonsToComboBoxes(controller.getButtons());
    }//GEN-LAST:event_controllerComboBoxActionPerformed
    
    private JComboBox [] getComboBoxes(){
        return new JComboBox[] {
            startButtonComboBox,
            specialButtonComboBox,
            note4ComboBox,
            note3ComboBox,
            note2ComboBox,
            note1ComboBox,
            note0ComboBox,
            strumUpComboBox,
            strumDownComboBox
        };
    }
    
    private JButton[] getButtons(){
        return new JButton[] {
            setStartButton,
            setSpecialButton,
            setNote4,
            setNote3,
            setNote2,
            setNote1,
            setNote0,
            setStrumUp,
            setStrumDown
        };
    }
    
    private void addButtonsToComboBoxes(Button[] buttons){
        //Enable the 'set' buttons
        for(JButton cb : getButtons()){
            cb.setEnabled(true);
        }
        
        JComboBox[] comboBoxes = getComboBoxes();
        //Remove all the current items
        for(JComboBox cb : comboBoxes){
            cb.removeAllItems();
        }
        
        //If the button is null, we shoudl clear the list of items
        if(buttons == null){
            logger.info("Clearing Combo Boxes (Null received)");
            
            for(JComboBox currentBox : comboBoxes){
                currentBox.setEnabled(false);
            }
            return;
        }
        logger.info("Setting all combo-box items to list " + buttons);
        
        //Add all the identifiers to all the buttons
        for(Button currentButton : buttons){
            for(JComboBox currentBox : comboBoxes){
                currentBox.addItem(currentButton);
            }
        }
        
        //Enable all the boxes
        for(JComboBox currentBox : comboBoxes){
            currentBox.setEnabled(true);
        }
    }
    
    public void setButtonConfig(ButtonConfig buttonConfig){
        if(buttonConfig == null) return;
        //Set the controller combo box to the specified controller
        controllerComboBox.setSelectedItem(buttonConfig.getGamepad());
        if(controllerComboBox.getSelectedItem() != buttonConfig.getGamepad()){
            logger.warning("Specified gamepad isn't selected !!");
        }
        
        note0ComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_0));
        note1ComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_1));
        note2ComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_2));
        note3ComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_3));
        note4ComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_4));
        startButtonComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.PAUSE));
        specialButtonComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.SPECIAL));
        strumUpComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.STRUM_UP));
        strumDownComboBox.setSelectedItem(buttonConfig.getButtonFor(ButtonConfig.Action.STRUM_DOWN));
        requireStrumCheckBox.setSelected(buttonConfig.isStrumRequired());
    }
    
    protected void silentlySelectInComboBox(Object toSelect, JComboBox comboBox){
        ActionListener[] actionListeners = comboBox.getActionListeners();
        for(ActionListener a : actionListeners){
            comboBox.removeActionListener(a);
        }
        comboBox.setSelectedItem(toSelect);
        for(ActionListener a : actionListeners){
            comboBox.addActionListener(a);
        }
    }
    
    public ButtonConfig getButtonConfig(){
        ButtonConfig config = new ButtonConfig((Gamepad) controllerComboBox.getSelectedItem());
        config.assign((Button) startButtonComboBox.getSelectedItem(), ButtonConfig.Action.PAUSE);
        config.assign((Button) specialButtonComboBox.getSelectedItem(), ButtonConfig.Action.SPECIAL);
        config.assign((Button) strumUpComboBox.getSelectedItem(), ButtonConfig.Action.STRUM_UP);
        config.assign((Button) strumDownComboBox.getSelectedItem(), ButtonConfig.Action.STRUM_DOWN);
        config.assign((Button) note0ComboBox.getSelectedItem(), ButtonConfig.Action.TRACK_0);
        config.assign((Button) note1ComboBox.getSelectedItem(), ButtonConfig.Action.TRACK_1);
        config.assign((Button) note2ComboBox.getSelectedItem(), ButtonConfig.Action.TRACK_2);
        config.assign((Button) note3ComboBox.getSelectedItem(), ButtonConfig.Action.TRACK_3);
        config.assign((Button) note4ComboBox.getSelectedItem(), ButtonConfig.Action.TRACK_4);
        config.setStrumRequired(requireStrumCheckBox.isSelected());
        logger.info("Created Button Config - " + config);
        return config;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox controllerComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox note0ComboBox;
    private javax.swing.JComboBox note1ComboBox;
    private javax.swing.JComboBox note2ComboBox;
    private javax.swing.JComboBox note3ComboBox;
    private javax.swing.JComboBox note4ComboBox;
    private javax.swing.JCheckBox requireStrumCheckBox;
    private javax.swing.JButton setNote0;
    private javax.swing.JButton setNote1;
    private javax.swing.JButton setNote2;
    private javax.swing.JButton setNote3;
    private javax.swing.JButton setNote4;
    private javax.swing.JButton setSpecialButton;
    private javax.swing.JButton setStartButton;
    private javax.swing.JButton setStrumDown;
    private javax.swing.JButton setStrumUp;
    private javax.swing.JComboBox specialButtonComboBox;
    private javax.swing.JComboBox startButtonComboBox;
    private javax.swing.JComboBox strumDownComboBox;
    private javax.swing.JComboBox strumUpComboBox;
    // End of variables declaration//GEN-END:variables
}
