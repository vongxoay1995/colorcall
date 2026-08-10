package com.colorcall.callerscreen.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.colorcall.callerscreen.database.Background;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

/** Stores the selected background as stable JSON and migrates the legacy Hawk object safely. */
public final class BackgroundSelectionStorage {
    private static final String JSON_KEY = "BACKGROUND_SELECT_JSON_V2";
    private static final String LEGACY_KEY = "BACKGROUND_SELECT";
    private static final Gson GSON = new Gson();

    private BackgroundSelectionStorage() {
    }

    @NonNull
    public static synchronized Background get(@NonNull Background defaultValue) {
        if (!Hawk.isBuilt()) {
            return defaultValue;
        }

        Object jsonValue = readSafely(JSON_KEY);
        if (jsonValue instanceof String) {
            Background background = parse((String) jsonValue);
            if (isValid(background)) {
                deleteSafely(LEGACY_KEY);
                return background;
            }
            deleteSafely(JSON_KEY);
        } else if (jsonValue != null) {
            deleteSafely(JSON_KEY);
        }

        Object legacyValue = readSafely(LEGACY_KEY);
        if (legacyValue instanceof Background && isValid((Background) legacyValue)) {
            Background background = (Background) legacyValue;
            put(background);
            return background;
        }

        if (legacyValue != null) {
            deleteSafely(LEGACY_KEY);
        }
        return defaultValue;
    }

    public static synchronized void put(@NonNull Background background) {
        if (!Hawk.isBuilt() || !isValid(background)) {
            return;
        }

        try {
            String json = GSON.toJson(background);
            if (Hawk.put(JSON_KEY, json)) {
                deleteSafely(LEGACY_KEY);
            }
        } catch (RuntimeException ignored) {
            // A selected background is optional and must never crash the app.
        }
    }

    @Nullable
    private static Object readSafely(String key) {
        try {
            boolean valueExists = Hawk.contains(key);
            Object value = Hawk.<Object>get(key);
            if (valueExists && value == null) {
                deleteSafely(key);
            }
            return value;
        } catch (RuntimeException exception) {
            deleteSafely(key);
            return null;
        }
    }

    @Nullable
    private static Background parse(String json) {
        try {
            return GSON.fromJson(json, Background.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static boolean isValid(@Nullable Background background) {
        return background != null
                && background.getPathThumb() != null
                && background.getPathItem() != null
                && background.getName() != null
                && background.getTimeUpdate() != null;
    }

    private static void deleteSafely(String key) {
        try {
            Hawk.delete(key);
        } catch (RuntimeException ignored) {
            // Ignore disposable cache cleanup failures.
        }
    }
}
