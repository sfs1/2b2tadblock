# Minecraft Chat Adblocker

## Requirements:
- Minecraft >= 1.21
- Fabric >= 0.16.14
- [Cloth Config](https://modrinth.com/mod/cloth-config/versions?l=fabric) >= 15.0.140
- [Mod Menu](https://modrinth.com/mod/modmenu/versions?c=release&l=fabric) >=11.0.1

## How it works
If a chat message passes the regex (test to check if two strings match pretty much), the message is not shown on the client.

## Hacking
- `ChatHudMixin.java` contains the code for the regex matching and chat filtering
- `ModConfig.java` houses the configuration fields (variables?), (de)serialisation is done by cloth config
- `ModMenuIntegration.java` has the config screen, heavy lifting is done by mod menu
- `AdblockClient.java` initialises the config and fetches the regex from github if enabled
Have fun hacking away, i guess
