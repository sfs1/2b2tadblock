package com.sfsarfe.adblock.client;

import me.shedaniel.autoconfig.AutoConfig;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Syntax:

# comment
!SERVER (ip, e.g. *.2b2t.org)
blockthis\([0-9]*\)
alsoblockthis\(${myvar}\)

 */
public class FilterParser {
    public static List<Filter> parseList(String list) throws ParseException {
        String[] lines = list.split("\n");
        List<Filter> out = new ArrayList<>();
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            String server = "*";

            String[] parts;
            if (line.contains(" "))
                parts = line.split(" ");
            else
                parts = new String[]{line};

            if (line.startsWith("#"))
                continue;
            if (line.startsWith("!"))
            {
                switch (parts[0])
                {
                    case "!SERVER":
                    {
                        if (parts.length != 2)
                            throw new ParseException("SERVER without following server ip", i);
                        server = parts[1];
                        continue;
                    }
                }
            }

            try {
                "test value".matches(line);
                out.add(new Filter(line, server));
            } catch (Exception e) {
                throw new ParseException("Invalid regex `" + line + "`", i);
            }
        }

        return out;
    }


    // idk ill prob rename or redo this
    public record Filter(String blockRegex, String server) {}
}
