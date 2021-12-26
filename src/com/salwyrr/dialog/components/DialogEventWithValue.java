package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

public class DialogEventWithValue extends DialogPart {

    private final String eventKey;
    private final String value;

    public DialogEventWithValue(String eventKey, String value) {
        this.eventKey = eventKey;
        this.value = value;
    }

    @Override
    public boolean apply(Dialog dialog) {
        dialog.getHandler().eventWithValue(dialog.getNarrator(), this.eventKey, this.value, dialog);

        return true;
    }

    public String getEventKey() {
        return this.eventKey;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean needWait() {
        return true;
    }
}
