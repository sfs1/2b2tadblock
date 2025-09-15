package com.sfsarfe.adblock.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.CommandSource.suggestMatching;

public class CommandConfig {
    private static final String MESSAGE_PREFIX = "§c[AdBlock]§f ";
    private static final ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    public static void registerCommands() {
        MinecraftClient client = MinecraftClient.getInstance();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
        {

            dispatcher.register(ClientCommandManager.literal("adblock")
                    .then(ClientCommandManager.literal("help")
                            .executes(CommandConfig::helpCommand))
                    .then(ClientCommandManager.literal("about")
                            .executes(CommandConfig::aboutCommand))
                    .then(ClientCommandManager.literal("config")
                            .executes(CommandConfig::configCommand)
                            .then(ClientCommandManager.argument("Config Name", StringArgumentType.string())
                                    .suggests(CommandConfig::suggestOptions)
                                    .executes(CommandConfig::getConfigCommand)
                                    .then(ClientCommandManager.argument("Value", StringArgumentType.greedyString())
                                            .suggests(CommandConfig::suggestValue)
                                            .executes(CommandConfig::setConfigCommand)
                                    )
                            )
                    )
                    .then(ClientCommandManager.literal("reload")
                            .executes(CommandConfig::reloadCommand)
                    )

                    .then(ClientCommandManager.literal("edit")
                            .executes(CommandConfig::editBlocklistsCommand)
                    )
            );
        });



    }

    private static int reloadCommand(CommandContext<FabricClientCommandSource> context) {
        MessageFiltering.loadBlocklists();

        MinecraftClient client = MinecraftClient.getInstance();

        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Reloading blocklists"));
        return Command.SINGLE_SUCCESS;
    }


    private static CompletableFuture<Suggestions> suggestValue(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder)
    {
        List<String> values = new ArrayList<>();

        String configName = context.getArgument("Config Name", String.class);

        Field field;
        try {
            field = ModConfig.class.getField(configName);
        } catch (NoSuchFieldException e) {
            return suggestMatching(values, builder);
        }

        Type type = field.getType();
        switch (type.getTypeName()) {
            case "boolean":
                values.add("true");
                values.add("false");
                break;
            case "java.lang.String":
            case "int":
            case "double":
                break; // anything other than boolean is just stupid
            default:
                values.add("(unknown-type)");
                break;

        }
        return suggestMatching(values, builder);
    }
    private static CompletableFuture<Suggestions> suggestOptions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder)
    {
        Field[] fields = ModConfig.class.getFields();
        List<String> configNames = new ArrayList<>();

        for (Field field : fields) {
            configNames.add(field.getName());
        }

        return suggestMatching(configNames, builder);
    }

    public static int helpCommand(CommandContext<FabricClientCommandSource> context)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Adblock Commands"));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "/adblock help - Shows this help"));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "/adblock about - Shows information about this mod"));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "/adblock config - Show all available config options"));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "/adblock config (name) - Show the value and the type of a config option"));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "/adblock config (name) (value) - Set the value of a config option."));
        return Command.SINGLE_SUCCESS;
    }
    private static int aboutCommand(CommandContext<FabricClientCommandSource> context)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        String version = FabricLoader.INSTANCE.getModContainer("adblock").get().getMetadata().getVersion().getFriendlyString();

        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Adblock v" + version));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "This mod is licensed under the GNU GPLv3 license"));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "You can see the source code at: https://github.com/sfs1/2b2tadblock"));
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "- sfsarfe :)"));
        return Command.SINGLE_SUCCESS;
    }
    private static int configCommand(CommandContext<FabricClientCommandSource> context)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "not implemented"));
        return Command.SINGLE_SUCCESS;
    }
    private static int getConfigCommand(CommandContext<FabricClientCommandSource> context)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "not implemented"));
        return Command.SINGLE_SUCCESS;
    }

    private static int setConfigCommand(CommandContext<FabricClientCommandSource> context)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "not implemented"));
        return Command.SINGLE_SUCCESS;
    }


    private static int editBlocklistsCommand(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (AdblockClient.editBlocklist()) {
            if (config.verboseMode)
                client.player.sendMessage(Text.of(MESSAGE_PREFIX + "OK."));
        } else {
            client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Failed to open blocklists file. Set the EDITOR environment variable to a text editor"));
        }
        return Command.SINGLE_SUCCESS;
    }



}
