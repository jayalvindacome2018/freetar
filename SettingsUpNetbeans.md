# Note #

This tutorial assumes you have downloaded the Freetar directory structure to <DOWNLOAD DIR> on your computer.

# Download From Google Code #

Visit Freetar Hero on Google Code and download the latest version of the code using your favorite Subversion client and save it into a folder of your choice. (Referred to as <DOWNLOAD DIR> for the rest of these instructions)

# Download and install Netbeans 5.5 IDE #

Download and install the Netbeans IDE from Netbeans.org.

# Setting Up A New Project & Adding The Sourcecode #

First we have to create a new project to hold the code and runtime configuration for Freetar.
  * Start Up Netbeans, and wait for it to load.
  * Choose 'New Project' from the 'File' menu
  * On the next screen, choose 'Java Project With Existing Sources' from the 'General' category
  * Next, you will be asked to choose the name of the project as well as the folder to store the project files. You can choose any folder on your computer for this part (i.e. the folder does not have to be the same place that you downloaded the Freetar Code to).
  * The next step asks you to specify the source packages for Freetar. You should have a ./src folder in your <DOWNLOAD DIR> folder where you placed the SVN of the freetar code - Add this folder to the 'Source Package Folders' section.
  * Now all the Freetar source packages should appear in the 'Source Packages' browser in netbeans along with the Java source files for each class.

# Setting Up Required Libraries #

Next we have to tell Netbeans where all the necessary .jar files are. These files are external libraries that are required to build and run Freetar. They should have been downloaded to your <DOWNLOAD DIR>/libraries folder by Subversion. There are essentially 34 libraries required:

jInput
> Required for Gamepad input
NativeFmodEx
> Required for music playback (Java bindings for FMOD)
jME
> Required for the Game-Engine
Swing Layout Extensions
> Required for the Swing panels and forms

  * With the freetar project selected as your main project, goto the 'File' menu and choose '"Freetar" Properties'.
  * Click on the 'Libraries' section, and add the following jar files to the 'Compile Time' libraries section:
    * <DOWNLOAD DIR>/libraries/jInput\_2006.05.14/jInput.jar
    * <DOWNLOAD DIR>/libraries/NativeFmodEx\_1.3.1/NativeFmodEx.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-awt.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-editors.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-effects.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-font.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-gamestates.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-model.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-scene.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-sound.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/jme-terrain.jar
    * <DOWNLOAD DIR>/libraries/jME\_CVS\_Aug\_29/lib/lwjgl.jar
    * <DOWNLOAD DIR>/resources/DefaultSkin.jar
    * <DOWNLOAD DIR>/resources/EditorIcons.jar
    * <DOWNLOAD DIR>/resources/GameResources.jar

  * Finally click on the 'Add Library' button and choose the 'Swing Layout Extensions' library (should be installed with Netbeans by default) to add the Swing Layout libraries.

# Setting Up Runtime Configuration #

  * Finally choose the 'Run' category and set the following:
    * Main Class - 'net.freetar.game.Launcher'
    * Working Directory - ' <DOWNLOAD DIR> '
    * VM Options - '-Djava.library.path=./lib'

# Compiling & Running Freetar #

  * Now Freetar should be all set up! You can use the 'Clean' button from the main menu to clean and build the project.
  * Click the 'Run' button to run the program! Thats it!