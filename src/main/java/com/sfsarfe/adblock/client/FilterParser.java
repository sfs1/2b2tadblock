package com.sfsarfe.adblock.client;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/*
Syntax:

# comment
!IF (condition)
!ENDIF
!SERVER (ip, e.g. *.2b2t.org)
blockthis\([0-9]*\)

 */
public class FilterParser {
    public static List<Filter> parseList(String list) throws ParseException {
        String[] lines = list.split("\n");
        List<Filter> out = new ArrayList<>();
        boolean process = true;

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
                    case "!IF":
                    {
                        // IF stuff goes here
                        boolean condition = false;
                        process = !condition;
                        continue;
                    }
                    case "!ENDIF":
                    {
                        if (!process)
                            process = true;
                        else
                            throw new ParseException("ENDIF without an IF", i);
                        continue;
                    }
                    case "!SERVER":
                    {
                        if (parts.length != 2)
                            throw new ParseException("SERVER without following server ip", i);
                        server = parts[1];
                        continue;
                    }
                }
            }

            if (process)
            {
                try
                {
                    "test value".matches(line);
                    out.add(new Filter(line, server));
                }
                catch (Exception e)
                {
                    throw new ParseException("Invalid regex `" + line + "`", i);
                }
            }
            else
                continue;
        }

        return out;
    }

    // idk ill prob rename or redo this
    public record Filter(String blockRegex, String server) {}
}
