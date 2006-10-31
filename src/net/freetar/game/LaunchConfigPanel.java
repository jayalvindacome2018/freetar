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

import com.jme.system.DisplaySystem;
import com.jme.system.PropertiesIO;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import net.freetar.Song;
import net.freetar.SongProperties;
import net.freetar.bgmusic.BackgroundMusic;
import net.freetar.input.ButtonConfig;
import net.freetar.input.ControllerNotFoundException;
import net.freetar.input.ControllerNotSupportedException;
import net.freetar.input.GamepadConfigDialog;
import net.freetar.io.FileFormatException;
import net.freetar.io.IncorrectVersionException;
import net.freetar.util.DebugHandler;
import net.freetar.util.ExceptionDisplayDialog;
import net.freetar.util.FileUtils;
import net.freetar.util.GeneralUtils;
import net.freetar.util.SongUtils;
import net.freetar.util.StringFileFilter;
import net.freetar.util.UnsupportedVersionException;

/**
 *
 * @author  Anton
 */
public class LaunchConfigPanel extends javax.swing.JPanel {
    private static final Logger logger = DebugHandler.getLogger("net.freetar.game.LauncherPanel");
    private static final File CONTROLLER_SETTINGS_FILE = new File("controller.ini");
    
    private File songFile = null;
    private File musicFile = null;
    private File buttonFile = null;
    
    private Song song = null;
    private BackgroundMusic music = null;
    private ButtonConfig buttonConfig = null;
    private PropertiesIO graphicsSettings;
    private ExceptionDisplayDialog exceptionDisplayDialog;
    
    
    /** Creates new form LauncherPanel */
    public LaunchConfigPanel() {
        exceptionDisplayDialog = new ExceptionDisplayDialog();
        
        initComponents();
        
        //Load the graphics settings
        graphicsSettings = new PropertiesIO("properties.cfg");
        graphicsSettings.load();
        updateGraphicsInfoArea();
        loadDefaultButtonConfig();
        updateButtonConfigInfoArea();
    }
    
    public ButtonConfig getButtonConfig(){
        return buttonConfig;
    }
    
    public Song getSong(){
        return song;
    }
    
    public BackgroundMusic getBackgroundMusic(){
        return music;
    }
    
    private void handleException(Exception ex){
        exceptionDisplayDialog.setException(ex);
        exceptionDisplayDialog.displayDialog();
        DebugHandler.logException(logger, ex);
    }
    
    public void loadDefaultButtonConfig(){
        this.buttonConfig = null;
        BufferedReader in = null;
        if(!CONTROLLER_SETTINGS_FILE.exists()){
            return;
        }
        try{
            in = new BufferedReader(new FileReader(CONTROLLER_SETTINGS_FILE));
            buttonConfig = ButtonConfig.createButtonConfigFrom(in);
        }catch(IOException ex){
            this.buttonConfig = null;
            handleException(ex);
        }catch(ControllerNotFoundException ex){
            this.buttonConfig = null;
            handleException(ex);
        }catch(ControllerNotSupportedException ex){
            this.buttonConfig = null;
            handleException(ex);
        } finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException ex) {
                    handleException(ex);
                }
            }
        }
    }
    
    private void updateSongInfoArea(){
        if(song == null || music == null){
            this.musicFileTextField.setText("");
            this.songNameTextField.setText("");
            this.songLengthTextField.setText("");
            this.numberOfNotesTextField.setText("");
            this.difficultyTextField.setText("");
        }else{
            SongProperties prop = song.getProperties();
            this.musicFileTextField.setText(prop.getMusicFileName());
            this.songNameTextField.setText(prop.getTitle());
            this.songLengthTextField.setText(prop.getLength() + "");
            this.numberOfNotesTextField.setText(song.getAllNotes().size() + "");
            this.difficultyTextField.setText(prop.getDifficulty() + "");
        }
    }
    
    private void updateGraphicsInfoArea(){
        //Disable the listeners
        ActionListener[] resListeners = GeneralUtils.disableListenersForComboBox(resolutionComboBox);
        ActionListener[] colorListeners = GeneralUtils.disableListenersForComboBox(colorDepthComboBox);
        ActionListener[] freqListeners = GeneralUtils.disableListenersForComboBox(frequencyComboBox);
        ActionListener[] fsListeners = fullScreenCheckBox.getActionListeners();
        for(ActionListener listener : fsListeners){
            fullScreenCheckBox.removeActionListener(listener);
        }
        
        //Change the settings
        resolutionComboBox.setSelectedItem(graphicsSettings.getWidth() + "x" + graphicsSettings.getHeight());
        colorDepthComboBox.setSelectedItem(graphicsSettings.getDepth() + " bpp");
        frequencyComboBox.setSelectedItem(graphicsSettings.getFreq() + " hz");
        fullScreenCheckBox.setSelected(graphicsSettings.getFullscreen());
        
        //Enable the listeners
        GeneralUtils.addActionListeners(resolutionComboBox, resListeners);
        GeneralUtils.addActionListeners(colorDepthComboBox, colorListeners);
        GeneralUtils.addActionListeners(frequencyComboBox, freqListeners);
        for(ActionListener listener : fsListeners){
            fullScreenCheckBox.addActionListener(listener);
        }
    }
    
    private void updateGraphicsSettings(){
        String display = (String)resolutionComboBox.getSelectedItem();
        int width = Integer.parseInt(display.substring(0, display.indexOf("x")));
        display = display.substring(display.indexOf("x") + 1);
        int height = Integer.parseInt(display);
        
        String depthString = (String)colorDepthComboBox.getSelectedItem();
        int depth = Integer.parseInt(depthString.substring(0, depthString.indexOf(" ")));
        
        String freqString = (String)frequencyComboBox.getSelectedItem();
        int freq = Integer.parseInt(freqString.substring(0, freqString.indexOf(" ")));
        
        boolean fullscreen = fullScreenCheckBox.isSelected();
        String renderer = graphicsSettings.getRenderer();
        
        //test valid display mode
        DisplaySystem disp = DisplaySystem.getDisplaySystem(renderer);
        boolean valid = (disp != null) ? disp.isValidDisplayMode(width, height, depth, freq) : false;
        
        if (valid) {
            //use the propertiesio class to save it.
            graphicsSettings.save(width, height, depth, freq, fullscreen, renderer);
            
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The selected display mode is not valid!",
                    "Invalid Mode",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateButtonConfigInfoArea(){
        if(buttonConfig != null){
            this.controllerTextField.setText(buttonConfig.getGamepad().getName());
        }else{
            this.controllerTextField.setText("");
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        musicFileTextField = new javax.swing.JTextField();
        songNameTextField = new javax.swing.JTextField();
        songLengthTextField = new javax.swing.JTextField();
        numberOfNotesTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        difficultyTextField = new javax.swing.JTextField();
        changeSongButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        configureControllerButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        controllerTextField = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        resolutionComboBox = new javax.swing.JComboBox();
        colorDepthComboBox = new javax.swing.JComboBox();
        frequencyComboBox = new javax.swing.JComboBox();
        fullScreenCheckBox = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/FreetarLauncherSidebar.png")));

        jLabel2.setText("Music Filename:");

        jLabel3.setText("Song Name:");

        jLabel4.setText("Song Length:");

        jLabel5.setText("Number Of Notes:");

        musicFileTextField.setEditable(false);

        songNameTextField.setEditable(false);

        songLengthTextField.setEditable(false);

        numberOfNotesTextField.setEditable(false);

        jLabel6.setText("Difficulty:");

        difficultyTextField.setEditable(false);

        changeSongButton.setText("Change Song");
        changeSongButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeSongButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(changeSongButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(musicFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .add(songNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .add(songLengthTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .add(numberOfNotesTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                            .add(difficultyTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {difficultyTextField, musicFileTextField, numberOfNotesTextField, songLengthTextField, songNameTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(musicFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(songNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(songLengthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(numberOfNotesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(difficultyTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(changeSongButton)
                .addContainerGap(68, Short.MAX_VALUE))
        );
        jTabbedPane1.addTab("Song", jPanel1);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/skinPreview.png")));
        jLabel7.setEnabled(false);

        jButton2.setText("Change Skin");
        jButton2.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 301, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jTabbedPane1.addTab("Skin", jPanel2);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Controller Options"));
        configureControllerButton.setText("Setup");
        configureControllerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureControllerButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Controller:");

        controllerTextField.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(jLabel8)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controllerTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configureControllerButton)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(controllerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(configureControllerButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Graphics Options"));
        jLabel9.setText("Resolution:");

        jLabel10.setText("Color Depth:");

        jLabel11.setText("Frequency:");

        resolutionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "640x480", "800x600", "1024x768", "1280x1024", "1600x1200" }));
        resolutionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolutionComboBoxActionPerformed(evt);
            }
        });

        colorDepthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "16 bpp", "24 bpp", "32 bpp" }));
        colorDepthComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorDepthComboBoxActionPerformed(evt);
            }
        });

        frequencyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0 hz", "60 hz", "70 hz", "75 hz", "80 hz", "85 hz" }));
        frequencyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frequencyComboBoxActionPerformed(evt);
            }
        });

        fullScreenCheckBox.setText("Fullscreen Mode");
        fullScreenCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fullScreenCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fullScreenCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullScreenCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(fullScreenCheckBox)
                    .add(colorDepthComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(frequencyComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resolutionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel5Layout.linkSize(new java.awt.Component[] {colorDepthComboBox, frequencyComboBox, resolutionComboBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(resolutionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(colorDepthComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(frequencyComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fullScreenCheckBox))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Game Options"));
        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 83, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(22, 22, 22))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jTabbedPane1.addTab("Options", jPanel3);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
        
    private void fullScreenCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullScreenCheckBoxActionPerformed
        updateGraphicsSettings();
    }//GEN-LAST:event_fullScreenCheckBoxActionPerformed
    
    private void frequencyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frequencyComboBoxActionPerformed
        updateGraphicsSettings();
    }//GEN-LAST:event_frequencyComboBoxActionPerformed
    
    private void colorDepthComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorDepthComboBoxActionPerformed
        updateGraphicsSettings();
    }//GEN-LAST:event_colorDepthComboBoxActionPerformed
    
    private void resolutionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboBoxActionPerformed
        updateGraphicsSettings();
    }//GEN-LAST:event_resolutionComboBoxActionPerformed
    
    private void changeSongButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeSongButtonActionPerformed
//Ask the user for the file containing the XML data
        File songFile = null;
        File musicFile = null;
        
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setDialogTitle("Select a SNG file");
        fileChooser.setFileFilter(FileUtils.createFileFilter("sng", "Song Files"));
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            songFile = fileChooser.getSelectedFile();
        }else{
            song = null;
            music = null;
            updateSongInfoArea();
            return;
        }
        
        try {
            //Load the song file
            song = SongUtils.loadFromFile(songFile);
        } catch (IOException ex) {
            handleException(ex);
            song = null;
            music = null;
            updateSongInfoArea();
            return;
        } catch (UnsupportedVersionException ex) {
            handleException(ex);
            song = null;
            music = null;
            updateSongInfoArea();
            return;
        } catch (IncorrectVersionException ex) {
            handleException(ex);
            song = null;
            music = null;
            updateSongInfoArea();
            return;
        } catch (FileFormatException ex) {
            handleException(ex);
            song = null;
            music = null;
            updateSongInfoArea();
            return;
        }
        
        //Try to load the music from the same directory as the songFile
        musicFile = new File(songFile.getParentFile().getPath() + "/" + song.getProperties().getMusicFileName());
        
        if(!musicFile.exists()){
            //Try to load the music from the music hint directory
            musicFile = new File(song.getProperties().getMusicDirectoryHint() + "/" + song.getProperties().getMusicFileName());
        }
        if(!musicFile.exists()){
            //Ask the user where the music file is
            musicFile = null;
            fileChooser.setDialogTitle("Please Locate '" + song.getProperties().getMusicFileName() + "' on your computer");
            fileChooser.addChoosableFileFilter(FileUtils.createFileFilter("MP3, OGG, WAV, MIDI, MID, WMA, FLAC", "Song Files (*.MP3, *.OGG, *.WAV, *.MIDI, *.MID, *.WMA, *.FLAC)"));
            fileChooser.setFileFilter(new StringFileFilter(song.getProperties().getMusicFileName()));
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setApproveButtonText("Open Music File");
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                musicFile = fileChooser.getSelectedFile();
            }else{
                song = null;
                music = null;
                updateSongInfoArea();
                return;
            }
        }
        try{
            music = BackgroundMusic.loadMusicFrom(musicFile);
        }catch(BackgroundMusic.MusicException ex){
            handleException(ex);
            song = null;
            music = null;
            return;
        }
        
        this.musicFile = musicFile;
        this.songFile = songFile;
        
        updateSongInfoArea();
    }//GEN-LAST:event_changeSongButtonActionPerformed
    
    private void configureControllerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureControllerButtonActionPerformed
        GamepadConfigDialog gamepadChooser = new GamepadConfigDialog(buttonConfig);
        GamepadConfigDialog.ResultType result = gamepadChooser.displayDialog();
        if(result == GamepadConfigDialog.ResultType.OKAY_OPTION){
            buttonConfig = gamepadChooser.getButtonConfig();
            
            //Try to save the new button config to the preferences file
            BufferedWriter out = null;
            if(CONTROLLER_SETTINGS_FILE.exists()){
                CONTROLLER_SETTINGS_FILE.delete();
            }
            try{
                out = new BufferedWriter(new FileWriter(CONTROLLER_SETTINGS_FILE));
                buttonConfig.saveButtonConfigTo(out);
            }catch(IOException ex){
                handleException(ex);
            }finally{
                try{
                    if(out != null){
                        out.flush();
                        out.close();
                    }
                }catch(IOException ex){
                    handleException(ex);
                }
            }
        }
        updateButtonConfigInfoArea();
    }//GEN-LAST:event_configureControllerButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeSongButton;
    private javax.swing.JComboBox colorDepthComboBox;
    private javax.swing.JButton configureControllerButton;
    private javax.swing.JTextField controllerTextField;
    private javax.swing.JTextField difficultyTextField;
    private javax.swing.JComboBox frequencyComboBox;
    private javax.swing.JCheckBox fullScreenCheckBox;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField musicFileTextField;
    private javax.swing.JTextField numberOfNotesTextField;
    private javax.swing.JComboBox resolutionComboBox;
    private javax.swing.JTextField songLengthTextField;
    private javax.swing.JTextField songNameTextField;
    // End of variables declaration//GEN-END:variables
    
}
