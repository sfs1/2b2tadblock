package com.sfsarfe.adblock.client.mixin;

import com.sfsarfe.adblock.client.MessageFiltering;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sfsarfe.adblock.client.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(net.minecraft.client.gui.hud.ChatHud.class)
public class ChatHudMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("adblock");
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void onAddChatMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci)
    {
//        LOGGER.info("Message: " + message.getString()); // debug shitz
        if (MessageFiltering.containsAd(message))
        {
            ci.cancel();
            return;
        }


        if (MessageFiltering.isSpam(message))
        {
            ci.cancel();
            return;
        }
    }

}
