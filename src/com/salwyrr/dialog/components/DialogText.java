package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

import java.util.ArrayList;
import java.util.List;

public class DialogText extends DialogPart {

    private final List<String> initialTextParts = new ArrayList<>();
    private List<String> textParts;
    private long lastTime = -1;

    public DialogText(String text) {
        String[] args = text.replace("\\n", "\n").split("\\|");

        int i = 0;
        for (String part : args) {
            if (i % 2 == 0) {
                for (char c : part.toCharArray()) {
                    this.initialTextParts.add(String.valueOf(c));
                }
            } else {
                this.initialTextParts.add(part);
            }
            i++;
        }

        this.reset();
    }

    @Override
    public void reset() {
        this.lastTime = -1;
        this.textParts = new ArrayList<>(this.initialTextParts);
    }

    @Override
    public boolean apply(Dialog dialog) {
        if (this.lastTime == -1) this.lastTime = dialog.getDialogStart();

        long current = System.currentTimeMillis();
        int count = (int) (((current - this.lastTime) / 1000.0F) * Dialog.CHARS_PER_SECOND);
        if (dialog.getDialogStart() == Long.MAX_VALUE) count = Integer.MAX_VALUE;

        int limit = Math.min(this.textParts.size(), count);
        boolean max = limit == this.textParts.size();
        boolean edited = false;
        for (int i = 0; i < limit; i++) {
            String part = this.textParts.remove(0);
            if (part.equals("_")) {
                if (count < Integer.MAX_VALUE) {
                    this.lastTime = current + Dialog.UNDERSCORE_PAUSE_MILLIS;
                    count = 0;
                    break;
                } else {
                    continue;
                }
            }
            if (part.equals(" ") && !max) i--;

            edited = true;
            dialog.getCurrentText().append(part);
        }

        if (count >= 1) this.lastTime = current;
        if (edited) dialog.getHandler().display(dialog.getCurrentText().toString(), dialog);

        if (this.textParts.isEmpty()) {
            this.reset();
            return true;
        }

        return false;
    }

    @Override
    public boolean needWait() {
        return true;
    }
}
