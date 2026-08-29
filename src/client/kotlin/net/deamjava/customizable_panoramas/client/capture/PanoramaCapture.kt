package net.deamjava.customizable_panoramas.client.capture

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import com.mojang.blaze3d.pipeline.MainTarget
import com.mojang.blaze3d.pipeline.RenderTarget


private data class Face(val layerIndex: Int, val yaw: Float, val pitch: Float)

private val FACES = listOf(
	Face(1, yaw = 270f, pitch = 0f),    // right
	Face(0, yaw = 90f,  pitch = 0f),   // left
	Face(2, yaw = 0f,   pitch = 90f),  // up
	Face(3, yaw = 0f,   pitch = -90f),    // down
	Face(4, yaw = 0f,   pitch = 0f),  // front
	Face(5, yaw = 180f, pitch = 0f),  // back
)


private const val SETTLE_TICKS = 15


object PanoramaCapture {
	private val LOGGER = LoggerFactory.getLogger("customizable-panoramas")

	private class ActiveCapture(
		val stagingDir: Path,
		val originalYaw: Float,
		val originalPitch: Float,
		val originalHudHidden: Boolean,
		val originalFov: Int,
		val originalTarget: RenderTarget,
		val originalWidth: Int,
		val originalHeight: Int,
		val future: CompletableFuture<Path>
	) {
		var faceIndex = 0
		var ticksRemaining = SETTLE_TICKS
		var awaitingScreenshot = false
	}

	private var active: ActiveCapture? = null

	val inProgress: Boolean get() = active != null


	const val CAPTURE_SIZE = 1024 * 2

	fun start(): CompletableFuture<Path> {
		val minecraft = Minecraft.getInstance()
		val player = minecraft.player
			?: return CompletableFuture.failedFuture(IllegalStateException("No player in world"))

		if (active != null) {
			return CompletableFuture.failedFuture(IllegalStateException("Capture already in progress"))
		}

		val stagingDir = try {
			Files.createTempDirectory("customizable-panoramas-capture")
		} catch (e: IOException) {
			return CompletableFuture.failedFuture(e)
		}

		val future = CompletableFuture<Path>()
		val hud = minecraft.gui.hud
		val options = minecraft.options
		val originalTarget = minecraft.gameRenderer.mainRenderTarget()

		active = ActiveCapture(
			stagingDir = stagingDir,
			originalYaw = player.yRot,
			originalPitch = player.xRot,
			originalHudHidden = hud.isHidden,
			originalFov = options.fov().get(),
			originalTarget = originalTarget,
			originalWidth = originalTarget.width,
			originalHeight = originalTarget.height,
			future = future
		)

		originalTarget.resize(CAPTURE_SIZE, CAPTURE_SIZE)

		options.fov().set(90)
		setHudHidden(true)

		beginFace(0)

		return future
	}

	private fun setHudHidden(hidden: Boolean) {
		val hud = Minecraft.getInstance().gui.hud
		if (hud.isHidden != hidden) {
			hud.toggle()
		}
	}

	fun tick(client: Minecraft) {
		val capture = active ?: return
		if (capture.awaitingScreenshot) return

		if (capture.ticksRemaining > 0) {
			capture.ticksRemaining--
			return
		}

		capture.awaitingScreenshot = true
		val target = capture.originalTarget
		try {
			Screenshot.takeScreenshot(target) { image ->
				client.execute { onFaceCaptured(capture, image) }
			}
		} catch (e: Exception) {
			failCapture(capture, e)
		}
	}

	private fun beginFace(index: Int) {
		val capture = active ?: return
		val player = Minecraft.getInstance().player
			?: return failCapture(capture, IllegalStateException("Player left the world mid-capture"))

		if (index >= FACES.size) {
			finishCapture(capture)
			return
		}

		val face = FACES[index]
		player.yRot = face.yaw
		player.xRot = face.pitch
		capture.faceIndex = index
		capture.ticksRemaining = SETTLE_TICKS + if (index == 0) 10 else 0
		capture.awaitingScreenshot = false
	}

	private fun onFaceCaptured(capture: ActiveCapture, image: NativeImage) {
		if (active !== capture) {
			image.close()
			return
		}

		val face = FACES[capture.faceIndex]
		val outFile = capture.stagingDir.resolve("${face.layerIndex}.png")
		try {
			image.writeToFile(outFile)
		} catch (e: IOException) {
			failCapture(capture, e)
			return
		} finally {
			image.close()
		}

		beginFace(capture.faceIndex + 1)
	}

	private fun cropToSquare(source: NativeImage): NativeImage {
		val size = minOf(source.width, source.height)
		val srcX = (source.width - size) / 2
		val srcY = (source.height - size) / 2
		val cropped = NativeImage(size, size, false)
		source.copyRect(cropped, srcX, srcY, 0, 0, size, size, false, false)
		return cropped
	}

	private fun finishCapture(capture: ActiveCapture) {
		restoreOrientation(capture)
		active = null
		capture.future.complete(capture.stagingDir)
	}

	private fun failCapture(capture: ActiveCapture, error: Throwable) {
		LOGGER.error("Panorama capture failed", error)
		restoreOrientation(capture)
		active = null
		capture.future.completeExceptionally(error)
	}

	private fun restoreOrientation(capture: ActiveCapture) {
		val minecraft = Minecraft.getInstance()
		capture.originalTarget.resize(capture.originalWidth, capture.originalHeight)

		setHudHidden(capture.originalHudHidden)
		minecraft.options.fov().set(capture.originalFov)

		val player = minecraft.player ?: return
		player.yRot = capture.originalYaw
		player.xRot = capture.originalPitch
	}


}