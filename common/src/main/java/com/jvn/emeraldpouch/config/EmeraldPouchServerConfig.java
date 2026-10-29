package com.jvn.emeraldpouch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.architectury.platform.Platform;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EmeraldPouchServerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = Platform.getConfigFolder().resolve("emeraldpouch-server.json");
    private static Settings settings = new Settings();

    private EmeraldPouchServerConfig() {
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            try {
                setKeepOnDeath(false);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not create " + PATH, exception);
            }
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            Settings loaded = GSON.fromJson(reader, Settings.class);
            if (loaded == null) {
                throw new JsonParseException("Expected a config object");
            }
            settings = loaded;
        } catch (IOException | JsonParseException exception) {
            throw new IllegalStateException("Could not read " + PATH, exception);
        }
    }

    public static boolean keepOnDeath() {
        return settings.keepOnDeath;
    }

    public static void setKeepOnDeath(boolean enabled) throws IOException {
        Settings updated = new Settings();
        updated.keepOnDeath = enabled;
        Files.createDirectories(PATH.getParent());
        Files.writeString(PATH, GSON.toJson(updated) + System.lineSeparator());
        settings = updated;
    }

    private static final class Settings {
        private boolean keepOnDeath;
    }
}
