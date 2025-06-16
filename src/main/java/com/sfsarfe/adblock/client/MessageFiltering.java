package com.sfsarfe.adblock.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageFiltering {

    private static final Logger LOGGER = LoggerFactory.getLogger("adblock");

    private static Map<String, String> filters;

    public static boolean containsAd(Text message)
    {
        try
        {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

            // do the adblocking shit
            if (!config.enableAdblock)
                return false;
            if (filters == null)
                return false;

            String address = "";
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
            if (networkHandler != null) {
                ServerInfo serverInfo = networkHandler.getServerInfo();
                if (serverInfo != null)
                    address = serverInfo.address;
            }

            // find the filters that apply to this server
            List<String> serverFilters = new ArrayList<>();
            for (Map.Entry<String, String> entry : filters.entrySet())
            {
                if (address.matches(entry.getKey()))
                    serverFilters.add(entry.getValue());
            }
            System.out.println("Filters: " + serverFilters);
            if (serverFilters.isEmpty())
                return false;

            String msg = message.getString().strip();
            // go thru each filter, checking if it applies
            for (String filterRegex : serverFilters)
            {
                Pattern pattern = Pattern.compile(filterRegex);
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find())
                    return true;
            }
            return false;

        } catch(Exception e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public static void loadBlocklists(String remoteBlocklist, String localBlocklist)
    {

        List<FilterParser.Filter> newFilters = new ArrayList<>();
        try {
            newFilters.addAll(FilterParser.parseList(remoteBlocklist));
            newFilters.addAll(FilterParser.parseList(localBlocklist));
        } catch (ParseException e) {
            LOGGER.error("Error parsing blocklist. Adblocking has been disabled, you must manually re-enable it in the config.");
            e.printStackTrace(System.err);
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            config.enableAdblock = false;
            return;
        }

        // combine each regex, sort them by server
        Map<String, String> combinedFilters = newFilters.stream()
                .collect(Collectors.groupingBy(
                        FilterParser.Filter::server,
                        Collectors.mapping(
                                f -> "(" + f.blockRegex() + ")",
                                Collectors.joining("|")
                        )
                ));

        filters = combinedFilters;

    }

    public static void loadBlocklists()
    {

        Path webBlocklistPath = Paths.get("config/adblock/remoteblocklist.txt");
        Path blocklistPath = Paths.get("config/adblock/blocklist.txt");

        try {
            String remoteBlocklist = Files.readString(webBlocklistPath);
            String localBlocklist = Files.readString(blocklistPath);
            loadBlocklists(remoteBlocklist, localBlocklist);
        } catch (IOException e) {
            LOGGER.error("Could not read blocklist files. Ad blocking will not work for the time being.");
            e.printStackTrace(System.err);
        }

    }

    private static final List<TimedMessage> messageHistory = new ArrayList<TimedMessage>();

    // TODO: improve spam detection algorithm

    public static boolean isSpam(Text message)
    {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        long now = System.currentTimeMillis();

        messageHistory.removeIf(msg -> (now - msg.timestamp) > config.spamFilterFrequency * 1000L * 60L);


        if (message.getString().length() < config.spamFilterMinLength)
            return false;

        messageHistory.add(new TimedMessage(message.getString(), now));


        if (messageHistory.size() < config.spamFilterMessageCount)
            return false;


        int similar = 0;

        for (TimedMessage msg : messageHistory)
        {
            double sim = cosineSimilarity(message.getString(), msg.message);
            if (sim > config.spamFilterSimilarityThreshold)
                similar++;
        }


        if (similar < config.spamFilterMessageCount)
            return false;
        boolean ret = similar >= (config.spamFilterMessageCount - 1);
//        LOGGER.info(String.format("similar: %d, is spam?: %b", similar, ret)); // debug shitz

        return ret;
    }

    // chat gipitee
    private static double cosineSimilarity(String a, String b) {
        Map<String, Integer> freqA = wordFreq(a);
        Map<String, Integer> freqB = wordFreq(b);

        Set<String> allWords = new HashSet<>();
        allWords.addAll(freqA.keySet());
        allWords.addAll(freqB.keySet());

        int dot = 0, magA = 0, magB = 0;
        for (String word : allWords) {
            int x = freqA.getOrDefault(word, 0);
            int y = freqB.getOrDefault(word, 0);
            dot += x * y;
            magA += x * x;
            magB += y * y;
        }

        return (magA == 0 || magB == 0) ? 0 : dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    private static Map<String, Integer> wordFreq(String msg) {
        Map<String, Integer> map = new HashMap<>();
        for (String word : msg.toLowerCase().split("\\s+")) {
            map.put(word, map.getOrDefault(word, 0) + 1);
        }
        return map;
    }

    private record TimedMessage(String message, long timestamp) {}
}
