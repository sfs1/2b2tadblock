package com.sfsarfe.adblock.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
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


    private static int editBlocklistsCommand(CommandContext<FabricClientCommandSource> context)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        if (AdblockClient.editBlocklist())
        {
            client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Failed to open blocklists file. Set the EDITOR environment variable to a text editor"));
        }
        else
        {
            client.player.sendMessage(Text.of(MESSAGE_PREFIX + "OK."));
        }
        return Command.SINGLE_SUCCESS;
    }
//    {
//        MinecraftClient client = MinecraftClient.getInstance();
//
//        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
//
//
//        String editor = System.getenv("EDITOR");
//        if (editor == null) {
////            // get the OS
////            String os = System.getProperty("os.name").toLowerCase();
////            if (os.contains("win"))
////                os = "windows";
////            else if (os.contains("nix") | os.contains("nux") | os.contains("mac"))
////                os = "unix";
////            else
////            {
////                client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Unable to find an editor. Set the EDITOR environment variable with a text editor (e.g. notepad) of your choice."));
////
////            }
//
//            for (String ed : ALL_EDITORS)
//            {
//                if (checkOnPath(ed))
//                {
//                    ProcessBuilder pb = new ProcessBuilder(ed, Paths.get(config.blockListPath).toAbsolutePath().toString()); // should probably work
//                    pb.redirectErrorStream(true);
//                    try {
//                        pb.start();
//                        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Launched blocklist file with " + ed));
//                        return Command.SINGLE_SUCCESS;
//
//                    } catch (IOException e) {
//                        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Error launching blocklist file with " + ed));
//                    }
//                }
//            }
//        }
//        else // editor != null
//        {
//            if (checkOnPath(editor))
//            {
//                ProcessBuilder pb = new ProcessBuilder(editor, Paths.get(config.blockListPath).toAbsolutePath().toString()); // should probably work
//                pb.redirectErrorStream(true);
//                try {
//                    pb.start();
//                    client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Launched blocklist file with " + editor));
//                    return Command.SINGLE_SUCCESS;
//
//                } catch (IOException e) {
//                    client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Error launching blocklist file with " + editor));
//                }
//            }
//        }
//
//        client.player.sendMessage(Text.of(MESSAGE_PREFIX + "Unable to find an editor on PATH. Set the EDITOR environment variable with a text editor (e.g. notepad) of your choice."));
//        return Command.SINGLE_SUCCESS;
//    }



}
