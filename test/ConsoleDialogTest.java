import com.salwyrr.dialog.Dialog;
import com.salwyrr.dialog.exceptions.DialogInvalidIndex;
import com.salwyrr.dialog.exceptions.DialogUnknownKey;
import com.salwyrr.dialog.handler.DialogHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleDialogTest implements DialogHandler {

    private static final LinkedHashMap<String, Dialog> dialogs = new LinkedHashMap<>();
    private String narrator = "";
    private String lookingAt = "";

    public static void main(String[] args) throws Exception {
        new ConsoleDialogTest();
    }

    public ConsoleDialogTest() throws Exception {
        File statesDirectory = new File("states/");
        ConsoleDialogTest.dialogs.put("repeated", new Dialog("repeated.dialog", statesDirectory));
        ConsoleDialogTest.dialogs.put("pause_group", new Dialog("pause_group.dialog", statesDirectory));
        ConsoleDialogTest.dialogs.put("branching", new Dialog("branching.dialog", statesDirectory));
        ConsoleDialogTest.dialogs.put("switch_narrator", new Dialog("switch_narrator.dialog", statesDirectory));
        ConsoleDialogTest.dialogs.put("looking_at", new Dialog("looking_at.dialog", statesDirectory));
        ConsoleDialogTest.dialogs.put("mission", new Dialog("mission.dialog", statesDirectory));
        ConsoleDialogTest.dialogs.put("mission_talk", new Dialog("mission_talk.dialog", statesDirectory));
        ConsoleDialogTest.dialogs.put("events_with_value", new Dialog("events_with_value.dialog", statesDirectory));

        boolean stop = false;
        Scanner scanner = new Scanner(System.in);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            Dialog dialog = null;
            while (dialog == null) {
                System.out.println("Selected the dialog (type 'quit' to exit):");
                for (String key : ConsoleDialogTest.dialogs.keySet()) {
                    System.out.println("-> " + key);
                }
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("quit")) {
                    stop = true;
                    break;
                }

                dialog = ConsoleDialogTest.dialogs.getOrDefault(input, null);
            }

            if (stop) break;

            if (!dialog.getMissions().isEmpty()) {
                System.out.println("This dialog contains missions, do you want to edit them ? (\"yes\" to edit the mission state, \"no\" to play the dialog)");
                if (scanner.nextLine().equalsIgnoreCase("yes")) {
                    System.out.println("Set the mission value (mission_name=true/false):");
                    for (Map.Entry<String, LinkedHashMap<String, Boolean>> entry : dialog.getMissions().entrySet()) {
                        for (Map.Entry<String, Boolean> mission : entry.getValue().entrySet()) {
                            System.out.println("-> " + mission.getKey() + "=" + mission.getValue());
                        }
                    }
                    String[] input = scanner.nextLine().split("=");
                    dialog.setMissionState(input[0], input[1].equalsIgnoreCase("true"));
                }
            }

            System.out.println("Select the characters/NPC (separated by a comma):");
            String[] characters = scanner.nextLine().split(",");

            String initialKey = null;
            while (initialKey == null) {
                System.out.println("Select dialog entry point:");
                for (String key : dialog.getEntryPoints()) {
                    System.out.println("-> " + key);
                }

                try {
                    initialKey = scanner.nextLine();
                    dialog.start(initialKey, this, (Object[]) characters);
                } catch (Exception e) {
                    initialKey = null;
                }
            }

            while (dialog.isRunning()) {
                dialog.update();

                if (bufferedReader.ready()) {
                    bufferedReader.readLine();
                    dialog.skip();
                }
            }
        }

        bufferedReader.close();
    }

    @Override
    public void display(String text, Dialog dialog) {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        if (this.lookingAt != null && !this.lookingAt.isEmpty()) {
            System.out.println(this.narrator + " > " + this.lookingAt + ": " + text);
        } else if (this.narrator != null && !this.narrator.isEmpty()) {
            System.out.println(this.narrator + ": " + text);
        } else {
            System.out.println(text);
        }
    }

    @Override
    public void displayChoices(List<String> choices, Dialog dialog) {
        int i = 1;
        for (String choice : choices) {
            System.out.println(i + " -> " + choice);
            i++;
        }

        int index = -1;
        while (index <= 0 || index > choices.size()) {
            try {
                index = Integer.parseInt(new Scanner(System.in).nextLine());
            } catch (Exception e) {
                index = -1;
            }
        }

        try {
            dialog.choose(index - 1);
        } catch (DialogInvalidIndex | DialogUnknownKey e) {
            e.printStackTrace();
        }
    }

    @Override
    public void event(Object narrator, String eventKey, Dialog dialog) {
        System.out.println("**" + narrator + ": " + eventKey + "**");
    }

    @Override
    public void eventWithValue(Object narrator, String eventKey, String value, Dialog dialog) {
        System.out.println("[" + narrator + ": " + eventKey + " = " + value + "]");
    }

    @Override
    public void switchNarrator(Object narrator, Dialog dialog) {
        System.out.println(narrator);
        this.narrator = (String) narrator;
        this.lookingAt = "";
    }

    @Override
    public void switchNarratorAndLookTo(Object narrator, Object characterFacedByNarrator, Dialog dialog) {
        System.out.println(narrator + " > " + characterFacedByNarrator);
        this.narrator = (String) narrator;
        this.lookingAt = (String) characterFacedByNarrator;
    }

    @Override
    public void waitingInput(Dialog dialog) {
        System.out.println("Press \"enter\" to continue...");
    }
}
