package com.sfsarfe.adblock.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import jdk.jfr.Description;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.CommandSource.suggestMatching;

public class CommandConfig {
    private static final String MESSAGE_PREFIX = "§c[AdBlock]§f ";
    private static final ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
        {

            dispatcher.register(ClientCommandManager.literal("adblock")
                    .then(ClientCommandManager.literal("help")
                            .executes(CommandConfig::helpCommand))
                    .then(ClientCommandManager.literal("about")
                            .executes(CommandConfig::aboutCommand))
                    .then(ClientCommandManager.literal("config")
                            .executes(CommandConfig::configCommand)
                            .then(ClientCommandManager.argument("Config Page", IntegerArgumentType.integer(1))
                                    .executes(CommandConfig::configCommand)
                            )
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

        client.inGameHud.getChatHud()
                .addMessage(Text.of(MESSAGE_PREFIX + "Reloading blocklists"));
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
        client.inGameHud.getChatHud()
                .addMessage(Text.of(MESSAGE_PREFIX + "Adblock Commands"));
        client.inGameHud.getChatHud()
                .addMessage(Text.of(MESSAGE_PREFIX + "/adblock help - Shows this help"));
        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "/adblock about - Shows information about this mod"));
        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "/adblock config - Show all available config options"));
        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "/adblock config (name) - Show the value and the type of a config option"));
        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "/adblock config (name) (value) - Set the value of a config option."));
        return Command.SINGLE_SUCCESS;
    }
    private static int aboutCommand(CommandContext<FabricClientCommandSource> context)
    {
        String version = FabricLoader.INSTANCE.getModContainer("adblock").get().getMetadata().getVersion().getFriendlyString();

        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "Adblock v" + version));
        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "This mod is licensed under the GNU GPLv3 license"));
        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "You can see the source code at: https://github.com/sfs1/2b2tadblock")); // if you fork, pls change this url
        client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "- sfsarfe :-)"));
        return Command.SINGLE_SUCCESS;
    }
    private static int configCommand(CommandContext<FabricClientCommandSource> context)
    {
        MinecraftClient client = MinecraftClient.getInstance();

        int configPage = 1;
        // if it doesn't exist, we get an exception
        try {
            configPage = context.getArgument("Config Page", Integer.class);
        }
        catch(Exception e) { System.out.println("could not access Config Page argument"); }


        Field[] fields = ModConfig.class.getFields();

        System.out.println("length: " + fields.length);
        System.out.println("from " + (configPage - 1) * 8 + " to " + configPage * 8);
        if ((configPage - 1) * 8 > fields.length)
        {
            System.out.println("nope, can't show this");
            return Command.SINGLE_SUCCESS;
        }

        System.out.println("cont. 1");
        client.inGameHud.getChatHud()
                .addMessage(Text.of("test" + configPage));
        client.inGameHud.getChatHud()
                .addMessage(Text.of("test"));

        client.inGameHud.getChatHud()
                .addMessage(Text.of(MESSAGE_PREFIX + "Adblock - Config Page " + configPage));
        for (int i = (configPage - 1) * 8; i < fields.length && i < configPage * 8; i++)
        {
            Field f = fields[i];
            System.out.println(i + " - " + f.getName());
            String value = "";
            try {
                value = f.getType() != String.class ? " (" + f.get(config) +")" : "";
            } catch (IllegalAccessException e) {
                System.out.println("IllegalAccessException while accessing type - value will not be shown");
            }
            System.out.println("got value");

            // BUG: after setting a config value, page command only shows the value thats been modified (on that page)
            // client.player.sendMessage is for some reason not working???
            // not even the first one outside the loop (right after cont. 1 print)
            client.inGameHud.getChatHud()
                    .addMessage(
                    Text.literal(MESSAGE_PREFIX)
                            .append(
                                    Text.literal(i + " - ")
                                            .setStyle(Style.EMPTY.withColor(0x808080)
                                            )
                            )
                            .append(Text.literal(fields[i].getName() + value)
                                    .setStyle(Style.EMPTY.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/adblock config " + f.getName())
                                            )
                                            .withHoverEvent(
                                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("/adblock config " + f.getName()))
                                            )
                                    )
                            )
            );

            System.out.println("shown config name");

        }
        Text pageSelectorText = Text.literal(MESSAGE_PREFIX).append(Text.literal("[<]")
                        .setStyle(Style.EMPTY.withClickEvent(
                                new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/adblock config " + Math.max(configPage - 1, 0))
                        ).withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Previous Page"))
                                )
                        )
                )
                .append(
                        Text.literal( " Page " + configPage + "/" + (int) Math.ceil(fields.length / 8f) + " ")
                                .setStyle(Style.EMPTY.withColor(0x808080))
                )
                .append(
                        Text.literal("[>]")
                                .setStyle(Style.EMPTY.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/adblock config " + (Math.min(configPage + 1, (int) Math.ceil(fields.length / 8f) ) ) )
                                ).withHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Next page"))
                                        )
                                )
                );
        client.inGameHud.getChatHud()
                .addMessage(pageSelectorText);

        return Command.SINGLE_SUCCESS;
    }


    private static int getConfigCommand(CommandContext<FabricClientCommandSource> context)
    {
        String configName = context.getArgument("Config Name", String.class);


        Field f;
        try {
            f = ModConfig.class.getField(configName);
        } catch (NoSuchFieldException e) {
            client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "No such config option exists!"));
            return Command.SINGLE_SUCCESS;
        }

        Description description = f.getAnnotation(Description.class);

        // cant have too many brackets, right? (im always right)
        client.inGameHud.getChatHud()
                .addMessage(Text.of(MESSAGE_PREFIX + configName + (description != null && !description.value().isBlank() ? ": " + description.value() : "")));
        try {
            client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "Value (default): " + f.get(config) + " (" + f.get(new ModConfig()) + ")"));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setConfigCommand(CommandContext<FabricClientCommandSource> context)
    {
        String configName = context.getArgument("Config Name", String.class);
        String value = context.getArgument("Value", String.class);

        Field f;
        Object oldValue;
        try {
            f = ModConfig.class.getField(configName);
        } catch (NoSuchFieldException e) {
            client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "No such config option exists!"));
            return Command.SINGLE_SUCCESS;
        }

        try {
            oldValue = f.get(config);
            f.set(config, convertToFieldType(f, value));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        client.inGameHud.getChatHud()
                .addMessage(Text.of(MESSAGE_PREFIX + "Set " + configName + " to " + value + " (old value: " + oldValue + " )"));
        return Command.SINGLE_SUCCESS;
    }


    private static int editBlocklistsCommand(CommandContext<FabricClientCommandSource> context) {
        if (AdblockClient.editBlocklist()) {
            if (config.verboseMode)
                client.inGameHud.getChatHud()
                        .addMessage(Text.of(MESSAGE_PREFIX + "OK."));
        } else {
            client.inGameHud.getChatHud()
                    .addMessage(Text.of(MESSAGE_PREFIX + "Failed to open blocklists file. Set the EDITOR environment variable to a text editor"));
        }
        return Command.SINGLE_SUCCESS;
    }


    public static Object convertToFieldType(Field field, String s) {
        Class<?> t = field.getType();

        if (t == String.class) return s;
        if (t == int.class || t == Integer.class) return Integer.parseInt(s);
        if (t == double.class || t == Double.class) return Double.parseDouble(s);
        if (t == boolean.class || t == Boolean.class) return Boolean.parseBoolean(s);
        throw new IllegalArgumentException("Unsupported field type: " + t.getName());
    }


}
