package net.deamjava.customizable_panoramas.client.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale


object PanoramaManager {
	private val LOGGER = LoggerFactory.getLogger("customizable-panoramas")
	private val GSON = GsonBuilder().setPrettyPrinting().create()

	private val rootDir: Path = FabricLoader.getInstance().configDir.resolve("customizable-panoramas")
	private val panoramasDir: Path = rootDir.resolve("panoramas")
	private val selectionFile: Path = rootDir.resolve("selection.json")

	private data class SelectionData(val selectedSlug: String? = null)
	private data class MetaData(val displayName: String = "", val createdAtEpochMillis: Long = 0L)

	private var cachedEntries: MutableList<PanoramaEntry>? = null
	var selectedSlug: String? = null
		private set

	fun init() {
		Files.createDirectories(panoramasDir)
		loadSelection()
		reloadEntries()
	}

	fun panoramaDir(slug: String): Path = panoramasDir.resolve(slug)

	fun faceFile(slug: String, faceIndex: Int): Path = panoramaDir(slug).resolve("$faceIndex.png")

	fun entries(): List<PanoramaEntry> {
		if (cachedEntries == null) reloadEntries()
		return cachedEntries!!.toList()
	}

	fun findEntry(slug: String): PanoramaEntry? = entries().firstOrNull { it.slug == slug }

	fun createEntry(desiredName: String): PanoramaEntry {
		val baseSlug = slugify(desiredName).ifBlank { "panorama" }
		var slug = baseSlug
		var suffix = 1
		while (Files.exists(panoramaDir(slug))) {
			suffix++
			slug = "$baseSlug-$suffix"
		}

		Files.createDirectories(panoramaDir(slug))
		val meta = MetaData(displayName = desiredName, createdAtEpochMillis = System.currentTimeMillis())
		writeMeta(slug, meta)
		reloadEntries()
		return PanoramaEntry(slug, meta.displayName, meta.createdAtEpochMillis)
	}

	fun rename(slug: String, newDisplayName: String) {
		val existing = findEntry(slug) ?: return
		writeMeta(slug, MetaData(displayName = newDisplayName, createdAtEpochMillis = existing.createdAtEpochMillis))
		reloadEntries()
	}

	fun delete(slug: String) {
		val dir = panoramaDir(slug)
		if (Files.exists(dir)) {
			Files.walk(dir).sorted(Comparator.reverseOrder()).forEach { path ->
				try {
					Files.delete(path)
				} catch (e: IOException) {
					LOGGER.warn("Failed to delete {}", path, e)
				}
			}
		}
		if (selectedSlug == slug) select(null)
		reloadEntries()
	}

	fun select(slug: String?) {
		selectedSlug = slug
		saveSelection()
	}


	private fun reloadEntries() {
		val result = mutableListOf<PanoramaEntry>()
		if (Files.isDirectory(panoramasDir)) {
			Files.newDirectoryStream(panoramasDir).use { stream ->
				for (dir in stream) {
					if (!Files.isDirectory(dir)) continue
					val slug = dir.fileName.toString()
					val meta = readMeta(slug) ?: continue
					result.add(PanoramaEntry(slug, meta.displayName, meta.createdAtEpochMillis))
				}
			}
		}
		result.sortBy { it.createdAtEpochMillis }
		cachedEntries = result
	}

	private fun readMeta(slug: String): MetaData? {
		val metaFile = panoramaDir(slug).resolve("meta.json")
		if (!Files.exists(metaFile)) return null
		return try {
			Files.newBufferedReader(metaFile).use { GSON.fromJson(it, MetaData::class.java) }
		} catch (e: Exception) {
			LOGGER.warn("Failed to read meta.json for {}", slug, e)
			null
		}
	}

	private fun writeMeta(slug: String, meta: MetaData) {
		val metaFile = panoramaDir(slug).resolve("meta.json")
		Files.newBufferedWriter(metaFile).use { GSON.toJson(meta, it) }
	}

	private fun loadSelection() {
		selectedSlug = try {
			if (Files.exists(selectionFile)) {
				Files.newBufferedReader(selectionFile).use { GSON.fromJson(it, SelectionData::class.java) }?.selectedSlug
			} else null
		} catch (e: Exception) {
			LOGGER.warn("Failed to read selection.json", e)
			null
		}
	}

	private fun saveSelection() {
		try {
			Files.createDirectories(rootDir)
			Files.newBufferedWriter(selectionFile).use { GSON.toJson(SelectionData(selectedSlug), it) }
		} catch (e: IOException) {
			LOGGER.warn("Failed to write selection.json", e)
		}
	}

	private fun slugify(name: String): String {
		val lower = name.lowercase(Locale.ROOT).trim()
		return lower.replace(Regex("[^a-z0-9]+"), "-").trim('-')
	}
}
