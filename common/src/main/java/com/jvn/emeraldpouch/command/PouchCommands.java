package com.jvn.emeraldpouch.command;

import com.jvn.emeraldpouch.config.EmeraldPouchServerConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class PouchCommands {
    private PouchCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("emeraldpouch")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("keepOnDeath")
                        .executes(context -> report(context.getSource()))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    try {
                                        EmeraldPouchServerConfig.setKeepOnDeath(enabled);
                                    } catch (IOException exception) {
                                        LogUtils.getLogger().error("Could not save emerald pouch server config", exception);
                                        context.getSource().sendFailure(Component.translatable("commands.emeraldpouch.save_failed"));
                                        return 0;
                                    }
                                    return report(context.getSource());
                                }))));
    }

    private static int report(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("commands.emeraldpouch.keep_on_death",
                EmeraldPouchServerConfig.keepOnDeath()), true);
        return 1;
    }
}
