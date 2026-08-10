package com.colorcall.callerscreen.r8;

import com.colorcall.callerscreen.dialer.models.CallContact;
import com.colorcall.callerscreen.dialer.models.PhoneAccountHandleModel;
import com.colorcall.callerscreen.dialer.models.SpeedDial;
import com.colorcall.callerscreen.database.Background;
import com.colorcall.callerscreen.model.AdsConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.simplemobiletools.commons.models.AlarmSound;
import com.simplemobiletools.commons.models.PhoneNumber;
import com.simplemobiletools.commons.models.contacts.Address;
import com.simplemobiletools.commons.models.contacts.Email;
import com.simplemobiletools.commons.models.contacts.Event;
import com.simplemobiletools.commons.models.contacts.IM;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class R8SerializationContractTest {
    private final Gson gson = new Gson();

    @Test
    public void speedDial_readsLegacyObfuscatedJson_andWritesCanonicalJson() {
        SpeedDial speedDial = gson.fromJson(
                "{\"a\":2,\"b\":\"0123456789\",\"c\":\"Home\"}",
                SpeedDial.class
        );

        assertEquals(2, speedDial.getId());
        assertEquals("0123456789", speedDial.getNumber());
        assertEquals("Home", speedDial.getDisplayName());
        assertTrue(speedDial.isValid());
        assertCanonicalKeys(speedDial, "id", "number", "displayName");
    }

    @Test
    public void phoneAccount_readsLegacyObfuscatedJson_andRejectsMissingIdentity() {
        PhoneAccountHandleModel model = gson.fromJson(
                "{\"a\":\"com.android.phone\",\"b\":\".TelecomService\",\"c\":\"sim-1\"}",
                PhoneAccountHandleModel.class
        );

        assertTrue(model.isValid());
        assertEquals("com.android.phone", model.getPackageName());
        assertEquals(".TelecomService", model.getClassName());
        assertEquals("sim-1", model.getId());
        assertCanonicalKeys(model, "packageName", "className", "id");

        PhoneAccountHandleModel invalid = gson.fromJson("{\"a\":null,\"b\":null}", PhoneAccountHandleModel.class);
        assertFalse(invalid.isValid());
    }

    @Test
    public void callContact_readsLegacyObfuscatedJson_andWritesCanonicalJson() {
        CallContact contact = gson.fromJson(
                "{\"a\":7,\"b\":\"Alice\",\"c\":\"photo\",\"d\":\"0909\",\"e\":\"Mobile\"}",
                CallContact.class
        );

        assertEquals(7, contact.getContactId());
        assertEquals("Alice", contact.getName());
        assertEquals("0909", contact.getNumber());
        assertCanonicalKeys(contact, "contactId", "name", "photoUri", "number", "numberLabel");
    }

    @Test
    public void roomContactConverters_readLegacyObfuscatedJson_andWriteCanonicalJson() {
        Address address = gson.fromJson("{\"a\":\"Street\",\"b\":1,\"c\":\"Home\"}", Address.class);
        Email email = gson.fromJson("{\"a\":\"a@example.com\",\"b\":2,\"c\":\"Work\"}", Email.class);
        Event event = gson.fromJson("{\"a\":\"2000-01-01\",\"b\":3}", Event.class);
        IM im = gson.fromJson("{\"a\":\"alice\",\"b\":4,\"c\":\"Chat\"}", IM.class);

        assertEquals("Street", address.getValue());
        assertEquals("a@example.com", email.getValue());
        assertEquals("2000-01-01", event.getValue());
        assertEquals("alice", im.getValue());
        assertCanonicalKeys(address, "value", "type", "label");
        assertCanonicalKeys(email, "value", "type", "label");
        assertCanonicalKeys(event, "value", "type");
        assertCanonicalKeys(im, "value", "type", "label");
    }

    @Test
    public void remainingPreferenceModels_readLegacyObfuscatedJson_andWriteCanonicalJson() {
        AlarmSound alarmSound = gson.fromJson("{\"a\":5,\"b\":\"Tone\",\"c\":\"content://tone\"}", AlarmSound.class);
        PhoneNumber phoneNumber = gson.fromJson(
                "{\"a\":\"0909\",\"b\":2,\"c\":\"Mobile\",\"d\":\"0909\",\"e\":true}",
                PhoneNumber.class
        );

        assertEquals("Tone", alarmSound.getTitle());
        assertEquals("0909", phoneNumber.getValue());
        assertCanonicalKeys(alarmSound, "id", "title", "uri");
        assertCanonicalKeys(phoneNumber, "value", "type", "label", "normalizedNumber", "isPrimary");
    }

    @Test
    public void hawkMigrationModels_writeStableJsonKeys() {
        AdsConfig adsConfig = gson.fromJson(
                "{\"ads_enable\":true,\"banner_enable\":true,\"inter_enable\":false,\"open_ads_enable\":true}",
                AdsConfig.class
        );
        Background background = new Background(
                42L,
                1,
                "thumb.webp",
                "video.mp4",
                false,
                "Theme",
                "2026-08-10",
                2
        );

        assertTrue(adsConfig.getAds_enable());
        assertCanonicalKeys(
                adsConfig,
                "ads_enable",
                "banner_enable",
                "inter_enable",
                "open_ads_enable"
        );
        assertCanonicalKeys(
                background,
                "id",
                "type",
                "path-thumb",
                "path-file",
                "delete",
                "name",
                "time_update",
                "position"
        );
    }

    private void assertCanonicalKeys(Object value, String... expectedKeys) {
        JsonObject json = JsonParser.parseString(gson.toJson(value)).getAsJsonObject();
        for (String key : expectedKeys) {
            assertTrue("Missing canonical JSON key: " + key + " in " + json, json.has(key));
        }
        assertFalse("New JSON must not use legacy key a: " + json, json.has("a"));
        assertFalse("New JSON must not use legacy key b: " + json, json.has("b"));
        assertFalse("New JSON must not use legacy key c: " + json, json.has("c"));
        assertFalse("New JSON must not use legacy key d: " + json, json.has("d"));
        assertFalse("New JSON must not use legacy key e: " + json, json.has("e"));
    }
}
