package org.cauecalil.personalfinance.infrastructure.keystore;

import lombok.extern.slf4j.Slf4j;
import org.cauecalil.personalfinance.infrastructure.exception.CredentialStoreException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Optional;

@Component
@Slf4j
public class KeyStoreCredentialStore {
    private static final String KEYSTORE_PATH = "./credentials.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final char[] KEYSTORE_PASSWORD = "pf-local-store".toCharArray();
    private static final String CLIENT_ID_ALIAS = "pluggy-client-id";
    private static final String CLIENT_SECRET_ALIAS = "pluggy-client-secret";

    public void save(String clientId, String clientSecret) {
        try {
            KeyStore keyStore = loadOrCreate();

            keyStore.setEntry(
                    CLIENT_ID_ALIAS,
                    new KeyStore.SecretKeyEntry(toSecretKey(clientId)),
                    new KeyStore.PasswordProtection(KEYSTORE_PASSWORD)
            );

            keyStore.setEntry(
                    CLIENT_SECRET_ALIAS,
                    new KeyStore.SecretKeyEntry(toSecretKey(clientSecret)),
                    new KeyStore.PasswordProtection(KEYSTORE_PASSWORD)
            );

            persistToFile(keyStore);

            log.debug("Credentials saved to KeyStore");
        } catch (Exception e) {
            throw new CredentialStoreException("Failed to save credentials", e);
        }
    }

    public Optional<String[]> load() {
        File file = new File(KEYSTORE_PATH);

        if (!file.exists()) {
            return Optional.empty();
        }

        try {
            KeyStore keyStore = loadOrCreate();

            KeyStore.ProtectionParameter protection = new KeyStore.PasswordProtection(KEYSTORE_PASSWORD);

            KeyStore.SecretKeyEntry clientIdEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(CLIENT_ID_ALIAS, protection);
            KeyStore.SecretKeyEntry clientSecretEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(CLIENT_SECRET_ALIAS, protection);

            if (clientIdEntry == null || clientSecretEntry == null) {
                return Optional.empty();
            }

            String clientId = new String(clientIdEntry.getSecretKey().getEncoded(), StandardCharsets.UTF_8);
            String clientSecret = new String(clientSecretEntry.getSecretKey().getEncoded(), StandardCharsets.UTF_8);

            return Optional.of(new String[]{clientId, clientSecret});
        } catch (Exception e) {
            throw new CredentialStoreException("Failed to load credentials", e);
        }
    }

    public void delete() {
        try {
            File file = new File(KEYSTORE_PATH);

            if (file.exists()) {
                Files.delete(Path.of(KEYSTORE_PATH));
                log.debug("Credentials deleted from KeyStore");
            }
        } catch (Exception e) {
            throw new CredentialStoreException("Failed to delete credentials", e);
        }
    }

    private KeyStore loadOrCreate() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        File file = new File(KEYSTORE_PATH);

        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                keyStore.load(is, KEYSTORE_PASSWORD);
            }
        } else {
            keyStore.load(null, KEYSTORE_PASSWORD);
        }

        return keyStore;
    }

    private void persistToFile(KeyStore keyStore) throws Exception {
        File file = new File(KEYSTORE_PATH);

        try (OutputStream os = new FileOutputStream(file)) {
            keyStore.store(os, KEYSTORE_PASSWORD);
        }
    }

    private SecretKey toSecretKey(String value) {
        return new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "AES");
    }
}