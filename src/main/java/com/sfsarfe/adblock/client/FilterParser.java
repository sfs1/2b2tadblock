package com.sfsarfe.adblock.client;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/*
Syntax:

# comment
!SERVER 2b2t.org (runs for all 2b2t.org servers)
blockthis\([0-9]*\)
!SERVER minehut.com (runs for all minehut.com servers)
minehut-block-[0-9]{1,4}


TODO: explicitly use *.server.com and maybe CIDR ranges for ips?

 */
public class FilterParser {
    public static List<Filter> parseList(String list) throws ParseException {
        list = list.replace("\r\n", "\n"); // fuck you windows
        String[] lines = list.split("\n");
        List<Filter> out = new ArrayList<>();
        String server = "";
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];

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
                        server = parts.length == 2 ? parts[1] : "";
                        continue;
                    }
                }
                throw new ParseException("Invalid directive: `" + line + "'", i);
            }

            // don't add blank regexes. that will end... badly
            if (line.isBlank())
                continue;

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
