package com.sfsarfe.adblock.client;

import net.fabricmc.api.ClientModInitializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.*;


public class AdblockClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("adblock");

    @Override
    public void onInitializeClient() {
        // Initialise config
        InitialiseConfig();

        fetchBlocklist();





    }

    public void InitialiseConfig()
    {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        try {

            Path configDir = Paths.get("config/adblock");
            if (!Files.exists(configDir))
                Files.createDirectory(configDir);

            Path blocklistPath = Paths.get("config/adblock/blocklist.txt");
            if (!Files.exists(blocklistPath))
            {
                Files.createFile(blocklistPath);
                Files.writeString(blocklistPath, "# Adblock block list. Each line is a seperate block. See https://github.com/sfs1/2b2tadblock for more details.");
            }
            Path webBlocklistPath = Paths.get("config/adblock/remoteblocklist.txt");
            if (!Files.exists(webBlocklistPath))
            {
                Files.createFile(webBlocklistPath);
                Files.writeString(webBlocklistPath, "# Adblock block list. This file is automagically updated from the github repo, if auto update is enabled in config.");
            }


            CommandConfig.registerCommands();
            MessageFiltering.loadBlocklists();

            // watch config dir
            CompletableFuture.runAsync(() -> {
                // watch config dir for file changes

                WatchService watchService = null;
                try {
                    watchService = FileSystems.getDefault().newWatchService();
                    configDir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                } catch (IOException e) {
                    LOGGER.error("Unable to watch blocklist directory for changes.");
                    e.printStackTrace(System.err);
                }

                while (true) {
                    try {
                        if (watchService == null)
                            continue;
                        WatchKey key = watchService.take();
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();

                            if (kind == ENTRY_MODIFY)
                            {
                                LOGGER.info("Reloading blocklists...");
                                MessageFiltering.loadBlocklists();
                                break;
                            }
                        }


                    } catch(Exception e)
                    {
                        LOGGER.error("Error watching blocklist directory for changes. Any blocklist changes will not be registered.");
                        e.printStackTrace(System.err);
                        break;
                    }

                }
            });

        } catch(Exception e)
        {
            LOGGER.error("Error creating blocklist files");
            e.printStackTrace();
        }



    }

    public static void fetchBlocklist()
    {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.autoupdateRegex)
            return;

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(config.blocklistUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                String blocklist = content.toString().strip();
                Path webBlocklistPath = Paths.get("config/adblock/remoteblocklist.txt");
                Files.writeString(webBlocklistPath, blocklist);


            } catch (Exception e) {
                LOGGER.error("Failed to fetch blocklist from github: " + e.getMessage());
            }
            MessageFiltering.loadBlocklists();
        });
    }
}
