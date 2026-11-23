package dev.xclient.mixin;

import dev.xclient.modules.SoundDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void xclient$sound(ClientboundSoundPacket packet, CallbackInfo ci) {
        // the handler is first invoked on the netty thread, then re-run on the main thread
        if (!Minecraft.getInstance().isSameThread()) return;
        if (SoundDetector.INSTANCE != null) {
            SoundDetector.INSTANCE.onSound(packet.getSound().getRegisteredName(), packet.getX(), packet.getY(), packet.getZ());
        }
    }
}
