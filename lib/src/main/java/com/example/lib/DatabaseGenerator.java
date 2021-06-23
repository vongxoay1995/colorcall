package com.example.lib;
import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;
public class DatabaseGenerator {
    private static final String PROJECT_DIR = System.getProperty("user.dir");
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(2, "com.colorcall.callerscreen.database");
        createTable(schema);
        new DaoGenerator().generateAll(schema, PROJECT_DIR+"/app/src/main/java/");
    }

    private static void createTable(Schema schema) {
        Entity personal = schema.addEntity("Background");
        personal.addIdProperty().autoincrement();
        personal.addIntProperty("type").notNull();
        personal.addStringProperty("pathThumb").notNull();
        personal.addStringProperty("pathItem").notNull();
        personal.addBooleanProperty("delete").notNull();


        Entity contact = schema.addEntity("Contact");
        contact.addIdProperty().autoincrement();
        contact.addStringProperty("contact_id").notNull();
        contact.addStringProperty("background_path").notNull();
    }
}
