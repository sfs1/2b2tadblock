package com.sfsarfe.adblock.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "adblock")
public class ModConfig implements ConfigData
{
    public String customRegex = "";
    public boolean enableAdblock = false;
    public boolean autoupdateRegex = false;
    public String webRegex = "";
    public boolean enableSpamFilter = false;
    public boolean spamFilterAutoIgnore = true;
    public String spamFilterIgnoreCommand = "/ignorehard";
    public int spamFilterMessageCount = 3;
    public int spamFilterFrequency = 15;
    public int spamFilterMinLength = 10;
    public double spamFilterSimilarityThreshold = 0.8;
    public boolean rightClickIgnore = false;
}
