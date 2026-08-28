package net.deamjava.customizable_panoramas.client.mixin;

import com.mojang.blaze3d.platform.Window;
import net.deamjava.customizable_panoramas.client.capture.PanoramaCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public abstract class WindowSquareAspectMixin {

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    private void customizable_panoramas$squareWidth(CallbackInfoReturnable<Integer> cir) {
        if (PanoramaCapture.INSTANCE.getInProgress()) {
            cir.setReturnValue(PanoramaCapture.CAPTURE_SIZE);
        }
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void customizable_panoramas$squareHeight(CallbackInfoReturnable<Integer> cir) {
        if (PanoramaCapture.INSTANCE.getInProgress()) {
            cir.setReturnValue(PanoramaCapture.CAPTURE_SIZE);
        }
    }
}