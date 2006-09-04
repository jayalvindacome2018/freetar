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

import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import javax.swing.ImageIcon;
import net.freetar.Song;
import net.freetar.SongProperties;
import net.freetar.bgmusic.BackgroundMusic;
import net.freetar.bgmusic.BackgroundMusic.MusicException;
import net.freetar.game.tests.CommandLineLauncher;
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
import com.jme.app.AbstractGame;
import com.jme.app.BaseGame;
import com.jme.system.DisplaySystem;
import com.jme.system.PropertiesIO;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author  Anton
 */
public class Launcher extends javax.swing.JFrame {
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.freetar.game.Launcher");
    private static final File CONTROLLER_SETTINGS_FILE = new File("controller.ini");
    
    private File songFile = null;
    private File musicFile = null;
    private File buttonFile = null;
    
    private Song song = null;
    private BackgroundMusic music = null;
    private ButtonConfig buttonConfig = null;
    private PropertiesIO graphicsSettings;
    private ExceptionDisplayDialog exceptionDisplayDialog;
    
    /** Creates new form Launchwer */
    public Launcher() {
        exceptionDisplayDialog = new ExceptionDisplayDialog();
        
        initComponents();
        
        //Load the graphics settings
        graphicsSettings = new PropertiesIO("properties.cfg");
        boolean loadedOK = graphicsSettings.load();
        if(!loadedOK){
            graphicsSettings.save(
                    PropertiesIO.DEFAULT_WIDTH,
                    PropertiesIO.DEFAULT_HEIGHT,
                    PropertiesIO.DEFAULT_DEPTH,
                    PropertiesIO.DEFAULT_FREQ,
                    PropertiesIO.DEFAULT_FULLSCREEN,
                    PropertiesIO.DEFAULT_RENDERER
                    );
        }
        
        updateGraphicsInfoArea();
        loadDefaultButtonConfig();
        updateButtonConfigInfoArea();
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
            
            launchGameButton.setEnabled(false);
        }else{
            SongProperties prop = song.getProperties();
            this.musicFileTextField.setText(prop.getMusicFileName());
            this.songNameTextField.setText(prop.getTitle());
            this.songLengthTextField.setText(prop.getLength() + "");
            this.numberOfNotesTextField.setText(song.getAllNotes().size() + "");
            this.difficultyTextField.setText(prop.getDifficulty() + "");
            
            if(buttonConfig != null){
                launchGameButton.setEnabled(true);
            }
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
            if(song != null && music != null){
                launchGameButton.setEnabled(true);
            }
        }else{
            this.controllerTextField.setText("");
            launchGameButton.setEnabled(false);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        launchGameButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        songPanel = new javax.swing.JPanel();
        changeSongButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        musicFileTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        numberOfNotesTextField = new javax.swing.JTextField();
        songNameTextField = new javax.swing.JTextField();
        songLengthTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        difficultyTextField = new javax.swing.JTextField();
        cusomizePanel = new javax.swing.JPanel();
        skinComboBox = new javax.swing.JComboBox();
        skinPreviewLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        optionsPanel = new javax.swing.JPanel();
        graphicsOptionsPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        resolutionComboBox = new javax.swing.JComboBox();
        colorDepthComboBox = new javax.swing.JComboBox();
        frequencyComboBox = new javax.swing.JComboBox();
        fullScreenCheckBox = new javax.swing.JCheckBox();
        controllerOptionsPanel = new javax.swing.JPanel();
        controllerTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        configureControllerButton = new javax.swing.JButton();
        gameOptionPanel = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        logoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Freetar Hero - Beta Version 0.1");
        setResizable(false);
        launchGameButton.setText("Launch Game");
        launchGameButton.setEnabled(false);
        launchGameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchGameButtonActionPerformed(evt);
            }
        });

        changeSongButton.setText("Change Song");
        changeSongButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeSongButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Music Filename:");

        musicFileTextField.setEditable(false);

        jLabel2.setText("Song Name:");

        jLabel3.setText("Song Length:");

        jLabel4.setText("Number Of Notes:");

        numberOfNotesTextField.setEditable(false);

        songNameTextField.setEditable(false);

        songLengthTextField.setEditable(false);

        jLabel5.setText("Difficulty:");

        difficultyTextField.setEditable(false);

        org.jdesktop.layout.GroupLayout songPanelLayout = new org.jdesktop.layout.GroupLayout(songPanel);
        songPanel.setLayout(songPanelLayout);
        songPanelLayout.setHorizontalGroup(
            songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(songPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(songPanelLayout.createSequentialGroup()
                        .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(musicFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                            .add(songNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                            .add(songLengthTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                            .add(numberOfNotesTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, difficultyTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, changeSongButton))
                .addContainerGap())
        );
        songPanelLayout.setVerticalGroup(
            songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(songPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(musicFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(songNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(songLengthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(numberOfNotesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(songPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(difficultyTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(changeSongButton)
                .add(123, 123, 123))
        );
        jTabbedPane1.addTab("Song", songPanel);

        skinComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default" }));
        skinComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinComboBoxActionPerformed(evt);
            }
        });

        skinPreviewLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        skinPreviewLabel.setIcon(new ImageIcon(Skin.getInstance().getResource("skinPreview.png")));
        skinPreviewLabel.setEnabled(false);

        jLabel12.setText("Skin:");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Coming Soon ...");

        org.jdesktop.layout.GroupLayout cusomizePanelLayout = new org.jdesktop.layout.GroupLayout(cusomizePanel);
        cusomizePanel.setLayout(cusomizePanelLayout);
        cusomizePanelLayout.setHorizontalGroup(
            cusomizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, cusomizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(cusomizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, skinPreviewLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, cusomizePanelLayout.createSequentialGroup()
                        .add(jLabel12)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(skinComboBox, 0, 260, Short.MAX_VALUE)))
                .addContainerGap())
        );
        cusomizePanelLayout.setVerticalGroup(
            cusomizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, cusomizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel13)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 133, Short.MAX_VALUE)
                .add(skinPreviewLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cusomizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(skinComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jTabbedPane1.addTab("Skins", cusomizePanel);

        graphicsOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Graphics Options"));
        jLabel8.setText("Resolution:");

        jLabel9.setText("Color Depth:");

        jLabel10.setText("Frequency:");

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

        org.jdesktop.layout.GroupLayout graphicsOptionsPanelLayout = new org.jdesktop.layout.GroupLayout(graphicsOptionsPanel);
        graphicsOptionsPanel.setLayout(graphicsOptionsPanelLayout);
        graphicsOptionsPanelLayout.setHorizontalGroup(
            graphicsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(graphicsOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(graphicsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(graphicsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(fullScreenCheckBox)
                    .add(frequencyComboBox, 0, 95, Short.MAX_VALUE)
                    .add(colorDepthComboBox, 0, 95, Short.MAX_VALUE)
                    .add(graphicsOptionsPanelLayout.createSequentialGroup()
                        .add(resolutionComboBox, 0, 95, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );

        graphicsOptionsPanelLayout.linkSize(new java.awt.Component[] {colorDepthComboBox, frequencyComboBox, resolutionComboBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        graphicsOptionsPanelLayout.setVerticalGroup(
            graphicsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(graphicsOptionsPanelLayout.createSequentialGroup()
                .add(graphicsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(resolutionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(graphicsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(colorDepthComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(graphicsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(frequencyComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fullScreenCheckBox))
        );

        controllerOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Controller Options"));
        controllerTextField.setEditable(false);

        jLabel6.setText("Controller:");

        configureControllerButton.setText("Setup");
        configureControllerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureControllerButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout controllerOptionsPanelLayout = new org.jdesktop.layout.GroupLayout(controllerOptionsPanel);
        controllerOptionsPanel.setLayout(controllerOptionsPanelLayout);
        controllerOptionsPanelLayout.setHorizontalGroup(
            controllerOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, controllerOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controllerTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configureControllerButton))
        );
        controllerOptionsPanelLayout.setVerticalGroup(
            controllerOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controllerOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(configureControllerButton)
                .add(jLabel6)
                .add(controllerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        gameOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Game Options"));
        jCheckBox1.setText("Lefty Flip");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setEnabled(false);
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout gameOptionPanelLayout = new org.jdesktop.layout.GroupLayout(gameOptionPanel);
        gameOptionPanel.setLayout(gameOptionPanelLayout);
        gameOptionPanelLayout.setHorizontalGroup(
            gameOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gameOptionPanelLayout.createSequentialGroup()
                .add(jCheckBox1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gameOptionPanelLayout.setVerticalGroup(
            gameOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gameOptionPanelLayout.createSequentialGroup()
                .add(jCheckBox1)
                .addContainerGap(84, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, controllerOptionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, optionsPanelLayout.createSequentialGroup()
                        .add(graphicsOptionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(gameOptionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(controllerOptionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(gameOptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(graphicsOptionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jTabbedPane1.addTab("Options", optionsPanel);

        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/FreetarLauncherSidebar.png")));
        logoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(logoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(launchGameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, logoLabel, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(launchGameButton)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void skinComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinComboBoxActionPerformed
        //TODO switch loaded JAR file after runtime
    }//GEN-LAST:event_skinComboBoxActionPerformed
    
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
        }catch(MusicException ex){
            handleException(ex);
            song = null;
            music = null;
            return;
        }
        
        this.musicFile = musicFile;
        this.songFile = songFile;
        
        updateSongInfoArea();
    }//GEN-LAST:event_changeSongButtonActionPerformed
    
    
    
    private void launchGameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchGameButtonActionPerformed
        //BaseGame app = new LaunchedGame(song, music, buttonConfig);
        //BaseGame app = new FreetarGame2(music, song, buttonConfig);
        this.dispose();
        try {
            FreetarGame.run(song, music, buttonConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
            handleException(ex);
        }
    }//GEN-LAST:event_launchGameButtonActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Launcher launcher = new Launcher();
                launcher.center();
                launcher.setVisible(true);
            }
        });
    }
    
    private void center(){
        int x, y;
        x = (Toolkit.getDefaultToolkit().getScreenSize().width - this.getWidth()) / 2;
        y = (Toolkit.getDefaultToolkit().getScreenSize().height - this.getHeight()) / 2;
        this.setLocation(x, y);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeSongButton;
    private javax.swing.JComboBox colorDepthComboBox;
    private javax.swing.JButton configureControllerButton;
    private javax.swing.JPanel controllerOptionsPanel;
    private javax.swing.JTextField controllerTextField;
    private javax.swing.JPanel cusomizePanel;
    private javax.swing.JTextField difficultyTextField;
    private javax.swing.JComboBox frequencyComboBox;
    private javax.swing.JCheckBox fullScreenCheckBox;
    private javax.swing.JPanel gameOptionPanel;
    private javax.swing.JPanel graphicsOptionsPanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton launchGameButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JTextField musicFileTextField;
    private javax.swing.JTextField numberOfNotesTextField;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JComboBox resolutionComboBox;
    private javax.swing.JComboBox skinComboBox;
    private javax.swing.JLabel skinPreviewLabel;
    private javax.swing.JTextField songLengthTextField;
    private javax.swing.JTextField songNameTextField;
    private javax.swing.JPanel songPanel;
    // End of variables declaration//GEN-END:variables
    
}
