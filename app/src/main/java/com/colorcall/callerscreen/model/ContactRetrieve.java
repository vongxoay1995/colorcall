package com.colorcall.callerscreen.model;

public class ContactRetrieve {
    private String name,contact_id;

    public ContactRetrieve(String name, String contact_id) {
        this.name = name;
        this.contact_id = contact_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }
}
