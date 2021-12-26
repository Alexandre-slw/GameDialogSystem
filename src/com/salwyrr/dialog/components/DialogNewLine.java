package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

public class DialogNewLine extends DialogPart {

    private long lastTime = -1;

    @Override
    public void reset() {
        this.lastTime = -1;
    }

    @Override
    public boolean apply(Dialog dialog) {
        if (this.lastTime == -1) {
            if (this.getNext() == null || this.getNext().needWait()) dialog.getHandler().waitingInput(dialog);
            this.lastTime = dialog.getDialogStart();
        }

        if (dialog.getDialogStart() == Long.MAX_VALUE) {
            dialog.clearCurrentText();
            this.reset();
            return true;
        }

        if (Dialog.DIALOG_AUTO_SKIP == -1) return false;

        if ((this.getNext() != null && !this.getNext().needWait()) || System.currentTimeMillis() - this.lastTime > Dialog.DIALOG_AUTO_SKIP) {
            dialog.clearCurrentText();
            this.reset();
            return true;
        }

        return false;
    }
}
