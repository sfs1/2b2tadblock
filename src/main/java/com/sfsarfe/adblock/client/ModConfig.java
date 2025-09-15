package com.sfsarfe.adblock.client;

import jdk.jfr.Description;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;


@Config(name = "adblock")
public class ModConfig implements ConfigData
{
    @Description("Whether to enable ad blocking")
    public boolean enableAdblock = false;
    @Description("Whether to update block list from remote")
    public boolean autoupdateRegex = false;
    @Description("URL for remote block list")
    public String blocklistUrl = "https://raw.githubusercontent.com/sfs1/2b2tadblock/refs/heads/main/blocklist.txt";
    @Description("Whether to enable spam filtering")
    public boolean enableSpamFilter = false;
//    public boolean spamFilterAutoIgnore = true;
    @Description("Command used for right click ignore")
    public String spamFilterIgnoreCommand = "/ignorehard";
    @Description("Number of similar messages for it to be considered spam")
    public int spamFilterMessageCount = 3;
    @Description("How long chat messages are considered for spam")
    public int spamFilterFrequency = 15;
    @Description("Minimum length of spam messages")

    public int spamFilterMinLength = 10;
    @Description("How similar a message has to be to others, for it to be considered spam")
    public double spamFilterSimilarityThreshold = 0.8;
    @Description("Ignore by right clicking someone's name")
    public boolean rightClickIgnore = false;
    @Description("Check whether there is a new version on github.")
    public boolean checkUpdates = false;

    public boolean verboseMode = false;
    public String blockListPath = "config/adblock/blocklist.txt";
    public String remoteBlocklistPath = "";
}
