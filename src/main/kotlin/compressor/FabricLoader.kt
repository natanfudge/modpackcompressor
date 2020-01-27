/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package compressor

import net.fabricmc.loader.discovery.DirectoryModCandidateFinder
import net.fabricmc.loader.discovery.ModCandidate
import net.fabricmc.loader.discovery.ModResolutionException
import net.fabricmc.loader.discovery.ModResolver
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files

/**
 * The main class for mod loading operations.
 */
class FabricLoader(gameDir: File) {

    private val modsDirectory: File = File(gameDir, "mods")
    private val compressedModsDirectory: File = File(gameDir, "compressedmods")

    fun load() {
        try {
            setup()
        } catch (e: ModResolutionException) {
            e.printStackTrace()
        }
    }

    private fun ModCandidate.isJij() = depth >= 1

    @Throws(ModResolutionException::class)
    private fun setup() {
        val resolver = ModResolver()
        resolver.addCandidateFinder(DirectoryModCandidateFinder(modsDirectory.toPath()))
        val candidateMap = resolver.resolve(this)

        for ((id, mod) in candidateMap) {
            println("$id:" + mod.originUrl)
        }

        compressedModsDirectory.mkdirs()

        for (mod in candidateMap.values) {
            val newLocation = File(
                compressedModsDirectory,
                if (mod.isJij()) mod.info.id + "-" + mod.info.version.toString() + ".jar"
                else File(mod.originUrl.file).name
            )
            FileUtils.copyURLToFile(mod.originUrl, newLocation)

            // Delete JIJs
            FileSystems.newFileSystem(newLocation.toPath(), null).use { fs ->
                val jijDir = fs.getPath("META-INF", "jars")
                if (Files.exists(jijDir)) Files.walk(jijDir).forEach { if (!Files.isDirectory(it)) Files.delete(it) }
            }
        }

    }

    fun getLogger() = logger

    companion object {
        val logger: Logger = LogManager.getFormatterLogger("Fabric|Loader")
    }
}