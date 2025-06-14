package com.sfsarfe.adblock.client.mixin;

import com.sfsarfe.adblock.client.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.text.Style;

import java.util.Optional;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.rightClickIgnore)
            return;
        if (button != 1) return; // Right-click only

        ChatScreen screen = (ChatScreen) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        ChatHud chatHud = client.inGameHud.getChatHud();

        Style style = chatHud.getTextStyleAt(mouseX, mouseY);
        if (style == null) return;


        // try to extract the name from either clickEvent (2b2t) or hoverEvent (vanilla)
        // sorta crude ig but it works so eh who cares, if it aint broke dont fix it
        String playerName;
        ClickEvent clickEvent = style.getClickEvent();
        HoverEvent hoverEvent = style.getHoverEvent();
        if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
            String value = clickEvent.getValue();

            if (!value.startsWith("/w"))
                return;
            String[] parts = value.strip().split(" ");
            if (parts.length == 2) {
                playerName = parts[1];
            }
            else return;
        }
        else if (hoverEvent != null)
        {
            HoverEvent.EntityContent entity = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (entity == null)
                return;
            playerName = entity.name.get().getString().strip();
        }
        else return;

        client.player.networkHandler.sendChatCommand(config.spamFilterIgnoreCommand.substring(1) + " " + playerName);
        client.inGameHud.getChatHud().addMessage(Text.literal("Ignored " + playerName));
        cir.setReturnValue(true);

    }
}
