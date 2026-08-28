package net.deamjava.customizable_panoramas.client.mixin;

import net.deamjava.customizable_panoramas.client.config.PanoramaManager;
import net.deamjava.customizable_panoramas.client.texture.PanoramaOverride;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CubeMap.class)
public abstract class CubeMapMixin {

	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/texture/TextureManager;getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;"
		)
	)
	private AbstractTexture customizablePanoramas$overridePanoramaTexture(TextureManager textureManager, Identifier location) {
		String selectedSlug = PanoramaManager.INSTANCE.getSelectedSlug();
		if (selectedSlug != null) {
			AbstractTexture override = PanoramaOverride.INSTANCE.currentOverride();
			if (override != null) {
				return override;
			}
		}
		return textureManager.getTexture(location);
	}
}
