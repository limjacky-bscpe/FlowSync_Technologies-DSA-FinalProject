package priorq;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

final class Account {
    final Path directory;
    final String email;
    private Properties profile;

    private Account(Path directory, String email, Properties profile) {
        this.directory = directory;
        this.email = email;
        this.profile = profile;
    }

    String name() { return profile.getProperty("name"); }
    boolean dark() { return Boolean.parseBoolean(profile.getProperty("dark")); }

    private static String normalized(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static Path folder(String email) throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(email.getBytes(StandardCharsets.UTF_8));
        return Main.HOME.resolve("accounts").resolve(HexFormat.of().formatHex(digest));
    }

    private static byte[] hash(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 600_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    static Account register(String name, String email, char[] password) throws Exception {
        email = normalized(email);
        if (name.isBlank()) throw new IllegalArgumentException("Enter your display name.");
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Enter a valid email address.");
        if (password.length < 8)
            throw new IllegalArgumentException("Use at least 8 characters for your password.");

        Path dir = folder(email);
        Files.createDirectories(dir);
        if (Files.exists(dir.resolve("profile.xml")))
            throw new IllegalArgumentException("That account already exists. Choose Log in.");

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        Properties p = new Properties();
        p.setProperty("salt", Base64.getEncoder().encodeToString(salt));
        p.setProperty("hash", Base64.getEncoder().encodeToString(hash(password, salt)));
        Account account = new Account(dir, email, p);
        account.saveProfile(name, false);
        return account;
    }

    static Account signIn(String email, char[] password) throws Exception {
        email = normalized(email);
        Path dir = folder(email);
        Path file = dir.resolve("profile.xml");
        String error = "Email or password is incorrect.";
        if (!Files.exists(file)) throw new IllegalArgumentException(error);

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) { p.loadFromXML(in); }
        byte[] salt = Base64.getDecoder().decode(p.getProperty("salt"));
        byte[] saved = Base64.getDecoder().decode(p.getProperty("hash"));
        if (!MessageDigest.isEqual(saved, hash(password, salt)))
            throw new IllegalArgumentException(error);
        return new Account(dir, email, p);
    }

    void saveProfile(String name, boolean dark) throws IOException {
        if (name.isBlank()) throw new IllegalArgumentException("Enter your display name.");
        Properties updated = new Properties();
        updated.putAll(profile);
        updated.setProperty("name", name.strip());
        updated.setProperty("dark", String.valueOf(dark));
        TaskStore.writeXml(directory.resolve("profile.xml"), updated);
        profile = updated;
    }
}