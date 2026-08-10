package com.colorcall.callerscreen.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.colorcall.callerscreen.model.AdsConfig;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

/**
 * Stores the remote ads configuration without persisting an obfuscated app class name.
 *
 * <p>Older releases stored {@link AdsConfig} directly through Hawk. Hawk includes the runtime
 * class name in its payload, so an R8 name such as {@code z5} can resolve to an unrelated class
 * after an app update. The V2 value is JSON stored as a String, whose class name is stable.</p>
 */
public final class AdsConfigStorage {
    private static final String JSON_KEY = "ADS_CONFIG_JSON_V2";
    private static final Gson GSON = new Gson();

    private AdsConfigStorage() {
    }

    @Nullable
    public static synchronized AdsConfig get() {
        if (!Hawk.isBuilt()) {
            return null;
        }

        Object jsonValue = readSafely(JSON_KEY);
        if (jsonValue instanceof String) {
            AdsConfig config = parse((String) jsonValue);
            if (config != null) {
                deleteSafely(ConstantAds.ADS_CONFIG);
                return config;
            }
            deleteSafely(JSON_KEY);
        } else if (jsonValue != null) {
            deleteSafely(JSON_KEY);
        }

        return migrateLegacyValue();
    }

    public static synchronized void put(@NonNull AdsConfig config) {
        if (!Hawk.isBuilt() || config == null) {
            return;
        }

        String json;
        try {
            json = GSON.toJson(config);
        } catch (RuntimeException exception) {
            return;
        }

        if (writeSafely(JSON_KEY, json)) {
            deleteSafely(ConstantAds.ADS_CONFIG);
        }
    }

    @Nullable
    private static AdsConfig migrateLegacyValue() {
        Object legacyValue = readSafely(ConstantAds.ADS_CONFIG);
        AdsConfig config = null;

        if (legacyValue instanceof AdsConfig) {
            config = (AdsConfig) legacyValue;
        } else if (legacyValue instanceof String) {
            config = parse((String) legacyValue);
        }

        if (config != null) {
            put(config);
        } else if (legacyValue != null) {
            // An old R8 name resolved to a different class. Never expose it to a typed caller.
            deleteSafely(ConstantAds.ADS_CONFIG);
        }

        return config;
    }

    @Nullable
    private static Object readSafely(String key) {
        try {
            // Force Object as the generic type so the call site cannot insert an unsafe cast.
            boolean valueExists = Hawk.contains(key);
            Object value = Hawk.<Object>get(key);
            if (valueExists && value == null) {
                // Hawk returns null when an old payload can no longer be deserialized.
                deleteSafely(key);
            }
            return value;
        } catch (RuntimeException exception) {
            deleteSafely(key);
            return null;
        }
    }

    private static boolean writeSafely(String key, String value) {
        try {
            return Hawk.put(key, value);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static void deleteSafely(String key) {
        try {
            Hawk.delete(key);
        } catch (RuntimeException ignored) {
            // A disposable remote config must never prevent the app from starting.
        }
    }

    @Nullable
    private static AdsConfig parse(String json) {
        try {
            return GSON.fromJson(json, AdsConfig.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
