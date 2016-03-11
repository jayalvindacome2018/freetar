# Downloaded song does not synch with your music file #

Typically, digitzing and encoding songs into a compressed format (MP3, OGG) causes the songs to change their 'length'. Short pauses at the begining and ends of songs are often removed automatically by digitizing software or by users in order to remove the slight pause between tracks when listenting to their music. Unfortunately, this causes versions of the same music file to differ between many users. Since the Freetar song format stores the 'trigger times' for each note, the music you are using may have a noticable delay between when you see the note and when you actually hear it if you are not using an exact copy of the music file. This is an inherint limitation of storing the songs and note-timing information in seperate files.

Fortunately, there is a quick and easy solution to the problem.

  1. Open the song in the Freetar editor along with the matching (but unsynchronized) music file
  1. Select the Edit -> Select All command from the menu (CTRL-A keyboard shortcut)
  1. Use the left and right arrow keys (or the appropriate Edit commands) to shift the notes ahead or behind until they match up with the music. Try to get them as close as possible, or you may find it difficult to play the song correctly.
  1. Once the notes match up with the song, save the .SNG file to keep your changes

That's it! Now your .SNG and music file should be synchronized!

# The Downloaded song starts synchronized, but drifts off or differs as the song goes along #

The problem is caused by using different recordings of the song - rather than simply differently encoded versions. If the song starts off synchronized, but differs later in the song, you may be using a different recording.

Rather than simply having a different amount of 'pause' at the begining of the song, you have a completely different recording. For example, say the .SNG file may have been created with the studio recording from an artists album. If you synchronize it with your Live recording of the same song, there will be differences between the two versions. Since no two different recordings of a song are exactly the same - the small errors the performers make along the way add up to cause large differences. The differences will cause the notes to be 'wrong' - some parts may be missing, extra parts added, or the synchronization may simply 'drift' off as the song goes on.

The only fix is to manually edit the .SNG file (as you would any normal song) to re-synchronize or add/remove the different sections of the song.

# Opening A Downloaded Song In The Editor #

  1. Legally aquire the Music file for the song you wish to edit
  1. Download the SNG file for the song you wish to edit
  1. If the .SNG file is in a compressed archive (.zip or .rar) unzip/rar it. You do not have to place the .SNG or Music file in any particular place on your computer.
  1. Start the Freetar Editor using either the downloaded .exe (windows), or webstart version (any OS)
  1. Choose 'File' -> 'Open' from the menu
  1. Browse to the location of the .SNG file
  1. Click on the 'Open Song' button to load the .SNG file

At this point, if you have the .SNG in the same folder as the Music file, the music will be loaded automatically. Also, if you have saved any changes to the .SNG file already (i.e. you have edited it in the past), the music file will open automatically. However, if neither of the above is true, you will be asked to locate the Music file on your computer.

That's it! The downloaded song should now be open for editing (or playback) in the Freetar Editor.

# Seek In The Editor #

If you right-click and drag the mouse on one of the note-tracks in the main editor window, you will pan ahead or behind in the song. This is helpfull for lining up the 'current time' bar.

# Matching up a downloaded song with your MP3(Ex. Live Shows) #

> Most of you are undoubtedly already privy to this by now, but I wanted to barf it out there for all to see because it certainly helped me and I wanted to share the wealth :D If your 'home' MP3 isn't matching up with a .sng file and you think all hope is lost, never fear: Ctrl+A is here! And then just use the right or left arrow keys to position the entire song's content at a different spot superimposed over the actual MP3. Experimentation led me to believe that the best way to sync up a file (that is, if the .sng is accurate to the actual song) is to line up the last strummed pitch(es) of the .sng with the last strummed pitch(es) of the .mp3. If the tabber did a good job, the rest of it should fall in line perfectly. I am wholeheartedly enjoying Billy Talent's 'The Ex' because of this nifty serendipity. Knock yourselves out!

Also take a look at the Download Songs section for step-by-step instructions on synchronizing your music to a downloaded .SNG file.

# Finding the Tempo of a song #

> All right, the last one was a no-brainer, but THIS one could very well be news to at least one person's ears! This is a procedure for honing in on the exact tempo of any song to the point where you can fill in the value for the BPS and have an evenly-charted demarcation of the beat and its subdivisions at all times as it scrolls by! First, find the actual tempo in BPM of the song. A metronome with a 'tap' function like a Boss "Dr. Beat" works perfectly in this situation, but if you don't have one of those just download a metronome program from Cnet or use a metronome of your own to find as close to the actual tempo as you can get. When you have this value... Divide it by 60 to obtain the true BPS for the song. Then, finally... Multiply this BPS value by whatever subdivision of the "quarter-note" beat you would like to have represented visually for you on the scale at the bottom of the editor screen. Most of the time sixteenth notes (multiply by 4) work just fine, occasionally thirty-second notes (multiply by 8) if you're trying to nail down a rippin' solo. Or eighth notes (multiply by 2), if that's your preference and the song works mostly in block chords or is just plain simple. Hmmm...perhaps I should include a guarantee of some sort in this post that I won't continue barraging the forums with tips that I'm assuming most folks have discovered already :P In any case, I hope that this is able to help some of you expert tabbers out there with your wonderful work! Until Anton can work his magic and create a tempo utility of some sort, this is about the best that one can do aside from being really familiar with a song, strumming the entire thing on a keyboard/controller, and being secure in their rhythmic stability as a human being ;D Cheers!