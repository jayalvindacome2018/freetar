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

package net.freetar.editor;

import net.freetar.DuplicateNoteException;
import net.freetar.InvalidDurationException;
import net.freetar.Note;
import net.freetar.NoteChangeListener;
import net.freetar.NoteEvent;
import net.freetar.Song;
import net.freetar.SongProperties;
import net.freetar.TrackBasedSong;
import net.freetar.editor.commands.ChangeSongBPSCommand;
import net.freetar.editor.commands.MacroCommand;
import net.freetar.input.ButtonConfig;
import net.freetar.input.ButtonEvent;
import net.freetar.input.GamepadButtonListener;
import net.freetar.input.ControllerNotFoundException;
import net.freetar.input.ControllerNotSupportedException;
import net.freetar.input.GamepadPoller;
import net.freetar.noteStates.FinalState;
import net.freetar.noteStates.NoteHoldState;
import net.freetar.noteStates.NoteMissedState;
import net.freetar.noteStates.NotePlayedState;
import net.freetar.noteStates.NoteState;
import net.freetar.noteStates.PrematureReleaseState;
import net.freetar.noteStates.PressableState;
import net.freetar.util.DebugHandler;
import net.freetar.util.SongUtils;
import net.freetar.bgmusic.MusicListener;
import net.freetar.input.GamepadConfigDialog;
import net.freetar.util.FileUtils;
import net.freetar.util.StringFileFilter;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import net.freetar.bgmusic.BackgroundMusic;
import net.freetar.bgmusic.BackgroundMusic.MusicState;
import net.freetar.bgmusic.MidiMusic;
import net.freetar.bgmusic.BackgroundMusic.MusicException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeListener;
import java.awt.Cursor;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import net.freetar.editor.commands.AddNoteCommand;
import net.freetar.editor.commands.ChangeAlbumCommand;
import net.freetar.editor.commands.ChangeAllowableErrorTimeCommand;
import net.freetar.editor.commands.ChangeArtistCommand;
import net.freetar.editor.commands.ChangeDifficultyCommand;
import net.freetar.editor.commands.ChangeNoteDurationCommand;
import net.freetar.editor.commands.ChangeNoteHammerCommand;
import net.freetar.editor.commands.ChangeTitleCommand;
import net.freetar.editor.commands.ChangeTrackCommand;
import net.freetar.editor.commands.ChangeYearCommand;
import net.freetar.editor.commands.ClipBoard;
import net.freetar.editor.commands.Command;
import net.freetar.editor.commands.CopyNoteCommand;
import net.freetar.editor.commands.CutNoteCommand;
import net.freetar.editor.commands.DeleteNoteCommand;
import net.freetar.editor.commands.HistoryQueue;
import net.freetar.editor.commands.MoveNotesCommand;
import net.freetar.editor.commands.PasteNoteCommand;

/**
 *
 * @author  Temp
 */
public class MusicEditor extends javax.swing.JFrame
        implements SongPanListener, MusicListener, GamepadButtonListener, NoteChangeListener{
    //Used for debug logging
    private static final Logger logger = DebugHandler.getLogger("com.astruyk.music.editor.MusicEditor");
    
    private static final File CONTROLLER_SETTINGS_FILE = new File("controller.ini");
    
    BackgroundMusic music = null;       //The background music of the song currently being edited
    Song song = null;                   //The song currently being edited
    HistoryQueue historyQueue = null;   //A queue of all the commands that are undoable
    ActionMap actions = null;           //A map of actions that can be used
    ClipBoard clipboard = null;         //A clipboard to copy things to
    ButtonConfig buttonConfig = null;   //The current button configuration for playing back the song
    GamepadPoller poller = null;        //A Controller Poller to listen for button presses
    File currentFile = null;            //A pointer to the file currently being edited
    ButtonPressHandler playbackHandler = null;   //Handles the button pressedEvents (for edit/playback modes)
    
    /** Creates new form MusicEditor */
    public MusicEditor() {
        actions = new ActionMap();  //Has to be done before initActions() or will generate NullPointerExceptions
        historyQueue = new HistoryQueue();
        clipboard = new ClipBoard();
        
        playbackHandler = new DoNothingButtonHandler();
        
        initActions();              //NOTE: Has to be called BEFORE initComponents()
        setupActionAccelerators();  //NOTE: has to be called AFTER initActions() and BEFORE initComponents()
        initComponents();           //NOTE: Make sure the initActions() is called first
        setupIcons();               //NOTE: has to be called AFTER initActions() and initComponents()
        
        //Set the actions for the UI components that are not in menus
        //TODO move this code to the EDITOR portion of the program (here for easy access & testing)
        playButton.setAction(actions.get("playAction"));
        playButton.setText("");
        pauseButton.setAction(actions.get("pauseAction"));
        pauseButton.setText("");
        
        //TODO move the loading of preferences out of the constructor (let it show the damn UI first!)
        //Load the user preferences from the preferences file
        loadDefaultButtonConfig();
        initButtonConfig(buttonConfig);
        
        //Add this panel as a listener to the songDisplayPanel to listen for click-and-drag events
        songDisplayPanel.addSongPanListener(this);
        
        song = null;
        music = null;
        songLoaded();
    }
    
    private void initActions(){
        /*
         *Playback Actions
         */
        actions.put("playAction",  new AbstractAction("Play Music"){
            public void actionPerformed(ActionEvent e){
                if(song != null){
                    song.resetSong();
                }
                if(music != null){
                    try{
                        music.play();
                    }catch(MusicException ex){
                        displayExceptionDialog(ex, "Error Encountered When Attempting To Play Music");
                    }
                }else{
                    displayErrorMessage("No Song Loaded - Cannot Begin Playback");
                }
            }
        });
        
        actions.put("pauseAction", new AbstractAction("Pause Music"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    //If we're NOT pausing the music (ie. it is already paused) reset the notes
                    //if(song != null && music.getState() != BackgroundMusic.MusicState.PLAYING){
                    //    song.resetSong();
                    //}
                    try{
                        music.pause();
                    }catch(MusicException ex){
                        displayExceptionDialog(ex, "Error encountered when attempting to pause");
                    }
                }else{
                    displayErrorMessage("No Song Loaded - Cannot Pause Song");
                }
            }
        });
        
        actions.put("stopAction",  new AbstractAction("Stop Music"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    music.stop();
                }else{
                    displayErrorMessage("No Song Loaded - Cannot Stop Playback");
                }
            }
        });
        
        actions.put("skipToSongEndAction", new AbstractAction("Skip To End Of Song"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    try {
                        music.skipTo(music.getLength());
                    } catch (BackgroundMusic.MusicException ex) {
                        displayExceptionDialog(ex, "Error Encountered When Skipping To End Of Song");
                    }
                }else{
                    displayErrorMessage("No Song Loaded - Cannot Skip To End");
                }
            }
        });
        
        actions.put("skipToSongBeginingAction", new AbstractAction("Skip To Begining Of Song"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    try {
                        music.skipTo(0);
                    } catch (BackgroundMusic.MusicException ex) {
                        displayExceptionDialog(ex, "Error encountered when skipping to begining of song: \n" + ex.getMessage());
                    }
                }else{
                    displayErrorMessage("No Song Loaded - Cannot Skip To Begining");
                }
            }
        });
        
        actions.put("seekAheadAction", new AbstractAction("Seek Ahead 0.5s"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    try{
                        music.skipTo(music.getTimeInSeconds() + 0.5f);
                    }catch(BackgroundMusic.MusicException ex){
                        displayExceptionDialog(ex, "Could not skip ahead - " + ex.getMessage());
                    }
                }
            }
        });
        
        actions.put("seekBehindAction", new AbstractAction("Seek Behind 0.5s"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    try{
                        music.skipTo(music.getTimeInSeconds() - 0.5f);
                    }catch(BackgroundMusic.MusicException ex){
                        displayExceptionDialog(ex, "Could not skip behind - " + ex.getMessage());
                    }
                }
            }
        });
        
        actions.put("normalPlaybackAction", new AbstractAction("Normal Playback Speed"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    music.setTempoFactor(1.0f);
                }
            }
        });
        
        actions.put("slowPlaybackAction", new AbstractAction("1/2 Speed"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    music.setTempoFactor(0.5f);
                }
            }
        });
        
        actions.put("verySlowPlaybackAction", new AbstractAction("1/4 Speed"){
            public void actionPerformed(ActionEvent e){
                if(music != null){
                    music.setTempoFactor(0.25f);
                }
            }
        });
        
        actions.put("resetNotesAction", new AbstractAction("Reset Note States"){
            public void actionPerformed(ActionEvent e){
                if(song != null){
                    song.resetSong();
                    songDisplayPanel.repaint();
                }
            }
        });
        
        /*
         * Undo/Redo Actions
         */
        actions.put("undoAction", new AbstractAction("Undo"){
            public void actionPerformed(ActionEvent e){
                historyQueue.stepBackward();
                songDisplayPanel.repaint();
            }
        });
        
        actions.put("redoAction", new AbstractAction("Redo"){
            public void actionPerformed(ActionEvent e){
                historyQueue.stepForward();
                songDisplayPanel.repaint();
            }
        });
        
        /*
         * Note Editing Actions
         */
        actions.put("deleteAction",  new AbstractAction("Delete"){
            public void actionPerformed(ActionEvent e){
                Command deleteCommand = new DeleteNoteCommand(songDisplayPanel.getSelectedNotes(), song);
                executeCommand(deleteCommand);
                songDisplayPanel.clearSelectedNotes();
            }
        });
        
        actions.put("cutAction", new AbstractAction("Cut"){
            public void actionPerformed(ActionEvent e){
                Command cutCommand = new CutNoteCommand(songDisplayPanel.getSelectedNotes(), song, clipboard);
                executeCommand(cutCommand);
            }
        });
        
        actions.put("copyAction", new AbstractAction("Copy"){
            public void actionPerformed(ActionEvent e){
                Command copyCommand = new CopyNoteCommand(songDisplayPanel.getSelectedNotes(), clipboard);
                executeCommand(copyCommand);
            }
        });
        
        actions.put("pasteAction", new AbstractAction("Paste"){
            public void actionPerformed(ActionEvent e){
                PasteNoteCommand pasteCommand = new PasteNoteCommand(clipboard, song, music.getTimeInSeconds(), songDisplayPanel);
                executeCommand(pasteCommand);
                songDisplayPanel.clearSelectedNotes();
                songDisplayPanel.selectNotes(pasteCommand.getPastedNotes());
            }
        });
        
        actions.put("newNote0Action", new AbstractAction("Insert Note On Track 0"){
            public void actionPerformed(ActionEvent e){
                createNewNoteForButton(0);
            }
        });
        
        actions.put("newNote1Action", new AbstractAction("Insert Note On Track 1"){
            public void actionPerformed(ActionEvent e){
                createNewNoteForButton(1);
            }
        });
        
        actions.put("newNote2Action", new AbstractAction("Insert Note On Track 2"){
            public void actionPerformed(ActionEvent e){
                createNewNoteForButton(2);
            }
        });
        
        actions.put("newNote3Action", new AbstractAction("Insert Note On Track 3"){
            public void actionPerformed(ActionEvent e){
                createNewNoteForButton(3);
            }
        });
        
        actions.put("newNote4Action", new AbstractAction("Insert Note On Track 4"){
            public void actionPerformed(ActionEvent e){
                createNewNoteForButton(4);
            }
        });
        
        actions.put("moveNoteAheadAction", new AbstractAction("Move Forward"){
            public void actionPerformed(ActionEvent e){
                Command moveNoteAheadCommand = new MoveNotesCommand(
                        songDisplayPanel.getSelectedNotes(),
                        songDisplayPanel.getSnapTime(),
                        song);
                executeCommand(moveNoteAheadCommand);
            }
        });
        
        actions.put("moveNoteBehindAction", new AbstractAction("Move Backwards"){
            public void actionPerformed(ActionEvent e){
                Command c = new MoveNotesCommand(
                        songDisplayPanel.getSelectedNotes(),
                        -songDisplayPanel.getSnapTime(),
                        song);
                executeCommand(c);
            }
        });
        
        actions.put("moveNoteUpAction", new AbstractAction("Move Note Up"){
            public void actionPerformed(ActionEvent e){
                Collection<Note> selected = songDisplayPanel.getSelectedNotes();
                Command c = new ChangeTrackCommand(
                        selected,
                        1);
                executeCommand(c);
                //HACK to ensure notes are drawn as selected after they change
                //Tracks - the Tracks weren't being informed when the notes
                //states were changing
                songDisplayPanel.clearSelectedNotes();
                songDisplayPanel.selectNotes(selected);
            }
        });
        
        actions.put("moveNoteDownAction", new AbstractAction("Move note Down"){
            public void actionPerformed(ActionEvent e){
                Collection<Note> selected = songDisplayPanel.getSelectedNotes();
                Command c = new ChangeTrackCommand(
                        selected,
                        -1);
                executeCommand(c);
                
                //HACK to ensure notes are drawn as selected after they change
                //Tracks
                songDisplayPanel.clearSelectedNotes();
                songDisplayPanel.selectNotes(selected);
            }
        });
        
        actions.put("increaseDurationAction", new AbstractAction("Increase Duration"){
            public void actionPerformed(ActionEvent e){
                Command c = new ChangeNoteDurationCommand(
                        songDisplayPanel.getSelectedNotes(),
                        songDisplayPanel.getSnapTime());
                executeCommand(c);
            }
        });
        
        actions.put("decreaseDurationAction", new AbstractAction("Decrease Duration"){
            public void actionPerformed(ActionEvent e){
                Command c = new ChangeNoteDurationCommand(
                        songDisplayPanel.getSelectedNotes(),
                        -songDisplayPanel.getSnapTime());
                executeCommand(c);
            }
        });
        
        /*
         * Selection Actions
         */
        
        actions.put("selectAllAction", new AbstractAction("Select All"){
            public void actionPerformed(ActionEvent e){
                songDisplayPanel.selectNotes(song.getAllNotes());
            }
        });
        
        actions.put("selectNoneAction", new AbstractAction("Selecte None"){
            public void actionPerformed(ActionEvent e){
                songDisplayPanel.clearSelectedNotes();
            }
        });
        
        /*
         * Zoom Actions
         */
        actions.put("zoomInAction", new AbstractAction("Zoom In"){
            public void actionPerformed(ActionEvent e){
                songDisplayPanel.increaseZoom();
            }
        });
        
        actions.put("zoomOutAction", new AbstractAction("Zoom Out"){
            public void actionPerformed(ActionEvent e){
                songDisplayPanel.decreaseZoom();
            }
        });
        
        actions.put("resetZoomAction", new AbstractAction("Reset Zoom"){
            public void actionPerformed(ActionEvent e){
                songDisplayPanel.resetZoom();
            }
        });
    }
    
    private void setupActionAccelerators(){
        //Set up the accelerators
        //Playback
        setAccelerator("playAction", "control SPACE");
        setAccelerator("pauseAction", "SPACE");
        setAccelerator("stopAction", "ESCAPE");
        setAccelerator("skipToSongEndAction", "END");
        setAccelerator("skipToSongBeginingAction", "HOME");
        setAccelerator("seekAheadAction", "shift RIGHT");
        setAccelerator("seekBehindAction", "shift LEFT");
        setAccelerator("normalPlaybackAction", "control 1");
        setAccelerator("slowPlaybackAction", "control 2");
        setAccelerator("verySlowPlaybackAction", "control 3");
        setAccelerator("resetNotesAction", "control R");
        //Editing
        setAccelerator("undoAction", "control Z");
        setAccelerator("redoAction", "control shift Z");
        setAccelerator("copyAction", "control C");
        setAccelerator("cutAction", "control X");
        setAccelerator("pasteAction", "control V");
        setAccelerator("newNote0Action", "1");
        setAccelerator("newNote1Action", "2");
        setAccelerator("newNote2Action", "3");
        setAccelerator("newNote3Action", "4");
        setAccelerator("newNote4Action", "5");
        setAccelerator("deleteAction", "DELETE");
        setAccelerator("moveNoteAheadAction", "RIGHT");
        setAccelerator("moveNoteBehindAction", "LEFT");
        setAccelerator("moveNoteUpAction", "UP");
        setAccelerator("moveNoteDownAction", "DOWN");
        setAccelerator("increaseDurationAction", "control RIGHT");
        setAccelerator("decreaseDurationAction", "control LEFT");
        //Selection
        setAccelerator("selectAllAction", "control A");
        setAccelerator("selectNoneAction", "control D");
        //View
        setAccelerator("zoomInAction", "EQUALS");
        setAccelerator("zoomOutAction", "MINUS");
        setAccelerator("resetZoomAction", "BACK_SPACE");
    }
    
    private void setupIcons(){
        setIcon("playAction", "playIcon.png");
        setIcon("pauseAction", "pauseIcon.png");
        setIcon("stopAction", "stopIcon.png");
        setIcon("skipToSongEndAction", "seekEndIcon.png");
        setIcon("skipToSongBeginingAction", "seekStartIcon.png");
        setIcon("seekAheadAction", "seekAheadIcon.png");
        setIcon("seekBehindAction", "seekBehindIcon.png");
        setIcon("normalPlaybackAction", "normalSpeedIcon.png");
        setIcon("slowPlaybackAction", "slowSpeedIcon.png");
        setIcon("verySlowPlaybackAction", "verySlowSpeedIcon.png");
        setIcon("resetNotesAction", "resetSongStateIcon.png");
        setIcon("undoAction", "undoIcon.png");
        setIcon("redoAction", "redoIcon.png");
        setIcon("copyAction", "copyIcon.png");
        setIcon("cutAction", "cutIcon.png");
        setIcon("deleteAction", "deleteIcon.png");
        setIcon("pasteAction", "pasteIcon.png");
        setIcon("moveNoteAheadAction", "moveNoteAheadIcon.png");
        setIcon("moveNoteBehindAction", "moveNoteBehindIcon.png");
        setIcon("moveNoteUpAction", "moveNoteUpIcon.png");
        setIcon("moveNoteDownAction", "moveNoteDownIcon.png");
        setIcon("increaseDurationAction", "increaseDurationIcon.png");
        setIcon("decreaseDurationAction", "decreaseDurationIcon.png");
        setIcon("zoomInAction", "zoomInIcon.png");
        setIcon("zoomOutAction", "zoomOutIcon.png");
        setIcon("resetZoomAction", "resetZoomIcon.png");
    }
    
    //Helper method for setting the icons to actions
    private void setIcon(String actionName, String iconName){
        try {
            final Action action = actions.get(actionName);
            final URL iconURL = this.getClass().getClassLoader().getResource(iconName);
            final ImageIcon imageIcon = new ImageIcon(ImageIO.read(iconURL));
            action.putValue(Action.SMALL_ICON, imageIcon);
        } catch (Exception ex) {
            displayExceptionDialog(ex, "Error Setting Icon to " + iconName + " for action " + actionName + ":\n" + ex);
        }
    }
    
    //Helper method for setting accelerators to actions
    private void setAccelerator(String actionName, String keyStrokeString){
        Action a = actions.get(actionName);
        if(a != null){
            a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStrokeString));
        }
    }
    
    public void executeCommand(Command c){
        if(c == null) return;
        c.execute();
        historyQueue.addToHistory(c);
        actions.get("undoAction").setEnabled(historyQueue.canStepBackward());
        actions.get("redoAction").setEnabled(historyQueue.canStepForward());
        this.saveMenuItem.setEnabled(true);
        this.repaint();
    }
    
    protected void initButtonConfig(ButtonConfig buttonConfig){
        if(buttonConfig == null) return;
        
        //Help Java Garbage collection
        if(this.buttonConfig != null){
            this.buttonConfig = null;
        }
        
        //Disable the poller that is running already, and remove ourselves from
        //its listener list, and then help Java GC by making it null
        if(poller != null){
            poller.stopPolling();
            poller.removeButtonPressListener(this);
            poller = null;
        }
        
        //Setup the link to the new buttonConfig and create and activate a new
        //controller poller to create callbacks
        this.buttonConfig = buttonConfig;
        try{
            poller = new GamepadPoller(buttonConfig.getGamepad());
            poller.addButtonPressListener(this);
            poller.startPolling();
        }catch(ControllerNotSupportedException ex){
            displayExceptionDialog(ex, "Cannot Use Indicated Buttons - Controller Not Supported (" + ex.getMessage() + ")");
        }
    }
    
    public void loadDefaultButtonConfig(){
        this.buttonConfig = null;
        BufferedReader in = null;
        if(!CONTROLLER_SETTINGS_FILE.exists()){
            displayErrorMessage("Could not load controller.ini\n Please set controller config in options menu.");
            return;
        }
        try{
            in = new BufferedReader(new FileReader(CONTROLLER_SETTINGS_FILE));
            buttonConfig = ButtonConfig.createButtonConfigFrom(in);
        }catch(IOException ex){
            this.buttonConfig = null;
            ex.printStackTrace();
            displayExceptionDialog(ex, "Error Loading Controller Preferences from file " + CONTROLLER_SETTINGS_FILE.getAbsolutePath());
        }catch(ControllerNotFoundException ex){
            this.buttonConfig = null;
            displayErrorMessage("Could not find default Controller.\n"
                    + "Set Gamepad using the Options->Configure Controller menu.");
        }catch(ControllerNotSupportedException ex){
            this.buttonConfig = null;
            displayErrorMessage("The controller " + ex.getController() + " is not supported. Reason: \n" + ex.getMessage());
        } finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException ex) {
                    displayExceptionDialog(ex, "Error Closing Controller Preferences File " + CONTROLLER_SETTINGS_FILE.getAbsolutePath());
                }
            }
        }
    }
    
    public Action getAction(String actionName){
        return actions.get(actionName);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        speedSelectionButtonGroup = new javax.swing.ButtonGroup();
        playbackModeButtonGroup = new javax.swing.ButtonGroup();
        difficultyButtonGroup = new javax.swing.ButtonGroup();
        exceptionDialog = new javax.swing.JDialog();
        okayButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        exceptionStackTraceArea = new javax.swing.JTextArea();
        exceptionMessageTextArea = new javax.swing.JTextField();
        exceptionNameTextArea = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        exceptionUserMessageArea = new javax.swing.JTextArea();
        songPropertiesDialog = new javax.swing.JDialog();
        songPropertiesCancelButton = new javax.swing.JButton();
        songPropertiesOkButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        artistTextField = new javax.swing.JTextField();
        albumTextField = new javax.swing.JTextField();
        yearSpinner = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        buttonPressTimeSpinner = new javax.swing.JSpinner();
        beatsPerSecondSpinner = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        difficultyNotSetRadioButton = new javax.swing.JRadioButton();
        easyDifficultyRadioButton = new javax.swing.JRadioButton();
        mediumDifficultyRadioButton = new javax.swing.JRadioButton();
        hardDifficultyRadioButton = new javax.swing.JRadioButton();
        expertDifficultyRadioButton = new javax.swing.JRadioButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        difficultyDescriptionTextArea = new javax.swing.JTextArea();
        aboutDialog = new javax.swing.JDialog();
        closeAboutDialogButton = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        noteDisplayPanel = new javax.swing.JPanel();
        activeTracksPanel = new javax.swing.JPanel();
        track4ActivePanel = new javax.swing.JPanel();
        track3ActivePanel = new javax.swing.JPanel();
        track2ActivePanel = new javax.swing.JPanel();
        track1ActivePanel = new javax.swing.JPanel();
        track0ActivePanel = new javax.swing.JPanel();
        songDisplayPanel = new net.freetar.editor.SongDisplayPanel();
        musicControlPanel = new javax.swing.JPanel();
        seekSlider = new javax.swing.JSlider();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        toolBar = new javax.swing.JToolBar();
        cutButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        pasteButton = new javax.swing.JButton();
        undoButton = new javax.swing.JButton();
        redoButton = new javax.swing.JButton();
        jSeparator10 = new javax.swing.JSeparator();
        resetSongButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newSongMenuItem = new javax.swing.JMenuItem();
        openSongDataMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteNoteMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        newNote4MenuItem = new javax.swing.JMenuItem();
        newNote3MenuItem = new javax.swing.JMenuItem();
        newNote2MenuItem = new javax.swing.JMenuItem();
        newNote1MenuItem = new javax.swing.JMenuItem();
        newNote0MenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        moveNoteAheadMenuItem = new javax.swing.JMenuItem();
        moveNoteBehindMenuItem = new javax.swing.JMenuItem();
        moveNoteUpMenuItem = new javax.swing.JMenuItem();
        moveNoteDownMenuItem = new javax.swing.JMenuItem();
        increaseDurationMenuItem = new javax.swing.JMenuItem();
        decreaseDurationMenuItem = new javax.swing.JMenuItem();
        enableHammerOnMenuItem = new javax.swing.JMenuItem();
        disableHammerOnMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        songPropertiesMenuItem = new javax.swing.JMenuItem();
        selectMenu = new javax.swing.JMenu();
        selectAllMenuItem = new javax.swing.JMenuItem();
        selectNoneMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        resetZoomMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        enableNoteLineCheckItem = new javax.swing.JCheckBoxMenuItem();
        drawPressTimesCheckItem = new javax.swing.JCheckBoxMenuItem();
        drawErrorBarsCheckItem = new javax.swing.JCheckBoxMenuItem();
        greenTrackOnBottomCheckItem = new javax.swing.JCheckBoxMenuItem();
        disablePitchShiftCheckItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        configureControllerItem = new javax.swing.JMenuItem();
        playbackMenu = new javax.swing.JMenu();
        normalSpeedMenuItem = new javax.swing.JRadioButtonMenuItem();
        halfSpeedMenuItem = new javax.swing.JRadioButtonMenuItem();
        quarterSpeedMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        playMenuItem = new javax.swing.JMenuItem();
        pauseMenuItem = new javax.swing.JMenuItem();
        stopMenuItem = new javax.swing.JMenuItem();
        skipToBeginingMenuItem = new javax.swing.JMenuItem();
        skipToEndMenuItem = new javax.swing.JMenuItem();
        seekAheadMenuItem = new javax.swing.JMenuItem();
        seekBehindMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        resetSongMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        doNothingModeMenuItem = new javax.swing.JRadioButtonMenuItem();
        practiceModeMenuItem = new javax.swing.JRadioButtonMenuItem();
        practiceStrumModeMenuItem = new javax.swing.JRadioButtonMenuItem();
        strumRecordingMenuItem = new javax.swing.JRadioButtonMenuItem();
        buttonPressRecordingMenuItem = new javax.swing.JRadioButtonMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        okayButton.setText("Dismiss This Dialog");
        okayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okayButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Error Information"));
        exceptionStackTraceArea.setColumns(20);
        exceptionStackTraceArea.setEditable(false);
        exceptionStackTraceArea.setRows(5);
        jScrollPane1.setViewportView(exceptionStackTraceArea);

        exceptionMessageTextArea.setEditable(false);
        exceptionMessageTextArea.setText("No Message Set");

        exceptionNameTextArea.setEditable(false);
        exceptionNameTextArea.setText("No Name Set");

        jLabel1.setText("Exception Name:");

        jLabel2.setText("Message:");

        jLabel5.setText("Stack Trace:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(jLabel5))
                .add(4, 4, 4)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .add(exceptionMessageTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .add(exceptionNameTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel5}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(exceptionNameTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(exceptionMessageTextArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel5)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("OH NO! An Error Occured In The Program!");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Below is some information to help track down the root of the problem!");

        jLabel6.setText("Message To User:");

        exceptionUserMessageArea.setColumns(20);
        exceptionUserMessageArea.setEditable(false);
        exceptionUserMessageArea.setRows(5);
        jScrollPane2.setViewportView(exceptionUserMessageArea);

        org.jdesktop.layout.GroupLayout exceptionDialogLayout = new org.jdesktop.layout.GroupLayout(exceptionDialog.getContentPane());
        exceptionDialog.getContentPane().setLayout(exceptionDialogLayout);
        exceptionDialogLayout.setHorizontalGroup(
            exceptionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(exceptionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(exceptionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                    .add(jLabel6)
                    .add(exceptionDialogLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, okayButton))
                .addContainerGap())
        );
        exceptionDialogLayout.setVerticalGroup(
            exceptionDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, exceptionDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(okayButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        songPropertiesDialog.setTitle("Song Properties");
        songPropertiesCancelButton.setText("Cancel");
        songPropertiesCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                songPropertiesCancelButtonActionPerformed(evt);
            }
        });

        songPropertiesOkButton.setText("OK");
        songPropertiesOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                songPropertiesOkButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Song Properties"));
        jLabel7.setText("Title:");

        titleTextField.setText("jTextField1");

        jLabel8.setText("Artist:");

        jLabel9.setText("Album:");

        jLabel10.setText("Year:");

        artistTextField.setText("jTextField2");

        albumTextField.setText("jTextField3");

        yearSpinner.setModel(new SpinnerNumberModel(0, 0, 2006, 1));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(jLabel8)
                    .add(jLabel9)
                    .add(jLabel10))
                .add(14, 14, 14)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(titleTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(artistTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(albumTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, yearSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {jLabel10, jLabel7, jLabel8, jLabel9}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(titleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(artistTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(albumTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(yearSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Properties"));
        jLabel11.setText("Beats Per Second:");

        jLabel12.setText("Allowable Error Time:");

        buttonPressTimeSpinner.setModel(new SpinnerNumberModel(0.25, 0.0f, 1.0f, 0.01f));

        beatsPerSecondSpinner.setModel(new SpinnerNumberModel(16.0, 1.0f, 32.0f, 0.5f));

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel11)
                .add(6, 6, 6)
                .add(beatsPerSecondSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 24, Short.MAX_VALUE)
                .add(jLabel12)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPressTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {beatsPerSecondSpinner, buttonPressTimeSpinner}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.linkSize(new java.awt.Component[] {jLabel11, jLabel12}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel11)
                .add(buttonPressTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel12)
                .add(beatsPerSecondSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Difficulty"));
        difficultyButtonGroup.add(difficultyNotSetRadioButton);
        difficultyNotSetRadioButton.setSelected(true);
        difficultyNotSetRadioButton.setText("Not Set");
        difficultyNotSetRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        difficultyNotSetRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        difficultyNotSetRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                difficultyNotSetRadioButtonActionPerformed(evt);
            }
        });

        difficultyButtonGroup.add(easyDifficultyRadioButton);
        easyDifficultyRadioButton.setText("Easy");
        easyDifficultyRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        easyDifficultyRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        easyDifficultyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                easyDifficultyRadioButtonActionPerformed(evt);
            }
        });

        difficultyButtonGroup.add(mediumDifficultyRadioButton);
        mediumDifficultyRadioButton.setText("Medium");
        mediumDifficultyRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mediumDifficultyRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mediumDifficultyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediumDifficultyRadioButtonActionPerformed(evt);
            }
        });

        difficultyButtonGroup.add(hardDifficultyRadioButton);
        hardDifficultyRadioButton.setText("Hard");
        hardDifficultyRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        hardDifficultyRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hardDifficultyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hardDifficultyRadioButtonActionPerformed(evt);
            }
        });

        difficultyButtonGroup.add(expertDifficultyRadioButton);
        expertDifficultyRadioButton.setText("Expert");
        expertDifficultyRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        expertDifficultyRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        expertDifficultyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expertDifficultyRadioButtonActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        difficultyDescriptionTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        difficultyDescriptionTextArea.setColumns(20);
        difficultyDescriptionTextArea.setEditable(false);
        difficultyDescriptionTextArea.setLineWrap(true);
        difficultyDescriptionTextArea.setRows(5);
        difficultyDescriptionTextArea.setWrapStyleWord(true);
        difficultyDescriptionTextArea.setEnabled(false);
        jScrollPane3.setViewportView(difficultyDescriptionTextArea);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(difficultyNotSetRadioButton)
                    .add(easyDifficultyRadioButton)
                    .add(hardDifficultyRadioButton)
                    .add(mediumDifficultyRadioButton)
                    .add(expertDifficultyRadioButton))
                .add(18, 18, 18)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jScrollPane3)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4Layout.createSequentialGroup()
                        .add(difficultyNotSetRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(easyDifficultyRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mediumDifficultyRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(hardDifficultyRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(expertDifficultyRadioButton)))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout songPropertiesDialogLayout = new org.jdesktop.layout.GroupLayout(songPropertiesDialog.getContentPane());
        songPropertiesDialog.getContentPane().setLayout(songPropertiesDialogLayout);
        songPropertiesDialogLayout.setHorizontalGroup(
            songPropertiesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(songPropertiesDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(songPropertiesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(songPropertiesDialogLayout.createSequentialGroup()
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, songPropertiesDialogLayout.createSequentialGroup()
                        .add(songPropertiesOkButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(songPropertiesCancelButton)
                        .addContainerGap())
                    .add(songPropertiesDialogLayout.createSequentialGroup()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        songPropertiesDialogLayout.linkSize(new java.awt.Component[] {songPropertiesCancelButton, songPropertiesOkButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        songPropertiesDialogLayout.setVerticalGroup(
            songPropertiesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, songPropertiesDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(songPropertiesDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(songPropertiesOkButton)
                    .add(songPropertiesCancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        aboutDialog.setTitle("Freetar Hero Editor - V0.2 Beta");
        aboutDialog.setAlwaysOnTop(true);
        aboutDialog.setModal(true);
        closeAboutDialogButton.setText("OK");
        closeAboutDialogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAboutDialogButtonActionPerformed(evt);
            }
        });

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freetarLogo.png")));
        jLabel13.setToolTipText("Freetar Hero - Play It Your Way...");

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/antonLogo.png")));
        jLabel15.setToolTipText("www.antonstruyk.com");

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("www.AntonStruyk.com");

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Freetar Hero Editor - V0.2 Beta");

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Web: http://freetar.antonstruyk.com");

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Programmed By - Anton Struyk");

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Special Thanks To Jon \"Doctorb\" Hillyard");

        org.jdesktop.layout.GroupLayout aboutDialogLayout = new org.jdesktop.layout.GroupLayout(aboutDialog.getContentPane());
        aboutDialog.getContentPane().setLayout(aboutDialogLayout);
        aboutDialogLayout.setHorizontalGroup(
            aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, aboutDialogLayout.createSequentialGroup()
                .add(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(closeAboutDialogButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .add(aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel16, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel13))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel17, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel18, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel19, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, aboutDialogLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)))
                .addContainerGap())
        );
        aboutDialogLayout.setVerticalGroup(
            aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(aboutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel14)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel17)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel18)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel19)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel15)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel16)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closeAboutDialogButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Music Editor v0.2 BETA");
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        noteDisplayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Notes"));
        activeTracksPanel.setLayout(new java.awt.GridLayout(6, 1));

        track4ActivePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        org.jdesktop.layout.GroupLayout track4ActivePanelLayout = new org.jdesktop.layout.GroupLayout(track4ActivePanel);
        track4ActivePanel.setLayout(track4ActivePanelLayout);
        track4ActivePanelLayout.setHorizontalGroup(
            track4ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        track4ActivePanelLayout.setVerticalGroup(
            track4ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 39, Short.MAX_VALUE)
        );
        activeTracksPanel.add(track4ActivePanel);

        track3ActivePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        org.jdesktop.layout.GroupLayout track3ActivePanelLayout = new org.jdesktop.layout.GroupLayout(track3ActivePanel);
        track3ActivePanel.setLayout(track3ActivePanelLayout);
        track3ActivePanelLayout.setHorizontalGroup(
            track3ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        track3ActivePanelLayout.setVerticalGroup(
            track3ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 39, Short.MAX_VALUE)
        );
        activeTracksPanel.add(track3ActivePanel);

        track2ActivePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        org.jdesktop.layout.GroupLayout track2ActivePanelLayout = new org.jdesktop.layout.GroupLayout(track2ActivePanel);
        track2ActivePanel.setLayout(track2ActivePanelLayout);
        track2ActivePanelLayout.setHorizontalGroup(
            track2ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        track2ActivePanelLayout.setVerticalGroup(
            track2ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 39, Short.MAX_VALUE)
        );
        activeTracksPanel.add(track2ActivePanel);

        track1ActivePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        org.jdesktop.layout.GroupLayout track1ActivePanelLayout = new org.jdesktop.layout.GroupLayout(track1ActivePanel);
        track1ActivePanel.setLayout(track1ActivePanelLayout);
        track1ActivePanelLayout.setHorizontalGroup(
            track1ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        track1ActivePanelLayout.setVerticalGroup(
            track1ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 39, Short.MAX_VALUE)
        );
        activeTracksPanel.add(track1ActivePanel);

        track0ActivePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        org.jdesktop.layout.GroupLayout track0ActivePanelLayout = new org.jdesktop.layout.GroupLayout(track0ActivePanel);
        track0ActivePanel.setLayout(track0ActivePanelLayout);
        track0ActivePanelLayout.setHorizontalGroup(
            track0ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 16, Short.MAX_VALUE)
        );
        track0ActivePanelLayout.setVerticalGroup(
            track0ActivePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 39, Short.MAX_VALUE)
        );
        activeTracksPanel.add(track0ActivePanel);

        org.jdesktop.layout.GroupLayout noteDisplayPanelLayout = new org.jdesktop.layout.GroupLayout(noteDisplayPanel);
        noteDisplayPanel.setLayout(noteDisplayPanelLayout);
        noteDisplayPanelLayout.setHorizontalGroup(
            noteDisplayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, noteDisplayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(songDisplayPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(activeTracksPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        noteDisplayPanelLayout.setVerticalGroup(
            noteDisplayPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(activeTracksPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, songDisplayPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
        );

        musicControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Song Controls"));
        seekSlider.setMajorTickSpacing(10);
        seekSlider.setMaximum(10);
        seekSlider.setMinorTickSpacing(1);
        seekSlider.setPaintTicks(true);
        seekSlider.setSnapToTicks(true);
        seekSlider.setValue(0);
        seekSlider.setFocusable(false);
        seekSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                seekSliderStateChanged(evt);
            }
        });

        playButton.setText("Play");
        playButton.setFocusable(false);

        pauseButton.setText("Pause");
        pauseButton.setFocusable(false);

        org.jdesktop.layout.GroupLayout musicControlPanelLayout = new org.jdesktop.layout.GroupLayout(musicControlPanel);
        musicControlPanel.setLayout(musicControlPanelLayout);
        musicControlPanelLayout.setHorizontalGroup(
            musicControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, musicControlPanelLayout.createSequentialGroup()
                .add(seekSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                .add(16, 16, 16)
                .add(playButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pauseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        musicControlPanelLayout.setVerticalGroup(
            musicControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(musicControlPanelLayout.createSequentialGroup()
                .add(musicControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(musicControlPanelLayout.createSequentialGroup()
                        .addContainerGap(11, Short.MAX_VALUE)
                        .add(musicControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(pauseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(playButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(seekSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
                .addContainerGap())
        );

        toolBar.setFloatable(false);
        cutButton.setAction(actions.get("cutAction"));
        cutButton.setFocusable(false);
        toolBar.add(cutButton);

        copyButton.setAction(actions.get("copyAction"));
        copyButton.setFocusable(false);
        toolBar.add(copyButton);

        pasteButton.setAction(actions.get("pasteAction"));
        pasteButton.setFocusable(false);
        toolBar.add(pasteButton);

        undoButton.setAction(actions.get("undoAction"));
        undoButton.setFocusable(false);
        toolBar.add(undoButton);

        redoButton.setAction(actions.get("redoAction"));
        redoButton.setFocusable(false);
        toolBar.add(redoButton);

        toolBar.add(jSeparator10);

        resetSongButton.setAction(actions.get("resetNotesAction"));
        resetSongButton.setFocusable(false);
        toolBar.add(resetSongButton);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        newSongMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newSongMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/newIcon.png")));
        newSongMenuItem.setMnemonic('N');
        newSongMenuItem.setText("New Song ...");
        newSongMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSongMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newSongMenuItem);

        openSongDataMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openSongDataMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/openIcon.png")));
        openSongDataMenuItem.setMnemonic('O');
        openSongDataMenuItem.setText("Open Song ...");
        openSongDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSongDataMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openSongDataMenuItem);

        fileMenu.add(jSeparator2);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/saveIcon.png")));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText("Save");
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/saveAsIcon.png")));
        saveAsMenuItem.setMnemonic('A');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setEnabled(false);
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);

        fileMenu.add(jSeparator1);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/exitIcon.png")));
        exitMenuItem.setMnemonic('X');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Edit");
        undoMenuItem.setAction(actions.get("undoAction"));
        editMenu.add(undoMenuItem);

        redoMenuItem.setAction(actions.get("redoAction"));
        editMenu.add(redoMenuItem);

        editMenu.add(jSeparator3);

        cutMenuItem.setAction(actions.get("cutAction"));
        editMenu.add(cutMenuItem);

        copyMenuItem.setAction(actions.get("copyAction"));
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAction(actions.get("pasteAction"));
        editMenu.add(pasteMenuItem);

        deleteNoteMenuItem.setAction(actions.get("deleteAction"));
        editMenu.add(deleteNoteMenuItem);

        editMenu.add(jSeparator6);

        newNote4MenuItem.setAction(actions.get("newNote4Action"));
        editMenu.add(newNote4MenuItem);

        newNote3MenuItem.setAction(actions.get("newNote3Action"));
        editMenu.add(newNote3MenuItem);

        newNote2MenuItem.setAction(actions.get("newNote2Action"));
        editMenu.add(newNote2MenuItem);

        newNote1MenuItem.setAction(actions.get("newNote1Action"));
        editMenu.add(newNote1MenuItem);

        newNote0MenuItem.setAction(actions.get("newNote0Action"));
        editMenu.add(newNote0MenuItem);

        editMenu.add(jSeparator5);

        moveNoteAheadMenuItem.setAction(actions.get("moveNoteAheadAction"));
        editMenu.add(moveNoteAheadMenuItem);

        moveNoteBehindMenuItem.setAction(actions.get("moveNoteBehindAction"));
        editMenu.add(moveNoteBehindMenuItem);

        moveNoteUpMenuItem.setAction(actions.get("moveNoteUpAction"));
        editMenu.add(moveNoteUpMenuItem);

        moveNoteDownMenuItem.setAction(actions.get("moveNoteDownAction"));
        editMenu.add(moveNoteDownMenuItem);

        increaseDurationMenuItem.setAction(actions.get("increaseDurationAction"));
        editMenu.add(increaseDurationMenuItem);

        decreaseDurationMenuItem.setAction(actions.get("decreaseDurationAction"));
        editMenu.add(decreaseDurationMenuItem);

        enableHammerOnMenuItem.setText("Enable Hammer Ons");
        enableHammerOnMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableHammerOnMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(enableHammerOnMenuItem);

        disableHammerOnMenuItem.setText("Disable Hammer Ons");
        disableHammerOnMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableHammerOnMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(disableHammerOnMenuItem);

        editMenu.add(jSeparator11);

        songPropertiesMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/songPropertiesIcon.png")));
        songPropertiesMenuItem.setText("Song Properties");
        songPropertiesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                songPropertiesMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(songPropertiesMenuItem);

        menuBar.add(editMenu);

        selectMenu.setText("Select");
        selectAllMenuItem.setAction(actions.get("selectAllAction"));
        selectMenu.add(selectAllMenuItem);

        selectNoneMenuItem.setAction(actions.get("selectNoneAction"));
        selectMenu.add(selectNoneMenuItem);

        menuBar.add(selectMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");
        zoomInMenuItem.setAction(actions.get("zoomInAction"));
        viewMenu.add(zoomInMenuItem);

        zoomOutMenuItem.setAction(actions.get("zoomOutAction"));
        viewMenu.add(zoomOutMenuItem);

        resetZoomMenuItem.setAction(actions.get("resetZoomAction"));
        viewMenu.add(resetZoomMenuItem);

        menuBar.add(viewMenu);

        optionsMenu.setMnemonic('O');
        optionsMenu.setText("Options");
        enableNoteLineCheckItem.setText("Draw Precise Note Lines");
        enableNoteLineCheckItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableNoteLineCheckItemActionPerformed(evt);
            }
        });

        optionsMenu.add(enableNoteLineCheckItem);

        drawPressTimesCheckItem.setText("Draw Note Press Times");
        drawPressTimesCheckItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawPressTimesCheckItemActionPerformed(evt);
            }
        });

        optionsMenu.add(drawPressTimesCheckItem);

        drawErrorBarsCheckItem.setText("Draw Note Error Bars");
        drawErrorBarsCheckItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawErrorBarsCheckItemActionPerformed(evt);
            }
        });

        optionsMenu.add(drawErrorBarsCheckItem);

        greenTrackOnBottomCheckItem.setSelected(true);
        greenTrackOnBottomCheckItem.setText("Green Track On Bottom");
        greenTrackOnBottomCheckItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenTrackOnBottomCheckItemActionPerformed(evt);
            }
        });

        optionsMenu.add(greenTrackOnBottomCheckItem);

        disablePitchShiftCheckItem.setText("Disable Pitch Shift");
        disablePitchShiftCheckItem.setEnabled(false);
        optionsMenu.add(disablePitchShiftCheckItem);

        optionsMenu.add(jSeparator7);

        configureControllerItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/configureControllerIcon.png")));
        configureControllerItem.setText("Configure Controller");
        configureControllerItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureControllerItemActionPerformed(evt);
            }
        });

        optionsMenu.add(configureControllerItem);

        menuBar.add(optionsMenu);

        playbackMenu.setMnemonic('P');
        playbackMenu.setText("Playback");
        normalSpeedMenuItem.setAction(actions.get("normalPlaybackAction"));
        speedSelectionButtonGroup.add(normalSpeedMenuItem);
        normalSpeedMenuItem.setSelected(true);
        normalSpeedMenuItem.setToolTipText("");
        playbackMenu.add(normalSpeedMenuItem);

        halfSpeedMenuItem.setAction(actions.get("slowPlaybackAction"));
        speedSelectionButtonGroup.add(halfSpeedMenuItem);
        halfSpeedMenuItem.setToolTipText("");
        playbackMenu.add(halfSpeedMenuItem);

        quarterSpeedMenuItem.setAction(actions.get("verySlowPlaybackAction"));
        speedSelectionButtonGroup.add(quarterSpeedMenuItem);
        quarterSpeedMenuItem.setToolTipText("");
        playbackMenu.add(quarterSpeedMenuItem);

        playbackMenu.add(jSeparator4);

        playMenuItem.setAction(actions.get("playAction"));
        playbackMenu.add(playMenuItem);

        pauseMenuItem.setAction(actions.get("pauseAction"));
        playbackMenu.add(pauseMenuItem);

        stopMenuItem.setAction(actions.get("stopAction"));
        playbackMenu.add(stopMenuItem);

        skipToBeginingMenuItem.setAction(actions.get("skipToSongBeginingAction"));
        playbackMenu.add(skipToBeginingMenuItem);

        skipToEndMenuItem.setAction(actions.get("skipToSongEndAction"));
        playbackMenu.add(skipToEndMenuItem);

        seekAheadMenuItem.setAction(actions.get("seekAheadAction"));
        playbackMenu.add(seekAheadMenuItem);

        seekBehindMenuItem.setAction(actions.get("seekBehindAction"));
        playbackMenu.add(seekBehindMenuItem);

        playbackMenu.add(jSeparator8);

        resetSongMenuItem.setAction(actions.get("resetNotesAction"));
        resetSongMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resetSongStateIcon.png")));
        playbackMenu.add(resetSongMenuItem);

        playbackMenu.add(jSeparator9);

        playbackModeButtonGroup.add(doNothingModeMenuItem);
        doNothingModeMenuItem.setSelected(true);
        doNothingModeMenuItem.setText("Do Nothing Mode");
        doNothingModeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doNothingModeMenuItemActionPerformed(evt);
            }
        });

        playbackMenu.add(doNothingModeMenuItem);

        playbackModeButtonGroup.add(practiceModeMenuItem);
        practiceModeMenuItem.setText("Practice Mode");
        practiceModeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                practiceModeMenuItemActionPerformed(evt);
            }
        });

        playbackMenu.add(practiceModeMenuItem);

        playbackModeButtonGroup.add(practiceStrumModeMenuItem);
        practiceStrumModeMenuItem.setText("Practice Strum Timing Mode");
        practiceStrumModeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                practiceStrumModeMenuItemActionPerformed(evt);
            }
        });

        playbackMenu.add(practiceStrumModeMenuItem);

        playbackModeButtonGroup.add(strumRecordingMenuItem);
        strumRecordingMenuItem.setText("Press Strum To Record Mode");
        strumRecordingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strumRecordingMenuItemActionPerformed(evt);
            }
        });

        playbackMenu.add(strumRecordingMenuItem);

        playbackModeButtonGroup.add(buttonPressRecordingMenuItem);
        buttonPressRecordingMenuItem.setText("Press Button To Record Mode");
        buttonPressRecordingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressRecordingMenuItemActionPerformed(evt);
            }
        });

        playbackMenu.add(buttonPressRecordingMenuItem);

        menuBar.add(playbackMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 692, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(musicControlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(noteDisplayPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noteDisplayPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(musicControlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void disableHammerOnMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableHammerOnMenuItemActionPerformed
        Command disableHammerCommand = new ChangeNoteHammerCommand(songDisplayPanel.getSelectedNotes(), false);
        executeCommand(disableHammerCommand);
    }//GEN-LAST:event_disableHammerOnMenuItemActionPerformed
    
    private void enableHammerOnMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableHammerOnMenuItemActionPerformed
        Command enableHammerCommand = new ChangeNoteHammerCommand(songDisplayPanel.getSelectedNotes(), true);
        executeCommand(enableHammerCommand);
    }//GEN-LAST:event_enableHammerOnMenuItemActionPerformed
    
    private void expertDifficultyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expertDifficultyRadioButtonActionPerformed
        difficultyDescriptionTextArea.setText("Expert difficulty uses all the buttons and follows "
                + "the song as closely as possible. Fast-changing chords and complex trills are "
                + "the name of the game, just make sure its still playable :).");
    }//GEN-LAST:event_expertDifficultyRadioButtonActionPerformed
    
    private void hardDifficultyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hardDifficultyRadioButtonActionPerformed
        difficultyDescriptionTextArea.setText("Hard difficulty songs use all the buttons on " +
                "the guitar. Chords are allowed, but should be fairly simple. Transitions " +
                "between chords should be fairly simple. Hammer-ons and Pull-Offs are acceptable " +
                "in the solo's.");
    }//GEN-LAST:event_hardDifficultyRadioButtonActionPerformed
    
    private void mediumDifficultyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mediumDifficultyRadioButtonActionPerformed
        difficultyDescriptionTextArea.setText("Medium difficulty songs use the first four buttons " +
                "(green, red, yellow, blue) and follow the song moderatley well. Easy chords are " +
                "acceptable, but only rarely.");
    }//GEN-LAST:event_mediumDifficultyRadioButtonActionPerformed
    
    private void easyDifficultyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_easyDifficultyRadioButtonActionPerformed
        difficultyDescriptionTextArea.setText("Easy difficulty songs should use only" +
                " the first three buttons (green, red, yellow) and have very slow" +
                " transitions. No chords or hammer-ons should be required.");
    }//GEN-LAST:event_easyDifficultyRadioButtonActionPerformed
    
    private void difficultyNotSetRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_difficultyNotSetRadioButtonActionPerformed
        difficultyDescriptionTextArea.setText("No Difficulty Specified");
    }//GEN-LAST:event_difficultyNotSetRadioButtonActionPerformed
    
    private void practiceStrumModeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_practiceStrumModeMenuItemActionPerformed
        this.setNewButtonHandler(new BeatPracticeModeHandler());
    }//GEN-LAST:event_practiceStrumModeMenuItemActionPerformed
    
    private void greenTrackOnBottomCheckItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenTrackOnBottomCheckItemActionPerformed
        songDisplayPanel.reorderTrackDisplayPanels(!songDisplayPanel.isGreenOnBottom());
    }//GEN-LAST:event_greenTrackOnBottomCheckItemActionPerformed
    
    private void closeAboutDialogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAboutDialogButtonActionPerformed
        aboutDialog.setVisible(false);
    }//GEN-LAST:event_closeAboutDialogButtonActionPerformed
    
    private void songPropertiesOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_songPropertiesOkButtonActionPerformed
        //Use a macro to encapsulate all the changes into one command, for one-step undoing
        MacroCommand changePropertiesCommand = new MacroCommand();
        
        //Check and see if changing the BPS will cause any duplicate notes
        Song newSong = new TrackBasedSong();
        final float newBPS = ((Number) beatsPerSecondSpinner.getValue()).floatValue();
        newSong.getProperties().setBeatsPerSecond(newBPS);
        boolean conflictingNotes = false;
        for(Note n : song.getAllNotes()){
            try{
                newSong.addNote(n);
            }catch(DuplicateNoteException ex){
                conflictingNotes = true;
                break;
            }
        }
        
        //If it will, then warn the user
        if(conflictingNotes){
            int result = JOptionPane.showConfirmDialog(this,
                    "Changing the BeatsPerSecond will cause some notes to be shifted"
                    +"to the same time, and to be lost.\n\n" +
                    "Are you sure you want to change the BPS?",
                    "Confirm Data Loss",
                    JOptionPane.YES_NO_OPTION);
            if(result == JOptionPane.YES_OPTION){
                changePropertiesCommand.addCommand(new ChangeSongBPSCommand(newBPS, song));
            }
        }else{
            changePropertiesCommand.addCommand(new ChangeSongBPSCommand(newBPS, song));
        }
        
        //Add the other change commands to the macro
        //Title
        changePropertiesCommand.addCommand(
                new ChangeTitleCommand(titleTextField.getText(), song));
        //Artist
        changePropertiesCommand.addCommand(
                new ChangeArtistCommand(artistTextField.getText(), song));
        //Album
        changePropertiesCommand.addCommand(
                new ChangeAlbumCommand(albumTextField.getText(), song));
        //Year
        changePropertiesCommand.addCommand(
                new ChangeYearCommand((Integer) yearSpinner.getValue(), song));
        //AllowableError
        changePropertiesCommand.addCommand(
                new ChangeAllowableErrorTimeCommand(
                ((Number) buttonPressTimeSpinner.getValue()).floatValue(),
                song));
        
        if(easyDifficultyRadioButton.isSelected()){
            changePropertiesCommand.addCommand(
                    new ChangeDifficultyCommand(SongProperties.Difficulty.EASY, song));
        }else if(mediumDifficultyRadioButton.isSelected()){
            changePropertiesCommand.addCommand(
                    new ChangeDifficultyCommand(SongProperties.Difficulty.MEDIUM, song));
        }else if(hardDifficultyRadioButton.isSelected()){
            changePropertiesCommand.addCommand(
                    new ChangeDifficultyCommand(SongProperties.Difficulty.HARD, song));
        }else if(expertDifficultyRadioButton.isSelected()){
            changePropertiesCommand.addCommand(
                    new ChangeDifficultyCommand(SongProperties.Difficulty.EXPERT, song));
        }
        
        //Execute the command
        executeCommand(changePropertiesCommand);
        //Update the UI to reflect the changes
        this.setTitle("Freetar Editor (" + song.getProperties().getTitle() + ")");
        
        songPropertiesDialog.setVisible(false);
    }//GEN-LAST:event_songPropertiesOkButtonActionPerformed
    
    private void songPropertiesCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_songPropertiesCancelButtonActionPerformed
        //Don't do anything, because the action was cancelled
        songPropertiesDialog.setVisible(false);
    }//GEN-LAST:event_songPropertiesCancelButtonActionPerformed
    
    private void songPropertiesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_songPropertiesMenuItemActionPerformed
        //Set the data in the properties menu to be current
        if(song != null){
            titleTextField.setText(song.getProperties().getTitle());
            artistTextField.setText(song.getProperties().getArtist());
            albumTextField.setText(song.getProperties().getAlbum());
            yearSpinner.setValue(song.getProperties().getYear());
            
            beatsPerSecondSpinner.setValue(song.getProperties().getBeatsPerSecond());
            buttonPressTimeSpinner.setValue(song.getProperties().getAllowableErrorTime());
            
            switch(song.getProperties().getDifficulty()){
                case NOT_SET:
                    difficultyNotSetRadioButton.setSelected(true);
                    difficultyNotSetRadioButtonActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                    break;
                case EASY:
                    easyDifficultyRadioButton.setSelected(true);
                    easyDifficultyRadioButtonActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                    break;
                case MEDIUM:
                    mediumDifficultyRadioButton.setSelected(true);
                    mediumDifficultyRadioButtonActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                    break;
                case HARD:
                    hardDifficultyRadioButton.setSelected(true);
                    hardDifficultyRadioButtonActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                    break;
                case EXPERT:
                    expertDifficultyRadioButton.setSelected(true);
                    expertDifficultyRadioButtonActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                    break;
            }
            
            songPropertiesDialog.setModal(true);
            songPropertiesDialog.setAlwaysOnTop(true);
            songPropertiesDialog.pack();
            songPropertiesDialog.setVisible(true);
        }else{
            displayErrorMessage("You must load a song, or create a new song before editing song properties");
        }
    }//GEN-LAST:event_songPropertiesMenuItemActionPerformed
    
    private void doNothingModeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doNothingModeMenuItemActionPerformed
        this.playbackHandler = new DoNothingButtonHandler();
        songDisplayPanel.setTrackBackgrounds(playbackHandler.getBackgroundColor());
    }//GEN-LAST:event_doNothingModeMenuItemActionPerformed
    
    private void drawErrorBarsCheckItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawErrorBarsCheckItemActionPerformed
        songDisplayPanel.setDrawErrorBars(drawErrorBarsCheckItem.isSelected());
    }//GEN-LAST:event_drawErrorBarsCheckItemActionPerformed
    
    private void drawPressTimesCheckItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawPressTimesCheckItemActionPerformed
        songDisplayPanel.setDrawPressTimes(drawPressTimesCheckItem.isSelected());
    }//GEN-LAST:event_drawPressTimesCheckItemActionPerformed
    
    private void buttonPressRecordingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPressRecordingMenuItemActionPerformed
        this.setNewButtonHandler(new PressButtonToRecordHandler());
    }//GEN-LAST:event_buttonPressRecordingMenuItemActionPerformed
    
    private void strumRecordingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strumRecordingMenuItemActionPerformed
        this.setNewButtonHandler(new PressStrumToRecordHandler());
    }//GEN-LAST:event_strumRecordingMenuItemActionPerformed
    
    private void practiceModeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_practiceModeMenuItemActionPerformed
        this.setNewButtonHandler(new PracticeModeHandler());
    }//GEN-LAST:event_practiceModeMenuItemActionPerformed
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(song != null && song.hasUnsavedChanges()){
            logger.info("Unsaved Changes!");
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes! \n Would you like to save before exiting?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if(result == JOptionPane.OK_OPTION){
                saveMenuItemActionPerformed(new java.awt.event.ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
            }else if(result == JOptionPane.NO_OPTION){
                System.exit(0);
            }else if(result == JOptionPane.CANCEL_OPTION){
                return;
            }
        }else{
            logger.info("No unsaved changes - exiting");
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing
    
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if(this.currentFile == null){
            this.saveAsMenuItemActionPerformed(evt);
        }else{
            try {
                SongUtils.saveToFile(currentFile, song);
            } catch (IOException ex) {
                displayExceptionDialog(ex, "I/O Exception when saving song to file.");
            }
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed
    
    private void okayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okayButtonActionPerformed
        exceptionDialog.setVisible(false);
    }//GEN-LAST:event_okayButtonActionPerformed
    
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    private void configureControllerItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureControllerItemActionPerformed
        GamepadConfigDialog configDialog = new GamepadConfigDialog();
        configDialog.setButtonConfig(buttonConfig);
        GamepadConfigDialog.ResultType returnVal = configDialog.displayDialog();
        
        if(returnVal == GamepadConfigDialog.ResultType.OKAY_OPTION){
            //Initialize the button configuration
            initButtonConfig(configDialog.getButtonConfig());
            
            //Try to save the new button config to the preferences file
            BufferedWriter out = null;
            if(CONTROLLER_SETTINGS_FILE.exists()){
                CONTROLLER_SETTINGS_FILE.delete();
            }
            try{
                out = new BufferedWriter(new FileWriter(CONTROLLER_SETTINGS_FILE));
                buttonConfig.saveButtonConfigTo(out);
            }catch(IOException ex){
                displayExceptionDialog(ex, "I/O Error when writing to configuration file " + CONTROLLER_SETTINGS_FILE.getName());
            }finally{
                try{
                    if(out != null){
                        out.flush();
                        out.close();
                    }
                }catch(IOException ex){
                    displayExceptionDialog(ex, "I/O Error when closing configuration file " + CONTROLLER_SETTINGS_FILE.getName());
                }
            }
        }else{
            logger.info("User Canceled Controller Configuration Dialog");
        }
    }//GEN-LAST:event_configureControllerItemActionPerformed
    
    private void enableNoteLineCheckItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableNoteLineCheckItemActionPerformed
        songDisplayPanel.setDrawNoteLines(enableNoteLineCheckItem.isSelected());
    }//GEN-LAST:event_enableNoteLineCheckItemActionPerformed
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.formWindowClosing(null);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    private void newSongMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSongMenuItemActionPerformed
        if(song != null && song.hasUnsavedChanges()){
            logger.info("Unsaved Changes!");
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes! \n Would you like to save before opening another file?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if(result == JOptionPane.OK_OPTION){
                saveMenuItemActionPerformed(new java.awt.event.ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
            }else if(result == JOptionPane.NO_OPTION){
                //CONTINUE
            }else if(result == JOptionPane.CANCEL_OPTION){
                return;
            }
        }
        
        File selectedFile = null;
        
        //Create a new FileChooser to select the music file
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select a Music File");
        chooser.setFileFilter(FileUtils.createFileFilter("mid, midi, mp3, ogg, wav, wma, flac",
                "Supported Music Files (*.mid, *.midi, *.mp3, *.ogg, *.wav, *.wma, *.flac)"));
        
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            selectedFile = chooser.getSelectedFile();
        }else{
            return;
        }
        
        //Do any cleanup that needs doing
        closeSong();
        
        if(selectedFile.exists()){
            //Open the music file to play the music
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                music = BackgroundMusic.loadMusicFrom(selectedFile);
            } catch (BackgroundMusic.MusicException ex) {
                displayExceptionDialog(ex, "Error While Opening File: " + selectedFile.getName());
                music = null;
                song = null;
                //Make sure the buttons are configured OK
                songLoaded();
                return;
            }
            
            //Create a new Song class to hold note information
            song = new TrackBasedSong();
            song.getProperties().setTitle(selectedFile.getName());
            song.getProperties().setMusicFileName(selectedFile.getName());
            song.getProperties().setMusicDirectoryHint(selectedFile.getParentFile().getPath());
            song.getProperties().setLength(music.getLength());
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            songLoaded();
        }
    }//GEN-LAST:event_newSongMenuItemActionPerformed
    
    private void seekSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_seekSliderStateChanged
        if(music != null){
            try{
                //Skip to the second that is being selected
                int selectedSecond = seekSlider.getValue();
                if(selectedSecond != (int) music.getTimeInSeconds()){
                    music.skipTo(selectedSecond);
                    updateForTimeChange();
                }
            }catch(MusicException e){
                displayExceptionDialog(e, "Error Encountered While Seeking: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_seekSliderStateChanged
    
    private void openSongDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSongDataMenuItemActionPerformed
        //Check to make sure we're not loosing and changes
        if(song != null && song.hasUnsavedChanges()){
            logger.info("Unsaved Changes!");
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes! \n Would you like to save before opening another file?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if(result == JOptionPane.OK_OPTION){
                saveMenuItemActionPerformed(new java.awt.event.ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
            }else if(result == JOptionPane.NO_OPTION){
                //CONTINUE
            }else if(result == JOptionPane.CANCEL_OPTION){
                return;
            }
        }
        
        File selectedDataFile = null;
        File selectedMusicFile = null;
        
        //Open a file chooser to select the data file for this song
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select the song file you wish to open:");
        chooser.setApproveButtonText("Open Song Data File");
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileFilter(FileUtils.createFileFilter("SNG", "Song Files (*.SNG)"));
        
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            selectedDataFile = chooser.getSelectedFile();
        }else{
            return;
        }
        
        //Close and open song and do and stuff that needs doing
        closeSong();
        
        //Try to load the song data
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            song = SongUtils.loadFromFile(selectedDataFile);
        }catch(IOException ex) {
            displayExceptionDialog(ex, "I/O Exception when attempting to load song from file.");
            song = null;
        }catch(Exception ex){
            displayExceptionDialog(ex, "Unhandled Exception");
            song = null;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        //If we couldn't load the song, give up and return
        if(song == null){
            music = null;
            songLoaded();
            return;
        }
        
        
        //Try to open the music file specified in the song data from the same dir
        selectedMusicFile = new File(selectedDataFile.getParentFile().getPath() + "/" + song.getProperties().getMusicFileName());
        
        //If the Music file isn't loadable from the same location, try loading from the hinted directory
        if(!selectedMusicFile.exists()){
            selectedMusicFile = new File(song.getProperties().getMusicDirectoryHint() + "/" + song.getProperties().getMusicFileName());
            
            //If the music file isn't loadable from THERE, then popup the search dialog
            if(!selectedMusicFile.exists()){
                selectedMusicFile = new File(selectedDataFile.getParentFile().getPath() + "/" + song.getProperties().getMusicFileName());
                chooser = new JFileChooser();
                chooser.addChoosableFileFilter(FileUtils.createFileFilter("MP3, OGG, WAV, MIDI, MID, WMA, FLAC", "Song Files (*.MP3, *.OGG, *.WAV, *.MIDI, *.MID, *.WMA, *.FLAC)"));
                chooser.setFileFilter(new StringFileFilter(selectedMusicFile.getName()));
                chooser.setCurrentDirectory(new File("."));
                chooser.setSelectedFile(selectedMusicFile);
                chooser.setApproveButtonText("Open Music File");
                chooser.setDialogTitle("Please Locate " + song.getProperties().getMusicFileName());
                
                if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                    selectedMusicFile = chooser.getSelectedFile();
                }else{
                    return;
                }
            }
        }
        
        //Try to load the music file
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try{
            music = BackgroundMusic.loadMusicFrom(selectedMusicFile);
        }catch(BackgroundMusic.MusicException e){
            displayExceptionDialog(e, "Error while opening music file " + selectedMusicFile.getName() + ": \n" + e);
            song = null;
            music = null;
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        //If we sucsongessfully loaded a song, then update the UI stuff
        if(song != null && music != null){
            //If the songs length's don't match, display a warning
            if(song.getProperties().getLength() != music.getLength()){
                displayErrorMessage("Warning!\n" +
                        "The length of the loaded music file does not match the \n" +
                        "length of the original music file that was used to \n" +
                        "create this song! You may need to re-check the note timing.\n" +
                        "(Expected: " + song.getProperties().getLength() + "s Loaded: " + music.getLength() + "s)");
            }
            this.currentFile = selectedDataFile;
            song.getProperties().setMusicDirectoryHint(selectedMusicFile.getParentFile().getPath());
            songLoaded();
        }
    }//GEN-LAST:event_openSongDataMenuItemActionPerformed
    
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        if(song != null){
            //Open a file chooser to save the data
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(FileUtils.createFileFilter("SNG", "Song Files (*.sng)"));
            chooser.setCurrentDirectory(new File("."));
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setDialogTitle("Save music data to:");
            
            File selectedFile = null;
            if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                selectedFile = chooser.getSelectedFile();
                if(selectedFile == null) return;
                String ext = FileUtils.getExtension(selectedFile);
                if(ext == null || !ext.equals("sng")){
                    logger.info("SNG not part of the fileName, appending automatically");
                    selectedFile = new File(selectedFile.getPath() + ".sng");
                }
            }else{
                return;
            }
            
            //If the selected file already exists, then ask if it should be overwritten
            if(selectedFile.exists()){
                int result = JOptionPane.showConfirmDialog(this, "The file " + selectedFile.getName() + " already exists.\n Are you sure you want to save over it?");
                //If the user DID NOT select OK, then don't do anything
                if(result != JOptionPane.OK_OPTION){
                    return;
                }
            }
            this.currentFile = selectedFile;
            try {
                SongUtils.saveToFile(selectedFile, song);
            } catch (IOException ex) {
                displayExceptionDialog(ex, "I/O Exception when saving to file.");
            }
        }
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void updateForTimeChange(){
        if(music != null && song != null){
            float currentTime = music.getTimeInSeconds();
            songDisplayPanel.setCurrentTime(currentTime);
            if(playbackHandler.updateNoteStatesOnPlayback()){
                song.updateForTimeChange(currentTime);
            }
        }
        
        //Remove the change listeners from the seekbar (to keep it skipping
        //when we songpdate it manually)
        ChangeListener [] seekListeners = seekSlider.getChangeListeners();
        for(ChangeListener cl : seekListeners){
            seekSlider.removeChangeListener(cl);
        }
        if(music != null) seekSlider.setValue((int) music.getTimeInSeconds());
        //Re-enable the change listeners
        for(ChangeListener cl : seekListeners){
            seekSlider.addChangeListener(cl);
        }
    }
    
    private void closeSong(){
        //Do any un-cloading that the music needs to do
        if(music != null){
            music.removeMusicListener(this);
            music.stop();
            music.cleanup();
            music = null;
        }
        
        if(song != null){
            song.removeNoteChangeListener(this);
        }
        
        setNewButtonHandler(new DoNothingButtonHandler());
        doNothingModeMenuItem.setSelected(true);
        
        saveAsMenuItem.setEnabled(false);
        saveMenuItem.setEnabled(false);
        
        actions.get("selectNoneAction").actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        actions.get("normalPlaybackAction").actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        
        normalSpeedMenuItem.setSelected(true);
        halfSpeedMenuItem.setSelected(false);
        quarterSpeedMenuItem.setSelected(false);
        
        songDisplayPanel.setCurrentTime(0);
        songDisplayPanel.setSong(null);
        songDisplayPanel.clearSelectedNotes();
        song = null;
        
        this.currentFile = null;
    }
    
    private void setNewButtonHandler(ButtonPressHandler newButtonHandler){
        this.playbackHandler = null;
        if(newButtonHandler != null){
            this.playbackHandler = newButtonHandler;
            songDisplayPanel.setTrackBackgrounds(playbackHandler.getBackgroundColor());
        }
        track0ActivePanel.setBackground(this.getBackground());
        track1ActivePanel.setBackground(this.getBackground());
        track2ActivePanel.setBackground(this.getBackground());
        track3ActivePanel.setBackground(this.getBackground());
        track4ActivePanel.setBackground(this.getBackground());
    }
    
    private void songLoaded(){
        if(currentFile != null){
            saveMenuItem.setEnabled(true);
            this.setTitle("Freetar Editor (" + song.getProperties().getTitle() + ")");
        }else{
            saveMenuItem.setEnabled(false);
        }
        
        
        //Update the slider values
        if(music != null){
            seekSlider.setMinimum(0);
            seekSlider.setMaximum((int) music.getLength());
            seekSlider.setValue(0);
            //Add ourselves as a music listener
            music.addMusicListener(this);
        }
        
        if(song != null){
            saveAsMenuItem.setEnabled(true);
            song.getProperties().setLength(music.getLength());
            //Since this is a clean song we can ensure it doesn't require a save
            song.setUnsavedChanges(false);
            song.addNoteChangeListener(this);
        }
        
        
        //Set the song to the SongDisplayPanel
        songDisplayPanel.setSong(song);
        songDisplayPanel.setCurrentTime(0);
    }
    
    private void createNewNoteForButton(int buttonNumber){
        if(music != null && buttonNumber >= 0 && buttonNumber < Song.TRACKS){
            logger.info("Creating new Note at: " + music.getTimeInSeconds() + " for track " + buttonNumber);
            float noteTime = SongUtils.snapTimeToNearest(music.getTimeInSeconds(), songDisplayPanel.getSnapTime());
            Note newNote = song.createNote(noteTime, 0, buttonNumber, false);
            Command c = new AddNoteCommand(newNote, song);
            executeCommand(c);
        }
    }
    
    private void displayErrorMessage(String errorMessage){
        JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void displayExceptionDialog(Exception e, String userMessage){
        e.printStackTrace();
        exceptionUserMessageArea.setText(userMessage);
        exceptionNameTextArea.setText(e.getClass() + "");
        exceptionMessageTextArea.setText(e.getMessage());
        String stackTraceString = "";
        
        for(StackTraceElement element : e.getStackTrace()){
            stackTraceString += element + "\n";
        }
        
        exceptionStackTraceArea.setText(stackTraceString);
        exceptionDialog.setModal(true);
        exceptionDialog.setAlwaysOnTop(true);
        exceptionDialog.pack();
        exceptionDialog.setVisible(true);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Set<String> commandLineOptions = new HashSet<String>();
        for(int i = 0; i < args.length; i++){
            commandLineOptions.add(args[i]);
        }
        
        if(!commandLineOptions.contains("-NoSystemLookAndFeel")){
            //Try to use the default L&F
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex){
                logger.warning("Cannot set Native L&F: \n" + ex);
            }
        }
        
        final MusicEditor musicEditor = new MusicEditor();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try{
                    musicEditor.setVisible(true);
                }catch(Exception e){
                    musicEditor.displayExceptionDialog(e, "UNHANDLED ERROR!!");
                }
            }
        });
    }
    
    /* SongPanListener Methods */
    public void timeChanged(float newTime) {
        try{
            music.skipTo(newTime);
        }catch(MusicException ex){
            displayExceptionDialog(ex, "Callback from Display Panel failed to skip time correctly: " + ex.getMessage());
        }
        this.updateForTimeChange();
    }
    
    /** MusicListener Methods **/
    public void musicTimeChanged(float currentSongTime) {
        updateForTimeChange();
    }
    
    public void stateChanged(MusicState oldState, MusicState newState) {
        if(newState == MusicState.STOPPED){
            actions.get("stopAction").setEnabled(false);
        }else if(newState == MusicState.PLAYING){
            actions.get("stopAction").setEnabled(true);
        }else if(newState == MusicState.PAUSED){
            //Do nothing... I guess...
        }
    }
    
    //BUTTON PRESS LISTENER METHODS
    public void tempoChanged(float newTempo) {
        //Do nothing..... This was probably caused by this class...
    }
    
    public void buttonActionTriggered(ButtonEvent event) {
        this.playbackHandler.handleButtonPress(event);
    }
    
    public void noteChangedState(NoteEvent event) {
        //Set the background of the trackActivePanels
        final boolean[] trackActive = new boolean[Song.TRACKS];
        for(int i = 0; i < Song.TRACKS; i++){
            trackActive[i] = false;
        }
        for(Note n : song.getActiveNotes()){
            trackActive[n.getButtonNumber()] = true;
        }
        
        if(trackActive[0]){
            track0ActivePanel.setBackground(SongDisplayPanel.TRACK_COLORS[0]);
        }else{
            track0ActivePanel.setBackground(this.getBackground());
        }
        if(trackActive[1]){
            track1ActivePanel.setBackground(SongDisplayPanel.TRACK_COLORS[1]);
        }else{
            track1ActivePanel.setBackground(this.getBackground());
        }
        if(trackActive[2]){
            track2ActivePanel.setBackground(SongDisplayPanel.TRACK_COLORS[2]);
        }else{
            track2ActivePanel.setBackground(this.getBackground());
        }
        if(trackActive[3]){
            track3ActivePanel.setBackground(SongDisplayPanel.TRACK_COLORS[3]);
        }else{
            track3ActivePanel.setBackground(this.getBackground());
        }
        if(trackActive[4]){
            track4ActivePanel.setBackground(SongDisplayPanel.TRACK_COLORS[4]);
        }else{
            track4ActivePanel.setBackground(this.getBackground());
        }
        
        //Set the last 'successfull' played note to be this note if the
        //note is changing into the 'NotePlayedState', if it is moving into a
        //different (not-correctly-played) final state, set last played note to null
        if(playbackHandler != null){
            if(event.getNewState() == NotePlayedState.getInstance()){
                playbackHandler.setLastPlayedNote(event.getNote());
            }else if(event.getNewState() instanceof FinalState){
                playbackHandler.setLastPlayedNote(null);
            }
        }
    }
    
    //Handling Buttons
    private interface ButtonPressHandler{
        public void handleButtonPress(ButtonEvent event);
        public Color getBackgroundColor();
        public boolean updateNoteStatesOnPlayback();
        public void setLastPlayedNote(Note n);
    }
    
    private class DoNothingButtonHandler implements ButtonPressHandler{
        public void handleButtonPress(ButtonEvent event) {
            //Don't do anything
        }
        
        public Color getBackgroundColor(){
            return new Color(245, 245, 255);
        }
        
        public boolean updateNoteStatesOnPlayback(){
            return false;
        }
        
        public void setLastPlayedNote(Note n){
        }
    }
    
    private class BeatPracticeModeHandler implements ButtonPressHandler{
        public void handleButtonPress(ButtonEvent event){
            ButtonConfig.Action pressedButton = buttonConfig.getAssignmentFor(event.getButton());
            //If the button was not mapped songit returns null from the getbutton methdos) then exit
            if(pressedButton == null){
                return;
            }
            final float currentTime = music.getTimeInSeconds();
            ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");  //Used to execute actions
            
            //Handle the pause button & Special button (just pause the game)
            if(event.getEventType() == ButtonEvent.EventType.BUTTON_PRESSED){
                switch(pressedButton ){
                    case SPECIAL:
                        //RESET THE NOTE STATES
                        actions.get("resetNotesAction").actionPerformed(actionEvent);
                        return;
                    case PAUSE:
                        actions.get("pauseAction").actionPerformed(actionEvent);
                        return;
                }
            }
            
            if(event.getEventType() == ButtonEvent.EventType.BUTTON_PRESSED){
                switch(pressedButton){
                    case STRUM_DOWN:
                    case STRUM_UP:
                        switch(event.getEventType()){
                            case BUTTON_PRESSED:
                                song.strumNote(0, currentTime);
                                song.strumNote(1, currentTime);
                                song.strumNote(2, currentTime);
                                song.strumNote(3, currentTime);
                                song.strumNote(4, currentTime);
                                break;
                            case BUTTON_RELEASED:
                                song.releaseNote(0, currentTime);
                                song.releaseNote(1, currentTime);
                                song.releaseNote(2, currentTime);
                                song.releaseNote(3, currentTime);
                                song.releaseNote(4, currentTime);
                                break;
                        }
                        break;
                }
            }
        }
        
        public Color getBackgroundColor() {
            return new Color(245, 255, 245);
        }
        
        public boolean updateNoteStatesOnPlayback() {
            return true;
        }
        
        public void setLastPlayedNote(Note n){
        }
    }
    
    private class PracticeModeHandler implements ButtonPressHandler{
        private Note lastPressedNote = null;    //Points to the last note pressed (if it was successfull), null if the last note was missed
        
        public void handleButtonPress(ButtonEvent event) {
            if(buttonConfig != null && music != null && song != null){
                ButtonConfig.Action pressedButton = buttonConfig.getAssignmentFor(event.getButton());
                //If the button was not mapped songit returns null from the getbutton methdos) then exit
                if(pressedButton == null) return;
                final float currentTime = music.getTimeInSeconds();
                
                ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");  //Used to execute actions
                
                //Handle the pause button & Special button (just pause the game)
                if(event.getEventType() == ButtonEvent.EventType.BUTTON_PRESSED){
                    switch(pressedButton ){
                        case SPECIAL:
                            //RESET THE NOTE STATES
                            actions.get("resetNotesAction").actionPerformed(actionEvent);
                            return;
                        case PAUSE:
                            actions.get("pauseAction").actionPerformed(actionEvent);
                            return;
                    }
                }
                if(event.getEventType() == ButtonEvent.EventType.BUTTON_PRESSED){
                    switch(pressedButton){
                        case TRACK_0:
                            if(!buttonConfig.isStrumRequired()){
                                song.strumNote(0, currentTime);
                            }
                            //Can not hammer on Track 0
                            songDisplayPanel.pressedNote(0);
                            break;
                        case TRACK_1:
                            if(!buttonConfig.isStrumRequired()){
                                song.strumNote(1,currentTime);
                            }else if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 0 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_0).isPressed()){
                                boolean success = song.tryHammerOn(currentTime, 1);
                                if(!success){
                                    lastPressedNote = null;
                                }
                            }
                            songDisplayPanel.pressedNote(1);
                            break;
                        case TRACK_2:
                            if(!buttonConfig.isStrumRequired()){
                                song.strumNote(2, currentTime);
                            }else if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 1 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_1).isPressed()){
                                boolean success = song.tryHammerOn(currentTime, 2);
                                if(!success){
                                    lastPressedNote = null;
                                }
                            }
                            songDisplayPanel.pressedNote(2);
                            break;
                        case TRACK_3:
                            if(!buttonConfig.isStrumRequired()){
                                song.strumNote(3, currentTime);
                            }else if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 2 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_2).isPressed()){
                                boolean success = song.tryHammerOn(currentTime, 3);
                                if(!success){
                                    lastPressedNote = null;
                                }
                            }
                            songDisplayPanel.pressedNote(3);
                            break;
                        case TRACK_4:
                            if(!buttonConfig.isStrumRequired()){
                                song.strumNote(4, currentTime);
                            }else if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 3 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_3).isPressed()){
                                boolean success = song.tryHammerOn(currentTime, 4);
                                if(!success){
                                    lastPressedNote = null;
                                }
                            }
                            songDisplayPanel.pressedNote(4);
                            break;
                        case STRUM_UP:
                        case STRUM_DOWN:
                            if(buttonConfig.isStrumRequired()){
                                List<Integer> buttonsPressed = new ArrayList<Integer>();
                                //Check which buttons are down, and send the 'pressed' values
                                if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_0).isPressed()){
                                    buttonsPressed.add(0);
                                }
                                if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_1).isPressed()){
                                    buttonsPressed.add(1);
                                }
                                if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_2).isPressed()){
                                    buttonsPressed.add(2);
                                }
                                if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_3).isPressed()){
                                    buttonsPressed.add(3);
                                }
                                if(buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_4).isPressed()){
                                    buttonsPressed.add(4);
                                }
                                song.strumNotes(buttonsPressed, currentTime);
                            }
                            songDisplayPanel.setStrumPressed(true);
                            break;
                        default:
                            logger.warning("Unhandled button press for " + pressedButton);
                            break;
                    }
                }else if(event.getEventType() == ButtonEvent.EventType.BUTTON_RELEASED){
                    switch(pressedButton){
                        case TRACK_0:
                            songDisplayPanel.releasedNote(0);
                            break;
                        case TRACK_1:
                            songDisplayPanel.releasedNote(1);
                            if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 1 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_0).isPressed()){
                                song.tryHammerOn(currentTime, 0);
                            }
                            break;
                        case TRACK_2:
                            songDisplayPanel.releasedNote(2);
                            if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 2 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_1).isPressed()){
                                song.tryHammerOn(currentTime, 1);
                            }
                            break;
                        case TRACK_3:
                            songDisplayPanel.releasedNote(3);
                            if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 3 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_2).isPressed()){
                                song.tryHammerOn(currentTime, 2);
                            }
                            break;
                        case TRACK_4:
                            songDisplayPanel.releasedNote(4);
                            if(lastPressedNote != null &&
                                    lastPressedNote.getButtonNumber() == 4 &&
                                    buttonConfig.getButtonFor(ButtonConfig.Action.TRACK_3).isPressed()){
                                song.tryHammerOn(currentTime, 3);
                            }
                            break;
                        case STRUM_UP:
                        case STRUM_DOWN:
                            songDisplayPanel.setStrumPressed(false);
                            break;
                        default:
                            //logger.warning("Unhandled button release for " + pressedButton);
                            break;
                    }
                }
            }
        }
        
        public Color getBackgroundColor(){
            return new Color(245, 255, 245);
        }
        
        public boolean updateNoteStatesOnPlayback(){
            return true;
        }
        
        public void setLastPlayedNote(Note n){
            this.lastPressedNote = n;
        }
    }
    
    private class PressStrumToRecordHandler implements ButtonPressHandler{
        private Set<ButtonConfig.Action> pressedButtons = new HashSet<ButtonConfig.Action>();
        public void handleButtonPress(ButtonEvent event) {
            //if we dont' have any of the necessary parts, we can't continue
            if(buttonConfig == null ||
                    music == null ||
                    song == null ||
                    event == null ||
                    songDisplayPanel == null){
                return;
            }
            ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");  //Used to execute actions
            
            ButtonConfig.Action pressedButton = buttonConfig.getAssignmentFor(event.getButton());
            
            //If the button was not mapped it returns null from the getbutton() method, do nothing
            if(pressedButton == null) return;
            
            final float currentTime = music.getTimeInSeconds();
            ButtonEvent.EventType eventType = event.getEventType();
            
            //Handle the pause button & Special button (just pause the game)
            if(eventType == ButtonEvent.EventType.BUTTON_PRESSED){
                switch(pressedButton ){
                    case SPECIAL:
                        //RESET THE NOTE STATES
                        actions.get("resetNotesAction").actionPerformed(actionEvent);
                        return;
                    case PAUSE:
                        actions.get("pauseAction").actionPerformed(actionEvent);
                        return;
                }
            }
            
            switch(eventType){
                case BUTTON_PRESSED:
                    switch(pressedButton){
                        case TRACK_0:
                            pressedButtons.add(ButtonConfig.Action.TRACK_0);
                            break;
                        case TRACK_1:
                            pressedButtons.add(ButtonConfig.Action.TRACK_1);
                            break;
                        case TRACK_2:
                            pressedButtons.add(ButtonConfig.Action.TRACK_2);
                            break;
                        case TRACK_3:
                            pressedButtons.add(ButtonConfig.Action.TRACK_3);
                            break;
                        case TRACK_4:
                            pressedButtons.add(ButtonConfig.Action.TRACK_4);
                            break;
                        case STRUM_UP:
                        case STRUM_DOWN:
                            //Check what buttons were down and create notes for them
                            if(pressedButtons.contains(ButtonConfig.Action.TRACK_0)){
                                actions.get("newNote0Action").actionPerformed(actionEvent);
                            }
                            if(pressedButtons.contains(ButtonConfig.Action.TRACK_1)){
                                actions.get("newNote1Action").actionPerformed(actionEvent);
                            }
                            if(pressedButtons.contains(ButtonConfig.Action.TRACK_2)){
                                actions.get("newNote2Action").actionPerformed(actionEvent);
                            }
                            if(pressedButtons.contains(ButtonConfig.Action.TRACK_3)){
                                actions.get("newNote3Action").actionPerformed(actionEvent);
                            }
                            if(pressedButtons.contains(ButtonConfig.Action.TRACK_4)){
                                actions.get("newNote4Action").actionPerformed(actionEvent);
                            }
                            break;
                    }
                    break;
                case BUTTON_RELEASED:
                    switch(pressedButton){
                        case TRACK_0:
                            pressedButtons.remove(ButtonConfig.Action.TRACK_0);
                            break;
                        case TRACK_1:
                            pressedButtons.remove(ButtonConfig.Action.TRACK_1);
                            break;
                        case TRACK_2:
                            pressedButtons.remove(ButtonConfig.Action.TRACK_2);
                            break;
                        case TRACK_3:
                            pressedButtons.remove(ButtonConfig.Action.TRACK_3);
                            break;
                        case TRACK_4:
                            pressedButtons.remove(ButtonConfig.Action.TRACK_4);
                            break;
                    }
                    break;
            }
            
        }
        
        public Color getBackgroundColor(){
            return new Color(255, 245, 245);
        }
        
        public boolean updateNoteStatesOnPlayback(){
            return false;
        }
        
        public void setLastPlayedNote(Note n){
        }
        
    }
    
    
    private class PressButtonToRecordHandler implements ButtonPressHandler{
        private Note[] holdingNotes = new Note[Song.TRACKS];
        
        public void handleButtonPress(ButtonEvent event) {
            //if we dont' have any of the necessary parts, we can't continue
            if(buttonConfig == null ||
                    music == null ||
                    song == null ||
                    event == null ||
                    songDisplayPanel == null){
                return;
            }
            ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");  //Used to execute actions
            
            ButtonConfig.Action pressedButton = buttonConfig.getAssignmentFor(event.getButton());
            
            //If the button was not mapped it returns null from the getbutton() method, do nothing
            if(pressedButton == null) return;
            
            final float currentTime = music.getTimeInSeconds();
            ButtonEvent.EventType eventType = event.getEventType();
            
            //Handle the pause button & Special button (just pause the game)
            if(eventType == ButtonEvent.EventType.BUTTON_PRESSED){
                switch(pressedButton ){
                    case SPECIAL:
                        //RESET THE NOTE STATES
                        actions.get("resetNotesAction").actionPerformed(actionEvent);
                        return;
                    case PAUSE:
                        actions.get("pauseAction").actionPerformed(actionEvent);
                        return;
                }
            }
            
            switch(eventType){
                case BUTTON_PRESSED:
                    switch(pressedButton){
                        case TRACK_0:
                            pressed(0, currentTime);
                            break;
                        case TRACK_1:
                            pressed(1, currentTime);
                            break;
                        case TRACK_2:
                            pressed(2, currentTime);
                            break;
                        case TRACK_3:
                            pressed(3, currentTime);
                            break;
                        case TRACK_4:
                            pressed(4, currentTime);
                            break;
                    }
                    break;
                case BUTTON_RELEASED:
                    switch(pressedButton){
                        case TRACK_0:
                            released(0, currentTime);
                            break;
                        case TRACK_1:
                            released(1, currentTime);
                            break;
                        case TRACK_2:
                            released(2, currentTime);
                            break;
                        case TRACK_3:
                            released(3, currentTime);
                            break;
                        case TRACK_4:
                            released(4, currentTime);
                            break;
                    }
                    break;
            }
        }
        
        protected void released(final int trackNumber, final float currentTime){
            if(holdingNotes[trackNumber] != null && currentTime - holdingNotes[trackNumber].getTime() >= song.getProperties().getAllowableErrorTime() * 1.5f){
                try {
                    holdingNotes[trackNumber].setDuration(currentTime - holdingNotes[trackNumber].getTime());
                } catch (InvalidDurationException ex) {
                    ex.printStackTrace();
                } catch (DuplicateNoteException ex) {
                    ex.printStackTrace();
                }
            }
            holdingNotes[trackNumber] = null;
        }
        
        protected void pressed(final int trackNumber, final float currentTime){
            if(holdingNotes[trackNumber] != null){
                holdingNotes[trackNumber] = null;
            }
            Note newNote = song.createNote(currentTime, trackNumber);
            AddNoteCommand cmd = new AddNoteCommand(newNote, song);
            executeCommand(cmd);
            holdingNotes[trackNumber] = newNote;
        }
        
        public Color getBackgroundColor(){
            return new Color(255, 245, 245);
        }
        
        public boolean updateNoteStatesOnPlayback(){
            return false;
        }
        
        public void setLastPlayedNote(Note n){
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog aboutDialog;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel activeTracksPanel;
    private javax.swing.JTextField albumTextField;
    private javax.swing.JTextField artistTextField;
    private javax.swing.JSpinner beatsPerSecondSpinner;
    private javax.swing.JRadioButtonMenuItem buttonPressRecordingMenuItem;
    private javax.swing.JSpinner buttonPressTimeSpinner;
    private javax.swing.JButton closeAboutDialogButton;
    private javax.swing.JMenuItem configureControllerItem;
    private javax.swing.JButton copyButton;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JButton cutButton;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem decreaseDurationMenuItem;
    private javax.swing.JMenuItem deleteNoteMenuItem;
    private javax.swing.ButtonGroup difficultyButtonGroup;
    private javax.swing.JTextArea difficultyDescriptionTextArea;
    private javax.swing.JRadioButton difficultyNotSetRadioButton;
    private javax.swing.JMenuItem disableHammerOnMenuItem;
    private javax.swing.JCheckBoxMenuItem disablePitchShiftCheckItem;
    private javax.swing.JRadioButtonMenuItem doNothingModeMenuItem;
    private javax.swing.JCheckBoxMenuItem drawErrorBarsCheckItem;
    private javax.swing.JCheckBoxMenuItem drawPressTimesCheckItem;
    private javax.swing.JRadioButton easyDifficultyRadioButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem enableHammerOnMenuItem;
    private javax.swing.JCheckBoxMenuItem enableNoteLineCheckItem;
    private javax.swing.JDialog exceptionDialog;
    private javax.swing.JTextField exceptionMessageTextArea;
    private javax.swing.JTextField exceptionNameTextArea;
    private javax.swing.JTextArea exceptionStackTraceArea;
    private javax.swing.JTextArea exceptionUserMessageArea;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JRadioButton expertDifficultyRadioButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JCheckBoxMenuItem greenTrackOnBottomCheckItem;
    private javax.swing.JRadioButtonMenuItem halfSpeedMenuItem;
    private javax.swing.JRadioButton hardDifficultyRadioButton;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem increaseDurationMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JRadioButton mediumDifficultyRadioButton;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem moveNoteAheadMenuItem;
    private javax.swing.JMenuItem moveNoteBehindMenuItem;
    private javax.swing.JMenuItem moveNoteDownMenuItem;
    private javax.swing.JMenuItem moveNoteUpMenuItem;
    private javax.swing.JPanel musicControlPanel;
    private javax.swing.JMenuItem newNote0MenuItem;
    private javax.swing.JMenuItem newNote1MenuItem;
    private javax.swing.JMenuItem newNote2MenuItem;
    private javax.swing.JMenuItem newNote3MenuItem;
    private javax.swing.JMenuItem newNote4MenuItem;
    private javax.swing.JMenuItem newSongMenuItem;
    private javax.swing.JRadioButtonMenuItem normalSpeedMenuItem;
    private javax.swing.JPanel noteDisplayPanel;
    private javax.swing.JButton okayButton;
    private javax.swing.JMenuItem openSongDataMenuItem;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JButton pasteButton;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JButton pauseButton;
    private javax.swing.JMenuItem pauseMenuItem;
    private javax.swing.JButton playButton;
    private javax.swing.JMenuItem playMenuItem;
    private javax.swing.JMenu playbackMenu;
    private javax.swing.ButtonGroup playbackModeButtonGroup;
    private javax.swing.JRadioButtonMenuItem practiceModeMenuItem;
    private javax.swing.JRadioButtonMenuItem practiceStrumModeMenuItem;
    private javax.swing.JRadioButtonMenuItem quarterSpeedMenuItem;
    private javax.swing.JButton redoButton;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JButton resetSongButton;
    private javax.swing.JMenuItem resetSongMenuItem;
    private javax.swing.JMenuItem resetZoomMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem seekAheadMenuItem;
    private javax.swing.JMenuItem seekBehindMenuItem;
    private javax.swing.JSlider seekSlider;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenu selectMenu;
    private javax.swing.JMenuItem selectNoneMenuItem;
    private javax.swing.JMenuItem skipToBeginingMenuItem;
    private javax.swing.JMenuItem skipToEndMenuItem;
    private net.freetar.editor.SongDisplayPanel songDisplayPanel;
    private javax.swing.JButton songPropertiesCancelButton;
    private javax.swing.JDialog songPropertiesDialog;
    private javax.swing.JMenuItem songPropertiesMenuItem;
    private javax.swing.JButton songPropertiesOkButton;
    private javax.swing.ButtonGroup speedSelectionButtonGroup;
    private javax.swing.JMenuItem stopMenuItem;
    private javax.swing.JRadioButtonMenuItem strumRecordingMenuItem;
    private javax.swing.JTextField titleTextField;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel track0ActivePanel;
    private javax.swing.JPanel track1ActivePanel;
    private javax.swing.JPanel track2ActivePanel;
    private javax.swing.JPanel track3ActivePanel;
    private javax.swing.JPanel track4ActivePanel;
    private javax.swing.JButton undoButton;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JSpinner yearSpinner;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JMenuItem zoomOutMenuItem;
    // End of variables declaration//GEN-END:variables
    
}
