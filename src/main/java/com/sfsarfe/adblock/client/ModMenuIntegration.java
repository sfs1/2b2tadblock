package com.sfsarfe.adblock.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.autoconfig.ConfigHolder;

import java.util.Optional;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // auto generated config screen:
        // return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
        return ModMenuIntegration::create;
    }

    public static Screen create(Screen parent)
    {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ConfigHolder<ModConfig> holder = AutoConfig.getConfigHolder(ModConfig.class);

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("Adblock Configuration"))
                .setSavingRunnable(holder::save);

        ConfigCategory adblockCategory = builder.getOrCreateCategory(Text.of("Adblock"));
        ConfigEntryBuilder adblockEntryBuilder = builder.entryBuilder();


        ConfigCategory spamCategory = builder.getOrCreateCategory(Text.of("Spam Filter"));
        ConfigEntryBuilder spamEntryBuilder = builder.entryBuilder();

        // Enable adblock option
        adblockCategory.addEntry(adblockEntryBuilder
                .startBooleanToggle(Text.of("Enable adblock"), config.enableAdblock)
                .setDefaultValue(true)
                .setSaveConsumer(newval -> config.enableAdblock = newval)
                .build()
        );

        // Enable regex autoupdate option
        adblockCategory.addEntry(adblockEntryBuilder
                .startBooleanToggle(Text.of("Autoupdate Block Regex"), config.autoupdateRegex)
                .setDefaultValue(true)
                .setSaveConsumer(newval -> config.autoupdateRegex = newval)
                .build()
        );


        // Custom regex
        adblockCategory.addEntry(adblockEntryBuilder
                .startTextField(Text.of("Custom Regex"), config.customRegex)
                .setDefaultValue("")
                .setSaveConsumer(newval -> config.customRegex = newval)
                .setErrorSupplier((value) -> {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        "test value".matches(value);
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.of(Text.of("Invalid regex!"));
                    }
                })
                .build()
        );



        /*
            Spam filter options
         */

        // Enable spam filter
        spamCategory.addEntry(spamEntryBuilder
                .startBooleanToggle(Text.of("Enable spam filter"), config.enableSpamFilter)
                .setDefaultValue(false)
                .setSaveConsumer(newval -> config.enableSpamFilter = newval)
                .build()
        );


        // Message count
        spamCategory.addEntry(spamEntryBuilder
                .startIntSlider(Text.of("Spam Message Count"), config.spamFilterMessageCount, 1, 60)
                .setDefaultValue(3)
                .setSaveConsumer(newval -> config.spamFilterMessageCount = newval)
                .build()
        );

        // Spam time
        spamCategory.addEntry(spamEntryBuilder
                .startIntField(Text.of("Spam Frequency (minutes)"), config.spamFilterFrequency)
                .setMin(1)
                .setMax(60)
                .setDefaultValue(20)
                .setSaveConsumer(newval -> config.spamFilterFrequency = newval)
                .build()
        );

        // Spam message min length
        spamCategory.addEntry(spamEntryBuilder
                .startIntField(Text.of("Spam Message Min Length"), config.spamFilterMinLength)
                .setMin(0)
                .setMax(50)
                .setDefaultValue(10)
                .setSaveConsumer(newval -> config.spamFilterMinLength = newval)
                .build()
        );

        // Spam ignore command
        spamCategory.addEntry(spamEntryBuilder
                .startStrField(Text.of("Ignore Command"), config.spamFilterIgnoreCommand)
                .setDefaultValue("/ignore")
                .setSaveConsumer(newval -> config.spamFilterIgnoreCommand = newval)
                .setErrorSupplier((value) -> {
                    if (!value.startsWith("/"))
                        return Optional.of(Text.of("Command must start with a /"));
                    return Optional.empty();
                })
                .build()
        );


        // Spam Message Similarity Threshold
        spamCategory.addEntry(spamEntryBuilder
                .startDoubleField(Text.of("Message Similarity Threshold (0-1)"), config.spamFilterSimilarityThreshold)
                .setMin(0)
                .setMax(1)
                .setDefaultValue(0.8)
                .setSaveConsumer(newval -> config.spamFilterSimilarityThreshold = newval)
                .build()
        );

        builder.setAfterInitConsumer(screen -> {
            AdblockClient.fetchRegex();
        });

        return builder.build();

    }
}