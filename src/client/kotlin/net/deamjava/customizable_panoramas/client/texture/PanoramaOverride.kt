package net.deamjava.customizable_panoramas.client.texture

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory

object PanoramaOverride {
	private val LOGGER = LoggerFactory.getLogger("customizable-panoramas")
	private var cachedSlug: String? = null
	private var cachedTexture: AbstractTexture? = null

	fun activate(slug: String?) {
		if (slug == cachedSlug) return
		release()
		if (slug == null) return

		val identifier = Identifier.fromNamespaceAndPath("customizable-panoramas", "custom_panorama/$slug")
		val minecraft = Minecraft.getInstance()
		val texture = DiskCubeMapTexture(identifier, slug)

		try {
			minecraft.textureManager.register(identifier, texture)

			val contents = texture.loadContents(minecraft.resourceManager)
			texture.apply(contents)

			cachedSlug = slug
			cachedTexture = texture
		} catch (e: Exception) {
			LOGGER.error("Failed to activate custom panorama '{}'", slug, e)
			try {
				minecraft.textureManager.release(identifier)
			} catch (releaseError: Exception) {
				LOGGER.warn("Failed to release partially-registered texture for '{}'", slug, releaseError)
			}
			texture.close()
			cachedSlug = null
			cachedTexture = null
		}
	}

	fun currentOverride(): AbstractTexture? = cachedTexture

	fun release() {
		cachedTexture?.close()
		cachedSlug = null
		cachedTexture = null
	}
}
