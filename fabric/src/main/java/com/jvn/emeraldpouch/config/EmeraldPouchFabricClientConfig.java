package com.jvn.emeraldpouch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.client.PouchClientSettings;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public final class EmeraldPouchFabricClientConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(EmeraldPouchMod.MOD_ID + "-client.json");

    private static ClientConfigData data = new ClientConfigData();

    private EmeraldPouchFabricClientConfig() {
    }

    public static void load() {
        ClientConfigData loaded = new ClientConfigData();
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ClientConfigData parsed = GSON.fromJson(reader, ClientConfigData.class);
                if (parsed != null) {
                    loaded = parsed;
                }
            } catch (IOException runtimeException) {
                LOGGER.warn("Failed to read Fabric client config from {}. Using defaults.", CONFIG_PATH, runtimeException);
            }
        }

        data = sanitize(loaded);
        save();
    }

    public static PouchClientSettings.HudPosition hudPosition() {
        return data.hudPosition;
    }

    public static PouchClientSettings.InventoryPosition inventoryPosition() {
        return data.inventoryPosition;
    }

    public static PouchClientSettings.MerchantPosition merchantPosition() {
        return data.merchantPosition;
    }

    public static boolean showBundleCountOverlay() {
        return data.showBundleCountOverlay;
    }

    public static PouchClientSettings.HudIconColor hudIconColor() {
        return data.hudIconColor;
    }

    public static void apply(
            PouchClientSettings.HudPosition hudPosition,
            PouchClientSettings.InventoryPosition inventoryPosition,
            PouchClientSettings.MerchantPosition merchantPosition,
            boolean showBundleCountOverlay,
            PouchClientSettings.HudIconColor hudIconColor
    ) {
        ClientConfigData updated = new ClientConfigData();
        updated.hudPosition = hudPosition;
        updated.inventoryPosition = inventoryPosition;
        updated.merchantPosition = merchantPosition;
        updated.showBundleCountOverlay = showBundleCountOverlay;
        updated.hudIconColor = hudIconColor;
        data = sanitize(updated);
        save();
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException runtimeException) {
            LOGGER.warn("Failed to write Fabric client config to {}.", CONFIG_PATH, runtimeException);
        }
    }

    private static ClientConfigData sanitize(ClientConfigData raw) {
        ClientConfigData sanitized = new ClientConfigData();
        if (raw.hudPosition != null) {
            sanitized.hudPosition = raw.hudPosition;
        }
        if (raw.inventoryPosition != null) {
            sanitized.inventoryPosition = raw.inventoryPosition;
        }
        if (raw.merchantPosition != null) {
            sanitized.merchantPosition = raw.merchantPosition;
        }
        sanitized.showBundleCountOverlay = raw.showBundleCountOverlay;
        if (raw.hudIconColor != null) {
            sanitized.hudIconColor = raw.hudIconColor;
        }
        return sanitized;
    }

    private static final class ClientConfigData {
        private PouchClientSettings.HudPosition hudPosition = PouchClientSettings.HudPosition.POSITION_1;
        private PouchClientSettings.InventoryPosition inventoryPosition = PouchClientSettings.InventoryPosition.POSITION_1;
        private PouchClientSettings.MerchantPosition merchantPosition = PouchClientSettings.MerchantPosition.POSITION_1;
        private boolean showBundleCountOverlay;
        private PouchClientSettings.HudIconColor hudIconColor = PouchClientSettings.HudIconColor.EMERALD;
    }
}
