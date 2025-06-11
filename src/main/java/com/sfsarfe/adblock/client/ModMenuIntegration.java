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

        ConfigCategory generalCategory = builder.getOrCreateCategory(Text.of("General"));
        ConfigEntryBuilder generalEntryBuilder = builder.entryBuilder();


        ConfigCategory advancedCategory = builder.getOrCreateCategory(Text.of("Advanced"));
        ConfigEntryBuilder advancedEntryBuilder = builder.entryBuilder();

        // Enable adblock option
        generalCategory.addEntry(generalEntryBuilder
                .startBooleanToggle(Text.of("Enable adblock"), config.enableAdblock)
                .setDefaultValue(true)
                .setSaveConsumer(newval -> config.enableAdblock = newval)
                .build()
        );

        // Enable regex autoupdate option
        generalCategory.addEntry(generalEntryBuilder
                .startBooleanToggle(Text.of("Autoupdate Block Regex"), config.autoupdateRegex)
                .setDefaultValue(true)
                .setSaveConsumer(newval -> config.autoupdateRegex = newval)
                .build()
        );


        // Custom regex
        advancedCategory.addEntry(advancedEntryBuilder
                .startTextField(Text.of("Custom Regex"), config.customRegex)
                .setDefaultValue("")
                .setSaveConsumer(newval -> config.customRegex = newval)
                .setErrorSupplier((value) -> {
                    try {
                        "test value".matches(value);
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.of(Text.of("Invalid regex!"));
                    }
                })
                .build()
        );


        return builder.build();

    }
}