# Getting Started #

First off, if you want to get the most out of the Freetar code, you should use Netbeans to view/edit it. The Editor GUI was created in Maitisse, and editing components of the GUI in any other program may lead to incompatibilites in the code. I strongly reccomend following the instructions in the Setting Up Freetar In Netbeans article to get the most out of the Freetar source code.

# Core Classes #

The net.freetar and net.freetar.bgmusic contain probably the most important interfaces in the Freetar code structure: Song, Note and BackgroundMusic. Classes implementing these interfaces provide a mechanism for storing note times and buttons (Note), aggregating, updating and manipulating Notes (Song) and playing/interacting with the background music (BackgroundMusic).

# Song & Note #

Song and note have the following relationship:

**IMAGE MISSING**

The Song and Note class are closely related. The Note class stores the time, button number, and state of each 'note' that should be pressed in a Song. Each note also contains a 'NoteState' which keeps track of what should happen when that button is pressed.


**IMAGE MISSING**

AbstractSong provides an abstract implementation of the Song interface that takes care of handling the SongProperties as well as registering and unregistering NoteChangeListeners (Observer Pattern). NoteChangeListeners that are registered to a song receive events every time a Note in the Song changes state. This provides a mechanism for Note state changes to trigger events in a client program.

The TrackBasedSong class provides a concrete implementaiton of the AbstractSong class (and by extension the Song interface). Other concrete implementations of the AbstractSong class are possible - but none exist in the source at present.

The AbstractNote class provides an abstract implementation of the Note interface. It primarily handles the State Updating code and allows concrete implementations (e.g. SimpleNote) to handle other note implementation details.

# BackgroundMusic #

The BackgroundMusic class provides an interface for interacting with the actual music playing in the background. It provides basic play/pause/seek functionality, as well as a mechanism for registering and deregistering listeners (observer pattern). It also provides for slowing down and speeding up the tempo of the music, as well as querying the current time.

BackgroundMusic is Abstract so that extending it to support different formats is as easy as possible. It provides code to handle changing the playback state (Started, Stopped, Paused, etc..) and notifying listeners of the change.