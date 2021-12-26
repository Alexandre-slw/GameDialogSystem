package com.salwyrr.dialog.components;

import com.salwyrr.dialog.Dialog;

public class DialogPart {

    private DialogPart next = null;

    public boolean apply(Dialog dialog) {
        return true;
    }

    public void reset() {

    }

    public void setNext(DialogPart next) {
        this.next = next;
    }

    public DialogPart getNext() {
        return this.next;
    }

    public DialogPart getLast() {
        if (this.getNext() == null) return this;
        return this.getNext().getLast();
    }

    public boolean needWait() {
        return false;
    }
}
