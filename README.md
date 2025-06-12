# Minecraft Chat Adblocker and AntiSpam

## Requirements:
- Minecraft >= 1.21
- Fabric >= 0.16.14
- [Cloth Config](https://modrinth.com/mod/cloth-config/versions?l=fabric) >= 15.0.140
- [Mod Menu](https://modrinth.com/mod/modmenu/versions?c=release&l=fabric) >=11.0.1

## Build Instructions
1. READ THE FUCKING SOURCE CODE!!!! (or at least get chatgpt to read it)
2. Unzip the source, open a command prompt/terminal in it
3. Type `gradlew.bat build` on windows or `./gradlew build` on linux/mac
4. Jar should be in `build/libs/2b2t-adblock-1.0.jar`



## Hacking
- `ChatHudMixin.java` just calls into MessageFiltering for adblock and antispam
- `MessageFiltering.java` has the code for adblocking and antispam
- `ModConfig.java` has the configuration fields (variables?), (de)serialisation is done by cloth config
- `ModMenuIntegration.java` has the config screen, heavy lifting is done by mod menu
- `AdblockClient.java` initialises the config and fetches the regex from github if enabled
Have fun hacking away, i guess

## TODO:
- [ ] Spam filtering
- [ ] Shadow cloth config and mod menu in output jar?????
- [ ] filter chat messages at packet level
- [ ] different adblock regex for other servers