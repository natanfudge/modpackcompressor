package compressor

import net.fabricmc.loader.api.metadata.ModDependency

object CompressorExceptions {
    @JvmStatic
    fun ignoreDep(dep : ModDependency) =  dep.modId == "fabricloader" || dep.modId == "minecraft"

}