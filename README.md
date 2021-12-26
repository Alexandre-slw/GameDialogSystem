# GameDialogSystem
 Dialog system for games, implementation of the CodeParade youtube channel system for the Hyperbolica game, with the specifications described in the video https://www.youtube.com/watch?v=DlL_20x0QH8

All the examples given in the video have been implemented and work completely

# Simple usage
```Java
import com.salwyrr.dialog.Dialog;
import com.salwyrr.dialog.handler.DialogHandler;

import java.io.File;
import java.util.List;

public class SimpleDialogReader implements DialogHandler {

    public SimpleDialogReader() throws Exception {
        Dialog dialogInstance = new Dialog("example.dialog", new File("states")); // the File instance is the directory where dialog states will be savec
        dialogInstance.start("bob", this, character1, character2, character3); // replace "bob" by the entry point of the dialog, characters 1, 2, 3... are the characters interacting in the dialog, you can set any Object as a character

        while (dialogInstance.isRunning()) {
            dialogInstance.update();
        }
    }

    @Override
    public void display(String text, Dialog dialog) {
        System.out.println(text);
    }

    @Override
    public void displayChoices(List<String> choices, Dialog dialog) {
        
    }

    @Override
    public void event(Object narrator, String eventKey, Dialog dialog) {
        
    }

    @Override
    public void eventWithValue(Object narrator, String eventKey, String value, Dialog dialog) {
        
    }

    @Override
    public void switchNarrator(Object narrator, Dialog dialog) {
        System.out.println(narrator);
    }

    @Override
    public void switchNarratorAndLookTo(Object narrator, Object characterFacedByNarrator, Dialog dialog) {
        System.out.println(narrator + " > " + characterFacedByNarrator);
    }

    @Override
    public void waitingInput(Dialog dialog) {
        
    }
}
```

For a complete working example check the ConsoleDialogTest.java in test

If you want to have events such as emotions you have to implement them yourself, the events will be sent with the narrator instance using these functions in your DialogHandler
```Java
@Override
public void event(Object narrator, String eventKey, Dialog dialog) {
    // add your own events implementation
}

@Override
public void eventWithValue(Object narrator, String eventKey, String value, Dialog dialog) {
    // add your own events implementation
}
```

After each dialog lines, the dialog will wait until you ask him to continue (probably because of an user input) or after a few seconds if you didn't disabled it
```Java
Dialog.CHARS_PER_SECOND = 15; // optional, set the animation speed, default is 15 characters by second
Dialog.DIALOG_AUTO_SKIP = 10_000; // optional, set the auto skip time in millis (default is 10 seconds), or -1 to disable
dialogInstance.skip(); // skip the animation (character by character) until the end of the line, or go to the next dialogue line if the previous one was finished
```

You can also force exit a dialog using
```Java
dialogInstance.exit();
```

# Missions

Missions are an important part of the dialog system, there is 2 types of missions
- Missions activated when you enter a specific entry point in the dialog file
- Missions activated by the game

The first one is automatic, when you enter the correct dialog, the linked mission is enabled.
The second one is manually managed by the game, you can set a mission state using:
```Java
dialogInstance.setMissionState("mission_name", true/false);
```

The states of the missions are automatically saved in the states file.

# Dialog file formatting

For a detailed explained you should watch the [video of CodeParade](https://www.youtube.com/watch?v=DlL_20x0QH8)

## Entry points and events

Entry points - which are the point where part of a dialogue begins - are written using a # at the start of the line, entry points always have a "\_number" at the end of the line, \_1 is the called when you access this entry point for the first time, then if a \_2 exists, it will be called the second time... the last one is repeated each time you enter the entry point
> Note that when starting the dialog, you must specify the entry point without the "\_number", for the example below, the entry point is "bob", the number is concatenated automatically
```Java
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 00:28

#bob_1
[Talk1]Hi, my name is Bob.
[e_Happy][nod1]I can talk with multiple lines[Giggle] and use different emotions.
[HeadTilt][e_Excited]Pretty cool, huh?[Idle]
[e_Blink]
#bob_2
[Talk4][e_Shocked]This is the second thing that I say.
[e_Blink]
#bob_3
[Talk2]This is the last thing I say, [Thinking]so it will repeat each time you talk to me.
[Idle]
```

Between the \[ \] there is events, it is mainly used to set the emotions of the current narrator, as said previously, you must implement your own events for your game.
A pause if performed after each line of text, pending user input or automatic skip.

## Small pause and word grouping

Underscores (\_) are not displayed in the final dialog, instead they are used as small pause in the text, more underscores means more longer pause.
Vertical lines (|) are used to group multiple characters together, so they are displayed at the same time and not character by character.

```
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 01:24

#bob_1
[Talk2][e_Sultry]Dude...______ I just__ like,___ saw something...
[Talk1]It was called:____\n"Hyperbolic Pizza"____\nYup.
[Talk4][e_Bliss]|It|____ |Was|____ |So|____ |Awesome!|
[e_Blink]
```

Underscore pause time can be set using the code below, default is 150 ms
```
Dialog.UNDERSCORE_PAUSE_MILLIS = 150;
```

## Branching

Branching can be done easily using this formatting: `$entry_point Text to display`
You can also navigate to another entry point using the event syntax with an #: `[#entry_point]`

```
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 01:55

#bob_1
[Talk1][e_Confused]What's your most anticipated game?
$bob_hyperbolica Hyperbolica
$bob_somethingelse Something else
$bob_dontplay I don't play video games
#bob_hyperbolica_1
[e_Happy]Aw yeah.__ You're on my wavelength buddy.
#bob_hyperbolica_2
[e_Annoyed]Listen, I get it.
[ShakeHead]No need to lick my boots.
[e_Blink]
#bob_somethingelse_1
[e_Shocked]Oh...____ [LockDown1]I see.[e_KindaSad]
Well,___ [Idle]that's okay..._____ I guess.
[e_Sad][Talk1]I'm not crying, I swear!
#bob_dontplay_1
[e_Blink]Wait, [ShakeHead]that doesn't make sense.
[Shrug]I'll just ask you again.
[#bob]
```

In your DialogHandler, the choices for the branching will be sent using
```Java
@Override
public void displayChoices(List<String> choices, Dialog dialog) {

}
```

At this point, the dialog is stopped, waiting for a choice.
Once the user has chosen a branch, you can continue the dialog using the branch index (the first index is 0):
```Java
dialogInstance.choose(index);
```

## Narrator switching

To switch the narrator between multiple characters, you use the same syntax as an event but with the index of the character instead of an event name, \[0\] is always the initial narrator.
Switching between narrator also change which the character that must receive the events.

```
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 02:52

#alice_1
[Talk1][e_Sultry]Hey, are you that famous person?
[1][Shocked][e_Star]Whoa, it IS you.
[2][Happy1][e_Bliss]Hey can I have your autograph?
[0][e_KindaSad][Nervous]Actually, I wanted to ask you something...[Idle]
[1][e_Excited]No no, me first! Please.[e_Heart]
[Idle][e_Blink][2][e_Excited]No, me! me!
[Idle][e_Blink][0][e_Annoyed]Sorry about my friends, [Suspicious1]they get like this sometimes.
[Idle][e_Blink]

#bob_1
Hi, I'm Bob.

#charlie_1
Hi, I'm Charlie.
```

Small reminder, you need to specify the characters interacting in this dialog at the start of the dialog using:
```Java
dialogInstance.start("entry_point", this, character1, character2, character3, ...);
```
You can pass any Object as a character, such as a simple String for a name, or a complete Entity/NPC/... instance from your game engine.

## Narrator switching and looking at another character

It works just like narrator switching works, except that the format is \[narrator_index>character_index\], for example \[0>1\] means that the narrator is now character 0, and character 0 is looking at character 1 (to simulate a real discussion)
When switching to a narrator using simple format \[0\], the narrator is facing the player.

```
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 03:27

#alice_1
[Talk2][e_Excited]Hey I just saw the funniest GIF today.
[e_Blink][1>0][e_Bored][Talk1]Um actually,__ it's pronounced like "JIF"
[0>1][e_Annoyed]Yeah, but no one actually says that[Shrug] do they ?
[0>2][1>2][2>0]I hate to be a contrarian, but...
[Talk1]I've always just said it like the letters 'G' 'I' 'F'.
[0>1][2>1][1>2][Talk4][e_Angry]That's like literally the worst thing you could do!
[1>0][2>0][0>1][Talk1]You take this way to seriously.
[1][e_Blink][2][0][Talk3][e_Blink]Anyway, I don't want to get you all wrapped up in this debate.
So I won't even get your opinion.

#bob_1
Hi, I'm Bob.

#chalie_1
Hi, I'm Charlie.
```

## Missions

Missions are defined using the format:
`@entry_point mission1 mission2 ...`
The entry_point is the initial entry point, working even if no mission is enabled
Then if mission1 is enabled, it will be concatenated to the entry_point, so if you try to start a dialog at `entry_point`, it will be converted to `entry_point_mission1`
Note that the missions are written from the lowest priority to the highest from left to right, so if mission2 is enabled, it will be used no matter if mission1 is enabled or not.

```
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 04:10

@bob mission_complete friend_in_trouble
#bob_1
[Talk1]I need you to find my lost lucky penny.
[Talk2]Come back when you find it.
#bob_mission_complete_1
[e_Bliss][Happy1]Hey you found it!
[e_Excited]Thanks so much!
[e_Blink]
#bob_mission_complete_2
[e_Happy]Thanks again buddy.
[e_Blink]
#bob_friend_in_trouble_1
[e_KindaSad][Talk1]Hey, you really need to help my friend!
[e_Shocked][Talk4]It's important to advance the plot!
[e_Blink]
```

## Missions enabled by another entry point

If a mission has the name of another entry point of the same dialog file, it can be automatically enabled by starting a dialog at this entry point.
In the example below, the entry point "bob" has a mission called "alice" which is another entry point, so when the player will trigger the dialog at "#alice", the mission will be enabled, and now starting a dialog at "#bob" will redirect to "#bob_alice".
> Do not forget that the "\_1" at the end of an entry point is added automatically, there is no need to specify it when starting a dialog in your code. The only place you need to specify it is in the dialog file

```
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 05:03

@bob alice
#bob_1
[e_Closed][CrossArmTalk]I won't tell you anything unless you know the secret password.
[Idle]
#bob_alice_1
[Talk1]I see you know the password.
[Talk3]Very well,___ I will help your quest.

#alice_1
[e_Suspicious][Suspicious]The secret password is____ "abc123".
[e_Blink][Idle]
```

## Events with value

Initially, it was created to handle Unity components, but since we're not using Unity, that doesn't make sense. So it works just like a simple event, the only difference is that a value is specified after ":", so the format is `event_name:value`

```
// Example taken from the CodeParade video (https://youtu.be/DlL_20x0QH8) at 05:33

#bob_1
[e_KindaSad][Nervous]Hey, I know we like..._____ just met.
[Talk2]But I have something___ dramatic to tell you.
[Talk1]So dramatic,____[FadeAudioEvent:0] that we needed to stop the music.
[Talk3]You see...
[e_Closed][LookDown1]I'm...
...
[Talk4][e_Happy][FadeAudioEvent:1]...just kidding!
[Shrug][e_Excited]Yeah, nothing wrong here.
[Giggle][e_Bliss]Haha, got you.
[e_Blink]
```

That's all for now, again if you want to see the system in action or have a more detailed explanation go watch the video of CodeParade.
