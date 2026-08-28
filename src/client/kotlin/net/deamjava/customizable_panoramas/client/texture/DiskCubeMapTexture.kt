package net.deamjava.customizable_panoramas.client.texture

import com.mojang.blaze3d.platform.NativeImage
import net.deamjava.customizable_panoramas.client.config.PanoramaManager
import net.minecraft.client.renderer.texture.CubeMapTexture
import net.minecraft.client.renderer.texture.MipmapStrategy
import net.minecraft.client.renderer.texture.TextureContents
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import java.io.IOException
import java.nio.file.Files

class DiskCubeMapTexture(id: Identifier, private val slug: String) : CubeMapTexture(id) {

	override fun loadContents(resourceManager: ResourceManager): TextureContents {
		var stacked: NativeImage? = null
		try {
			for (layer in 0 until 6) {
				val file = PanoramaManager.faceFile(slug, layer)
				if (!Files.exists(file)) {
					throw IOException("Missing panorama face file: $file")
				}

				Files.newInputStream(file).use { input ->
					NativeImage.read(input).use { face ->
						val target = stacked ?: NativeImage(face.width, face.height * 6, false).also { stacked = it }
						if (face.width != target.width || face.height != target.height / 6) {
							throw IOException(
								"Panorama '$slug' face $layer size ${face.width}x${face.height} does not match face 0"
							)
						}
						face.copyRect(target, 0, 0, 0, layer * face.height, face.width, face.height, false, true)
					}
				}
			}
		} catch (e: Exception) {
			stacked?.close()
			throw e
		}

		return TextureContents(
			checkNotNull(stacked),
			TextureMetadataSection(true, false, MipmapStrategy.MEAN, 0.0F)
		)
	}
}
