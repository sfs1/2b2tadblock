package com.sfsarfe.adblock.client.gui;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ConfigButton extends AbstractConfigListEntry<Void> {
    private final ButtonWidget button;

    public ConfigButton(Text label, Runnable onClick) {
        super(label, false);

        // Create the button widget. Position/size will be set dynamically in render().
        this.button = ButtonWidget.builder(label, btn -> onClick.run())
                .dimensions(0, 0, 150, 20)
                .build();
    }

    @Override
    public void render(DrawContext drawContext, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float delta) {
        // ClothConfig tells us where the entry should go:
        button.setX(x);
        button.setY(y);
        button.setWidth(entryWidth);
        button.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends Element> children() {
        return Collections.singletonList(button);
    }

    public List<? extends Selectable> selectableChildren() {
        return Collections.singletonList(button);
    }

    @Override
    public Void getValue() { return null; }

    @Override
    public Optional<Void> getDefaultValue() { return Optional.empty(); }

    @Override
    public void save() {
        // no-op, since this doesn’t store config values
    }

    @Override
    public List<? extends Selectable> narratables() {
        return List.of();
    }
}
