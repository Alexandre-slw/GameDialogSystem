package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

import java.util.ArrayList;
import java.util.List;

public class DialogChoice extends DialogPart {

    private final List<String> keys = new ArrayList<>();
    private final List<String> choices = new ArrayList<>();

    @Override
    public boolean apply(Dialog dialog) {
        dialog.setCurrentChoices(this.keys);
        dialog.getHandler().displayChoices(this.choices, dialog);

        return true;
    }

    public List<String> getKeys() {
        return this.keys;
    }

    public List<String> getChoices() {
        return this.choices;
    }
}
