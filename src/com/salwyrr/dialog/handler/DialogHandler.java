package com.salwyrr.dialog.handler;

import com.salwyrr.dialog.Dialog;

import java.util.List;

public interface DialogHandler {

    /**
     * This function is used to display the current text of the dialog to the user, the given text is already "animated" by giving it character by character
     * @param text The text to display
     * @param dialog Dialog instance
     */
    void display(String text, Dialog dialog);

    /**
     * This function is used to display the choices of a dialog to the user, the selected choice must be given to the dialog instance using Dialog.
     * @param choices List of choices, must be display as text
     * @param dialog Dialog instance
     */
    void displayChoices(List<String> choices, Dialog dialog);

    /**
     * This function is used to handle the events placed in [] in the dialog file, such as narrator animations
     * @param narrator the current narrator, null if no character has been given
     * @param eventKey the key placed into [] in the dialog file
     * @param dialog Dialog instance
     */
    void event(Object narrator, String eventKey, Dialog dialog);

    /**
     * This function is used to handle the events placed in [] in the dialog file, such as narrator animations
     * @param narrator the current narrator, null if no character has been given
     * @param eventKey the key placed into [] before the ":" in the dialog file
     * @param value the value placed into [] after the ":" in the dialog file
     * @param dialog Dialog instance
     */
    void eventWithValue(Object narrator, String eventKey, String value, Dialog dialog);

    /**
     * This function is used to set the narrator, currently speaking or acting, you probably want to handle that by moving the camera or changing the name of narrator in your display and making the narrator facing the user
     * @param narrator the narrator which is an object given as one of the character
     * @param dialog Dialog instance
     */
    void switchNarrator(Object narrator, Dialog dialog);

    /**
     * This function is used to set the narrator, currently speaking or acting, you probably want to handle that by moving the camera or changing the name of narrator in your display and making the narrator facing the character
     * @param narrator the narrator which is an object given as one of the character
     * @param characterFacedByNarrator the character the narrator is looking at
     * @param dialog Dialog instance
     */
    void switchNarratorAndLookTo(Object narrator, Object characterFacedByNarrator, Dialog dialog);

    /**
     * This function is used to warn that the dialog is currently waiting the {@link Dialog#skip()} to continue, or will automatically skip according to {@link Dialog#DIALOG_AUTO_SKIP}
     * @param dialog Dialog instance
     */
    void waitingInput(Dialog dialog);

}
