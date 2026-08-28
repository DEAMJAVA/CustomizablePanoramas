package net.deamjava.customizable_panoramas.client.mixin;

import net.deamjava.customizable_panoramas.client.gui.ManagePanoramasScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends net.minecraft.client.gui.screens.Screen {

	protected OptionsScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void customizablePanoramas$addManageButton(CallbackInfo ci) {
		this.addRenderableWidget(
			Button.builder(
					Component.translatable("customizable-panoramas.button.manage"),
					button -> this.minecraft.gui.setScreen(new ManagePanoramasScreen((OptionsScreen) (Object) this))
				)
				.bounds(4, 4, 100, 20)
				.build()
		);
	}
}
