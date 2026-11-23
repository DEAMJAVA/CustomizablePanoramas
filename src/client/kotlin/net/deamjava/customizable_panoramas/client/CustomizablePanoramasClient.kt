package net.deamjava.customizable_panoramas.client

import com.mojang.blaze3d.platform.InputConstants
import net.deamjava.customizable_panoramas.client.capture.PanoramaCapture
import net.deamjava.customizable_panoramas.client.config.PanoramaManager
import net.deamjava.customizable_panoramas.client.gui.NamePanoramaScreen
import net.deamjava.customizable_panoramas.client.texture.PanoramaOverride
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object CustomizablePanoramasClient : ClientModInitializer {
	private val LOGGER = LoggerFactory.getLogger("customizable-panoramas")

	val category = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath("customizable-panoramas", "customizable-panoramas")
	)

	private val captureKey = KeyMapping(
		"key.customizable-panoramas.capture",
		InputConstants.UNKNOWN.value,
		category
	)

	override fun onInitializeClient() {
		PanoramaManager.init()
		KeyMappingHelper.registerKeyMapping(captureKey)


		ClientLifecycleEvents.CLIENT_STARTED.register {
			PanoramaOverride.activate(PanoramaManager.selectedSlug)
		}

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			while (captureKey.consumeClick()) {
				triggerCapture(client)
			}
			PanoramaCapture.tick(client)
		}
	}

	private fun triggerCapture(client: Minecraft) {
		if (PanoramaCapture.inProgress) return
		if (client.player == null || client.level == null) return

		PanoramaCapture.start()
			.whenComplete { stagingDir, error ->
				client.execute {
					if (error != null) {
						LOGGER.error("Failed to capture panorama", error)
						return@execute
					}

					val previousScreen = client.gui.screen()
					client.gui.setScreen(
						NamePanoramaScreen(previousScreen) { name ->
							val entry = PanoramaManager.createEntry(name)
							for (i in 0 until 6) {
								val source = stagingDir.resolve("$i.png")
								val dest = PanoramaManager.faceFile(entry.slug, i)
								if (Files.exists(source)) {
									Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING)
								}
							}
							client.gui.setScreen(previousScreen)
						}
					)
				}
			}
	}
}