package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

public class DialogSwitchNarrator extends DialogPart {

    private final int narratorID;

    public DialogSwitchNarrator(int narratorID) {
        this.narratorID = narratorID;
    }

    @Override
    public boolean apply(Dialog dialog) {
        if (this.narratorID < 0 || this.narratorID >= dialog.getCharacters().length) return true;

        dialog.setNarratorID(this.narratorID);
        dialog.getHandler().switchNarrator(dialog.getCharacters()[this.narratorID], dialog);

        return true;
    }

    public int getNarratorID() {
        return this.narratorID;
    }

    @Override
    public boolean needWait() {
        return true;
    }
}
