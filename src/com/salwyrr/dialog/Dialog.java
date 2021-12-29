package com.salwyrr.dialog;

import com.salwyrr.dialog.components.*;
import com.salwyrr.dialog.exceptions.DialogInvalidIndex;
import com.salwyrr.dialog.exceptions.DialogUnknownKey;
import com.salwyrr.dialog.handler.DialogHandler;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dialog implements Serializable {

    /**
     * Set the animation speed
     */
    public static int CHARS_PER_SECOND = 15;
    /**
     * Set the underscore pause time in milliseconds
     */
    public static int UNDERSCORE_PAUSE_MILLIS = 150;
    /**
     * Set the time in milliseconds before the dialog continue, even if the user didn't press any key (-1 to disable)
     */
    public static int DIALOG_AUTO_SKIP = 10_000;

    private static final Pattern pattern = Pattern.compile("\\[([^\\[\\]]*)]");

    private final HashMap<String, Integer> dialogPartCount = new HashMap<>();
    private final HashMap<String, DialogPart> dialogPartComponents = new HashMap<>();
    private final LinkedHashMap<String, LinkedHashMap<String, Boolean>> missions = new LinkedHashMap<>();
    private List<String> currentChoices = new ArrayList<>();
    private final String dialogFile;

    private String currentKey = "";
    private long dialogStart = 0;
    private DialogPart currentDialogPart = null;
    private StringBuilder currentText = new StringBuilder();

    private DialogHandler handler;

    private Object[] characters = new Object[0];
    private int narratorID = 0;

    private final DialogStateSaver dialogStateSaver;

    /**
     * Create a new Dialog instance
     * @param dialogFile the dialog file to load
     * @param saveStateDirectory the directory used to save the states of this dialog
     */
    public Dialog(String dialogFile, File saveStateDirectory) throws IOException {
        this.dialogFile = dialogFile;
        this.dialogStateSaver = new DialogStateSaver(new File(saveStateDirectory, dialogFile + ".state"));
        this.loadDialog();
    }

    /**
     * Load the dialog file, can be used to reload dialogs in case of changes
     */
    public void loadDialog() throws IOException {
        this.dialogPartCount.clear();
        this.dialogPartComponents.clear();
        this.missions.clear();
        this.exit(true);

        this.dialogStateSaver.load();

        InputStream inputStream = Dialog.class.getClassLoader().getResourceAsStream(this.dialogFile);

        BufferedReader bufferedReader;
        if (inputStream == null) {
            bufferedReader = new BufferedReader(new FileReader(this.dialogFile));
        } else {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }

        String currentKey = "";
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("//")) continue;
            if (line.replaceAll("\\s", "").isEmpty()) continue;

            if (line.startsWith("#")) {
                currentKey = line.substring(1);
                String simpleKey = currentKey.substring(0, currentKey.length() - 2);
                this.dialogPartCount.put(simpleKey, Integer.parseInt(this.dialogStateSaver.get("#" + simpleKey, "1")));
                this.dialogPartComponents.put(currentKey, new DialogPart());
            } else if (line.startsWith("@")) {
                String key = line.split(" ")[0].substring(1);
                String[] missions = line.substring(key.length() + 2).split(" ");
                this.missions.put(key, new LinkedHashMap<>());

                for (String mission : missions) {
                    this.missions.get(key).put(mission, this.dialogStateSaver.get("@" + key + "_" + mission, "false").equalsIgnoreCase("true"));
                }
            } else if (line.startsWith("$") && !currentKey.isEmpty()) {
                DialogPart part = this.dialogPartComponents.get(currentKey).getLast();
                if (!(part instanceof DialogChoice)) {
                    part.setNext(new DialogChoice());
                    part = part.getNext();
                }

                String key = line.substring(1).split(" ")[0];
                ((DialogChoice) part).getKeys().add(key);
                ((DialogChoice) part).getChoices().add(line.substring(key.length() + 2));
            } else if (!currentKey.isEmpty()) {
                DialogPart part = this.dialogPartComponents.get(currentKey);

                boolean edited = false;
                String initalLine = line;
                Matcher matcher = Dialog.pattern.matcher(line);
                while (matcher.find()) {
                    line = line.replaceFirst(Pattern.quote(matcher.group(0)), "");

                    int matchIndex = initalLine.indexOf(matcher.group(0));
                    int lastIndex = initalLine.lastIndexOf("§");

                    if (lastIndex != -1 && lastIndex + 1 < matchIndex) {
                        String text = initalLine.substring(lastIndex + 1, matchIndex);
                        if (!text.replaceAll("\\s", "").isEmpty()) {
                            part.getLast().setNext(new DialogText(text));
                            edited = true;
                            line = line.replaceFirst(Pattern.quote(text), "");
                        }
                    }

                    StringBuilder replacement = new StringBuilder();
                    for (int i = 0; i < matcher.group(0).length(); i++) {
                        replacement.append("§");
                    }
                    initalLine = initalLine.replaceFirst(Pattern.quote(matcher.group(0)), replacement.toString());

                    String key = matcher.group(1);
                    if (key.startsWith("#")) {
                        part.getLast().setNext(new DialogGoTo(key.substring(1)));
                    } else if (key.replaceAll("[0-9]", "").isEmpty()) {
                        int index = Integer.parseInt(key);
                        part.getLast().setNext(new DialogSwitchNarrator(index));
                    } else if (key.replaceFirst("[0-9]*>[0-9]*", "").isEmpty()) {
                        String[] args = key.split(">");
                        int indexNarrator = Integer.parseInt(args[0]);
                        int indexCharacter = Integer.parseInt(args[1]);
                        part.getLast().setNext(new DialogSwitchNarratorAndLookTo(indexNarrator, indexCharacter));
                    } else if (key.contains(":")) {
                        String[] args = key.split(":");
                        part.getLast().setNext(new DialogEventWithValue(args[0], args[1]));
                    } else {
                        part.getLast().setNext(new DialogEvent(key));
                    }
                }

                if (edited || !line.replaceAll("\\s", "").isEmpty()) {
                    part.getLast().setNext(new DialogText(line));
                    part.getLast().setNext(new DialogNewLine());
                }
            }
        }

        bufferedReader.close();
    }

    /**
     * Set the state of a mission to enable or disable the associated dialogs
     * @param name The name of the mission
     * @param value if true, the associated dialogs will be enabled
     */
    public void setMissionState(String name, boolean value) {
        boolean edited = false;
        for (Map.Entry<String, LinkedHashMap<String, Boolean>> entry : this.missions.entrySet()) {
            if (!entry.getValue().containsKey(name)) continue;
            entry.getValue().put(name, value);
            this.dialogStateSaver.set("@" + entry.getKey() + "_" + name, String.valueOf(value));
            edited = true;
        }

        if (edited) {
            this.saveStates();
        }
    }

    /**
     * Continue the dialog with the selected choice
     * @param choiceIndex The index in the List of choices
     * @throws DialogInvalidIndex when the index is < 0 or >= the size of the list of choices
     * @throws DialogUnknownKey if the selected choice lead to an undefined dialog key
     */
    public void choose(int choiceIndex) throws DialogInvalidIndex, DialogUnknownKey {
        if (choiceIndex < 0 || choiceIndex >= this.currentChoices.size()) throw new DialogInvalidIndex(choiceIndex, this);

        this.read(this.currentChoices.get(choiceIndex));
    }

    /**
     * Start the dialog at the selected entry key, works only if the dialog is not running
     * @param key the key used to know where to start the dialog
     * @param handler handler for dialog actions
     * @param characters the characters who participate in the dialog, the first is the initial narrator
     * @throws DialogUnknownKey if the key is not defined in the dialog file
     * @return true if the dialog started at the selected key, false is already running
     */
    public boolean start(String key, DialogHandler handler, Object... characters) throws DialogUnknownKey {
        if (this.isRunning()) return false;

        this.characters = characters;

        this.handler = handler;
        this.read(key);
        return true;
    }

    public void read(String key) throws DialogUnknownKey {
        if (this.missions.containsKey(key)) {
            LinkedHashMap<String, Boolean> missions = this.missions.get(key);

            String m = "";
            for (Map.Entry<String, Boolean> mission : missions.entrySet()) {
                if (mission.getValue()) {
                    m = mission.getKey();
                }
            }

            if (!m.isEmpty() && this.dialogPartCount.containsKey(key + "_" + m)) {
                key += "_" + m;
            }
        }

        if (!this.dialogPartCount.containsKey(key)) throw new DialogUnknownKey(key, this);

        this.currentKey = key;

        int count = this.dialogPartCount.get(key);

        this.handler.switchNarrator(this.getNarrator(), this);
        this.resetAnimation();
        this.currentChoices.clear();
        this.currentText = new StringBuilder();
        this.currentDialogPart = this.dialogPartComponents.get(key + "_" + count);
        this.setMissionState(key, true);
    }

    /**
     * Update the dialog, should be done at each frame to have a correct char by char animation
     * @return true if the dialog is finished, if false the dialog update must be done again at the next frame
     */
    public boolean update() {
        while (this.currentDialogPart != null && this.currentDialogPart.apply(this)) {
            this.currentDialogPart = this.currentDialogPart.getNext();
            this.resetAnimation();
        }

        if (this.currentDialogPart == null) {
            int count = this.dialogPartCount.get(this.currentKey);
            if (this.dialogPartComponents.containsKey(this.currentKey + "_" + (count + 1))) {
                this.dialogPartCount.put(this.currentKey, count + 1);
                this.dialogStateSaver.set("#" + this.currentKey, String.valueOf(count + 1));
            }
            this.exit(false);
        }

        return this.currentDialogPart == null;
    }

    /**
     * Exit the dialog
     */
    public void exit(boolean clearChoices) {
        for (DialogPart part : this.dialogPartComponents.values()) {
            part.reset();
        }

        this.currentKey = "";
        if (clearChoices) this.currentChoices.clear();
        this.currentDialogPart = null;
        this.handler = null;

        this.characters = new Object[0];
        this.narratorID = 0;

        this.saveStates();
    }

    /**
     * Save the states (missions, dialog counts) in the state file
     */
    public void saveStates() {
        this.dialogStateSaver.save();
    }

    /**
     * Clear all the states (missions, dialog counts) and save the state file
     */
    public void clearStates() {
        this.dialogStateSaver.clear();
        this.dialogStateSaver.save();
    }

    /**
     *
     * @return true if the dialog is still running
     */
    public boolean isRunning() {
        return this.currentDialogPart != null;
    }

    /**
     * Skip the text animation (char by char) or switch to the next dialog lines
     */
    public void skip() {
        this.dialogStart = Long.MAX_VALUE;
    }

    private void resetAnimation() {
        this.dialogStart = System.currentTimeMillis();
    }

    public StringBuilder getCurrentText() {
        return this.currentText;
    }

    public void clearCurrentText() {
        this.currentText = new StringBuilder();
    }

    public String getDialogFile() {
        return this.dialogFile;
    }

    public long getDialogStart() {
        return this.dialogStart;
    }

    public DialogHandler getHandler() {
        return this.handler;
    }

    public void setCurrentChoices(List<String> currentChoices) {
        this.currentChoices = new ArrayList<>(currentChoices);
    }

    public Set<String> getEntryPoints() {
        return this.dialogPartCount.keySet();
    }

    public Object[] getCharacters() {
        return this.characters;
    }

    public void setNarratorID(int narratorID) {
        if (narratorID < 0 || narratorID >= this.characters.length) return;
        this.narratorID = narratorID;
    }

    public Object getNarrator() {
        if (this.characters.length == 0) return null;
        return this.characters[this.narratorID];
    }

    public LinkedHashMap<String, LinkedHashMap<String, Boolean>> getMissions() {
        return this.missions;
    }
}
