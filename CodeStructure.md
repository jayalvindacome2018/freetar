# Directory Structure #

./lib/
> Windows Runtime Libraries (DLL's) required for Freetar
./libraries/
> Copies of the libraries required to compile and run Freetar

> ./libraries/jInput\_2006.05.14/
> > the jInput version used in Freetar

> ./libraries/jME\_CVS\_Aug\_29/
> > the jME CVS version used in Freetar

> ./NativeFmodEx\_1.2.7/
> > the NativeFmodEx version used in Freetar

./nativelib/

> Signed JAR files required for Webstart

> ./nativelib/windows/
> > Windows-Specific JAR files

> ./nativelib/linux/
> > Linux-Specific JAR files

> ./nativelib/mac/
> > Mac-Specific JAR files

./resources/

> Game resources (models, sounds, icons) for Freetar

> ./resources/DefaultSkin
> > Source media files for the default skin (./resources/DefaultSkin.jar)

> ./resources/EditorIcons
> > Source media files for the icons used in the editor (./resources/EditorIcons.jar)

> ./resources/GameResources
> > Source media files for the resources used in the game (./resources/GameResources.jar)

./src/

> Source Code
> ./src/... : Java source code folders

# Source Code Packages #

The following Java packages are inluded in the source code tree:

net.freetar
> Core Freetar Classes
net.freetar.bgmusic
> Classes dealing with background music playback (FMOD, MIDI, etc...)
net.freetar.editor
> Classes dealing with the Editor GUI
net.freetar.editor.commands
> Commands that the editor uses
net.freetar.game
> Classes dealing with the game portion of freetar, including the Launcher
net.freetar.game.tests
> GUI tests
net.freetar.input
> Classes dealing with gamepad input
net.freetar.io
> Classes that load / save the Freetar core classes
net.freetar.noteStates
> Collection of classes that represent note states
net.freetar.util
> General utilities