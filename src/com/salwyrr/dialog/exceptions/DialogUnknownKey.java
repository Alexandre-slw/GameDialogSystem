package com.salwyrr.dialog.exceptions;

import com.salwyrr.dialog.Dialog;

public class DialogUnknownKey extends Exception {
    private final String key;
    private final Dialog dialog;

    public DialogUnknownKey(String key, Dialog dialog) {
        this.key = key;
        this.dialog = dialog;
    }

    @Override
    public String getMessage() {
        return String.format("Unknown key \"%s\" in dialog \"%s\"", this.key, this.dialog.getDialogFile());
    }

    public String getKey() {
        return this.key;
    }

    public Dialog getDialog() {
        return this.dialog;
    }
}
