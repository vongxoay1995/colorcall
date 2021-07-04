package com.colorcall.callerscreen.contact;

import java.util.Objects;

public class ContactInfor {
    public boolean isChecked;
    public String chosenTheme;
    public String contactId;
    public String displayName;
    public String number;
    public String photo;
    public ContactInfor() {
    }

    public ContactInfor(String contactId, String displayName, String number, String photo) {
        this(contactId, displayName, number, photo, null);
    }

    public ContactInfor(String contactId, String displayName, String number, String photo, String chosenTheme) {
        this.contactId = contactId;
        this.displayName = displayName;
        this.number = number;
        this.photo = photo;
        this.chosenTheme = chosenTheme;
    }

    public void copy(ContactInfor contactInfor) {
        this.contactId = contactInfor.contactId;
        this.displayName = contactInfor.displayName;
        this.number = contactInfor.number;
        this.photo = contactInfor.photo;
        this.chosenTheme = contactInfor.chosenTheme;
        this.isChecked = contactInfor.isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getChosenTheme() {
        return chosenTheme;
    }

    public void setChosenTheme(String chosenTheme) {
        this.chosenTheme = chosenTheme;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || ContactInfor.class != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.contactId, ((ContactInfor) obj).contactId);
    }

    public int hashCode() {
        return Objects.hash(this.contactId);
    }

    @Override
    public String toString() {
        return "ContactInfor{" +
                "isChecked=" + isChecked +
                ", displayName='" + displayName + '\n' +
                '}';
    }
}
