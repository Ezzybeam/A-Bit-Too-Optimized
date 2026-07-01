package com.abto.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads and saves AbtoConfig as JSON. Never throws on a missing or corrupt file:
 * it falls back to defaults and backs up a corrupt file so the user does not lose
 * a working game over a bad edit.
 */
public final class ConfigStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigStore() {
    }

    public static AbtoConfig load(Path file) {
        if (!Files.exists(file)) {
            return AbtoConfig.defaults();
        }
        try {
            String json = Files.readString(file);
            AbtoConfig config = GSON.fromJson(json, AbtoConfig.class);
            if (config == null) {
                return AbtoConfig.defaults();
            }
            config.fillMissingWithDefaults();
            return config;
        } catch (JsonSyntaxException | IOException e) {
            backupCorruptFile(file);
            return AbtoConfig.defaults();
        }
    }

    public static void save(Path file, AbtoConfig config) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, GSON.toJson(config));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save ABTO config", e);
        }
    }

    private static void backupCorruptFile(Path file) {
        try {
            Path backup = file.resolveSibling(file.getFileName().toString() + ".bak");
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Best effort: if we cannot back up, we still continue with defaults.
        }
    }
}
