package com.sfsarfe.adblock.client;

import net.fabricmc.api.ClientModInitializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AdblockClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("adblock");

    @Override
    public void onInitializeClient() {
        // Initialise config
        InitialiseConfig();

        fetchRegex();

    }

    public void InitialiseConfig()
    {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    public static void fetchRegex()
    {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.autoupdateRegex)
            return;

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/sfs1/2b2tadblock/refs/heads/main/regex.txt");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                // Store result in static field or pass it to wherever you want
                config.webRegex = content.toString().strip();
            } catch (Exception e) {
                LOGGER.error("Failed to fetch regex from github: " + e.getMessage());
            }
        });
    }
}
