package com.sfsarfe.adblock.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "adblock")
public class ModConfig implements ConfigData
{
    public String customRegex = "No regex configured!";
    public boolean enableAdblock = false;
    public boolean autoupdateRegex = false;
    public String webRegex = "";
}
