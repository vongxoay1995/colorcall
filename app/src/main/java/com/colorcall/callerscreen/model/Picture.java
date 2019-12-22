package com.colorcall.callerscreen.model;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "PICTURE")
public class Picture {
    @Id(autoincrement = true)
    @Property(nameInDb = "ID")
    private long id = 0;
    @Property(nameInDb = "NAME")
    private String name;

    @Property(nameInDb = "THUMBURL")
    private String thumbUrl;

    @Property(nameInDb = "ISDELETE")
    private boolean delete;

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public Picture() {
    }

    public Picture(String name, String thumbUrl, boolean delete) {
        this.name = name;
        this.thumbUrl = thumbUrl;
        this.delete = delete;
    }

    @Generated(hash = 1886069808)
    public Picture(long id, String name, String thumbUrl, boolean delete) {
        this.id = id;
        this.name = name;
        this.thumbUrl = thumbUrl;
        this.delete = delete;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getthumbUrl() {
        return thumbUrl;
    }

    public void setthumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getThumbUrl() {
        return this.thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public boolean getDelete() {
        return this.delete;
    }
}

