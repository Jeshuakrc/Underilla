package com.dotkntrell.mc.terraformer.io.reader;

import com.jkantrell.mca.Chunk;
import com.jkantrell.nbt.tag.CompoundTag;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Optional;

public class ChunkReader {

    //ASSETS
    public static record Range(Vector corner1, Vector corner2, Material material) {}


    //FIELDS
    private final Chunk chunk_;


    //CONSTRUCTORS
    ChunkReader(Chunk chunk) {
        this.chunk_ = chunk;
    }


    //UTIL
    public Material materialAt(int x, int y, int z) {
        if (this.chunk_ == null) { return null; }
        CompoundTag tag = this.chunk_.getBlockStateAt(x, y, z);
        return Optional.ofNullable(tag)
                .map(t -> t.getString("Name"))
                .map(Material::matchMaterial)
                .orElse(Material.AIR);
    }
    public ChunkReader.Range airColumn() {
        this.chunk_.getS
    }

}
