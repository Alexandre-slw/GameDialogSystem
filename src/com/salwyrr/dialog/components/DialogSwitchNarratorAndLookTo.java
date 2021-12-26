package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

public class DialogSwitchNarratorAndLookTo extends DialogPart {

    private final int narratorID;
    private final int characterID;

    public DialogSwitchNarratorAndLookTo(int narratorID, int characterID) {
        this.narratorID = narratorID;
        this.characterID = characterID;
    }

    @Override
    public boolean apply(Dialog dialog) {
        if (this.narratorID < 0 || this.narratorID >= dialog.getCharacters().length) return true;
        if (this.characterID < 0 || this.characterID >= dialog.getCharacters().length) return true;

        dialog.setNarratorID(this.narratorID);
        dialog.getHandler().switchNarratorAndLookTo(dialog.getCharacters()[this.narratorID], dialog.getCharacters()[this.characterID], dialog);

        return true;
    }

    public int getNarratorID() {
        return this.narratorID;
    }

    public int getCharacterID() {
        return this.characterID;
    }

    @Override
    public boolean needWait() {
        return true;
    }
}
