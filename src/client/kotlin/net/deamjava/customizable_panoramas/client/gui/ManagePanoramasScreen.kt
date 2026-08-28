package net.deamjava.customizable_panoramas.client.gui

import net.deamjava.customizable_panoramas.client.config.PanoramaManager
import net.deamjava.customizable_panoramas.client.texture.PanoramaOverride
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class ManagePanoramasScreen(private val parent: Screen) : Screen(Component.translatable("customizable-panoramas.screen.manage.title")) {

	private var renamingSlug: String? = null
	private lateinit var renameBox: EditBox

	override fun init() {
		val entries = PanoramaManager.entries()
		val rowHeight = 24
		var y = 32
		val listWidth = 280
		val x = (this.width - listWidth) / 2

		for (entry in entries) {
			if (renamingSlug == entry.slug) {
				renameBox = EditBox(this.font, x, y, 160, 20, Component.literal(entry.displayName))
				renameBox.value = entry.displayName
				renameBox.setMaxLength(48)
				this.addRenderableWidget(renameBox)

				this.addRenderableWidget(
					Button.builder(Component.translatable("gui.done")) {
						PanoramaManager.rename(entry.slug, renameBox.value.ifBlank { entry.displayName })
						renamingSlug = null
						this.rebuildWidgets()
					}.bounds(x + 166, y, 56, 20).build()
				)
			} else {
				val isSelected = PanoramaManager.selectedSlug == entry.slug
				val label = if (isSelected) "\u2605 ${entry.displayName}" else entry.displayName

				this.addRenderableWidget(
					Button.builder(Component.literal(label)) {
						if (isSelected) {
							PanoramaManager.select(null)
							PanoramaOverride.release()
						} else {
							PanoramaManager.select(entry.slug)
							PanoramaOverride.activate(entry.slug)
						}
						this.rebuildWidgets()
					}.bounds(x, y, 160, 20).build()
				)

				this.addRenderableWidget(
					Button.builder(Component.translatable("customizable-panoramas.screen.manage.rename")) {
						renamingSlug = entry.slug
						this.rebuildWidgets()
					}.bounds(x + 166, y, 56, 20).build()
				)
			}

			this.addRenderableWidget(
				Button.builder(Component.translatable("customizable-panoramas.screen.manage.delete")) {
					PanoramaManager.delete(entry.slug)
					this.rebuildWidgets()
				}.bounds(x + 226, y, 54, 20).build()
			)

			y += rowHeight
		}

		if (entries.isEmpty()) {
		}

		this.addRenderableWidget(
			Button.builder(Component.translatable("gui.back")) { this.minecraft.gui.setScreen(parent) }
				.bounds((this.width - 200) / 2, this.height - 28, 200, 20)
				.build()
		)
	}

	override fun onClose() {
		this.minecraft.gui.setScreen(parent)
	}
}
