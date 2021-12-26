package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;
import com.salwyrr.dialog.exceptions.DialogUnknownKey;

public class DialogGoTo extends DialogPart {

    private final String key;

    public DialogGoTo(String key) {
        this.key = key;
    }

    @Override
    public boolean apply(Dialog dialog) {
        try {
            dialog.read(this.key);
        } catch (DialogUnknownKey e) {
            e.printStackTrace();
        }

        return true;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public boolean needWait() {
        return true;
    }
}
