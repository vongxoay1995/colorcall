package com.colorcall.callerscreen.model;

public class SignMainImage {
    private boolean isCompleted;
    private boolean isSwiped;

    public SignMainImage(boolean isCompleted, boolean isRefresh) {
        this.isCompleted = isCompleted;
        this.isSwiped = isRefresh;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public boolean isSwiped() {
        return isSwiped;
    }

    public void setSwiped(boolean swiped) {
        isSwiped = swiped;
    }
}
