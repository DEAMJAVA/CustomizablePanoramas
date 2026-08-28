package net.deamjava.customizable_panoramas.client.gui

import net.deamjava.customizable_panoramas.client.config.PanoramaManager
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component


class NamePanoramaScreen(
	private val parent: Screen?,
	private val onNamed: (String) -> Unit
) : Screen(Component.translatable("customizable-panoramas.screen.name_panorama.title")) {

	private lateinit var nameBox: EditBox

	override fun init() {
		val boxWidth = 200
		val x = (this.width - boxWidth) / 2
		val y = this.height / 2 - 10

		nameBox = EditBox(this.font, x, y, boxWidth, 20, Component.translatable("customizable-panoramas.screen.name_panorama.field"))
		nameBox.value = defaultName()
		nameBox.setMaxLength(48)
		this.addRenderableWidget(nameBox)
		this.setInitialFocus(nameBox)

		this.addRenderableWidget(
			Button.builder(Component.translatable("customizable-panoramas.screen.name_panorama.confirm")) { confirm() }
				.bounds(x, y + 28, 96, 20)
				.build()
		)
		this.addRenderableWidget(
			Button.builder(Component.translatable("gui.cancel")) { this.minecraft.gui.setScreen(parent) }
				.bounds(x + 104, y + 28, 96, 20)
				.build()
		)
	}

	private fun confirm() {
		val name = nameBox.value.ifBlank { defaultName() }
		onNamed(name)
	}

	private fun defaultName(): String {
		val existing = PanoramaManager.entries().size + 1
		return "Panorama $existing"
	}

	override fun onClose() {
		this.minecraft.gui.setScreen(parent)
	}
}
