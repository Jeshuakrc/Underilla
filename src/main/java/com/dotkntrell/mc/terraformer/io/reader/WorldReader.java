package com.dotkntrell.mc.terraformer.io.reader;

import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.MCAFile;
import com.jkantrell.mca.MCAUtil;
import com.jkantrell.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.File;
import java.util.Optional;

public class WorldReader {

    //CONSTANTS
    private static final String REGION_DIRECTORY = "region";

    //FIELDS
    private File worldDirectory_;


    //CONSTRUCTORS
    public WorldReader(String filepath) {
        this(new File(filepath));
    }
    public WorldReader(File worldDirectory) {
        this.worldDirectory_ = worldDirectory;
    }


    //GETTERS
    public String getWorldName() {
        return this.worldDirectory_.getName();
    }


    //UTIL
    public Optional<Material> materialAt(int x, int y, int z) {
        int chunkX = MCAUtil.blockToChunk(x), chunkZ = MCAUtil.blockToChunk(z);
        MCAFile r = this.readRegion(chunkX >> 5,chunkZ >> 5);
        if (r == null) { return Optional.empty(); }
        int regionX = Math.floorMod(x, 512), regionZ = Math.floorMod(z, 512);
        CompoundTag tag = r.getBlockStateAt(regionX, y, regionZ);
        return Optional.ofNullable(tag)
                .map(t -> t.getString("Name"))
                .map(Material::matchMaterial);
    }
    public Optional<ChunkReader> readChunk(int x, int z) {
        MCAFile r = this.readRegion(x >> 5, z >> 5);
        return Optional.ofNullable(r)
                .map(reg -> reg.getChunk(Math.floorMod(x, 32), Math.floorMod(z, 32)))
                .map(ChunkReader::new);
    }


    //PRIVATE UTIL
    private MCAFile readRegion(int x, int z) {
        String fileName = "r." + x + "." + z + ".mca";
        String path = this.worldDirectory_.getPath() + File.separator + WorldReader.REGION_DIRECTORY + File.separator + fileName;
        try {
            return MCAUtil.read(path);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
            return null;
        }
    }
}
