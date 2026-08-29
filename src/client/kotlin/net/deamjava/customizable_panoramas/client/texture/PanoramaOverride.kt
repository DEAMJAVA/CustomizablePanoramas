package net.deamjava.customizable_panoramas.client.texture

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory

object PanoramaOverride {
	private val LOGGER = LoggerFactory.getLogger("customizable-panoramas")
	private var cachedSlug: String? = null
	private var cachedIdentifier: Identifier? = null
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
			cachedIdentifier = identifier
			cachedTexture = texture
		} catch (e: Exception) {
			LOGGER.error("Failed to activate custom panorama '{}'", slug, e)
			try {
				minecraft.textureManager.release(identifier)
			} catch (releaseError: Exception) {
				LOGGER.warn("Failed to release partially-registered texture for '{}'", slug, releaseError)
			}
			cachedSlug = null
			cachedIdentifier = null
			cachedTexture = null
		}
	}

	fun currentOverride(): AbstractTexture? = cachedTexture

	fun release() {
		val identifier = cachedIdentifier
		if (identifier != null) {
			try {
				Minecraft.getInstance().textureManager.release(identifier)
			} catch (e: Exception) {
				LOGGER.warn("Failed to release panorama texture for '{}'", cachedSlug, e)
			}
		}
		cachedSlug = null
		cachedIdentifier = null
		cachedTexture = null
	}
}