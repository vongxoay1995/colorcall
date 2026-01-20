package com.orhanobut.hawk;

import android.content.Context;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import android.util.Base64;
public class JetpackEncryption implements Encryption {
    private final EncryptedSharedPreferences sharedPreferences;

    public JetpackEncryption(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            this.sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "secure_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EncryptedSharedPreferences", e);
        }
    }

    @Override
    public boolean init() {
        return true; // Jetpack Security luôn sẵn sàng nếu cấu hình đúng
    }

    @Override
    public String encrypt(String key, String plainText) {
        try {
            byte[] bytes = plainText.getBytes();
            String encrypted = Base64.encodeToString(bytes, Base64.NO_WRAP);
            sharedPreferences.edit().putString(key, encrypted).apply();
            return encrypted;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String decrypt(String key, String cipherText) {
        try {
            String encrypted = sharedPreferences.getString(key, null);
            if (encrypted == null) return null;
            byte[] bytes = Base64.decode(encrypted, Base64.NO_WRAP);
            return new String(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}