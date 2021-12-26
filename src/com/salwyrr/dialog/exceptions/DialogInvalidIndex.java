package com.salwyrr.dialog.exceptions;

import com.salwyrr.dialog.Dialog;

public class DialogInvalidIndex extends Exception {
    private final int index;
    private final Dialog dialog;

    public DialogInvalidIndex(int index, Dialog dialog) {
        this.index = index;
        this.dialog = dialog;
    }

    @Override
    public String getMessage() {
        return String.format("Invalid index \"%d\" in dialog \"%s\"", this.index, this.dialog.getDialogFile());
    }

    public int getIndex() {
        return this.index;
    }

    public Dialog getDialog() {
        return this.dialog;
    }
}
