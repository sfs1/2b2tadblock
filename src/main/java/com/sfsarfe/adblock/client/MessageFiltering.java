package com.sfsarfe.adblock.client;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFiltering {

    private static final Logger LOGGER = LoggerFactory.getLogger("adblock");
    public static boolean containsAd(Text message)
    {
        try
        {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

            // do the adblocking shit
            if (!config.enableAdblock)
                return false;

            if (!config.customRegex.isEmpty())
            {
                Pattern customRegex = Pattern.compile(config.customRegex);
                Matcher customMatch = customRegex.matcher(message.getString());

                if (customMatch.find())
                    return true;
            }

            if (config.autoupdateRegex && !config.webRegex.isEmpty())
            {
                Pattern regex = Pattern.compile(config.webRegex);
                Matcher match = regex.matcher(message.getString());

                if (match.find())
                    return true;
            }

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private static final List<TimedMessage> messageHistory = new ArrayList<TimedMessage>();

    public static boolean isSpam(Text message)
    {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        long now = System.currentTimeMillis();

        messageHistory.removeIf(msg -> (now - msg.timestamp) > config.spamFilterFrequency * 1000L * 60L);

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
