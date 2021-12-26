package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

public class DialogEvent extends DialogPart {

    private final String eventKey;

    public DialogEvent(String eventKey) {
        this.eventKey = eventKey;
    }

    @Override
    public boolean apply(Dialog dialog) {
        dialog.getHandler().event(dialog.getNarrator(), this.eventKey, dialog);

        return true;
    }

    public String getEventKey() {
        return this.eventKey;
    }

    @Override
    public boolean needWait() {
        return true;
    }
}
