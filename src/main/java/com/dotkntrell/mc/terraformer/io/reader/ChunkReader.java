package com.dotkntrell.mc.terraformer.io.reader;

import com.google.common.collect.Lists;
import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.PaletteContainer;
import com.jkantrell.mca.Section;
import com.jkantrell.nbt.tag.CompoundTag;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.List;
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
        return ChunkReader.materialFromBlockTag(tag).orElse(Material.AIR);
    }
    public ChunkReader.Range airColumn() {
        List<Section> sections = Lists.reverse(this.chunk_.getSections());
        int chunkHeight = (sections.get(0).getHeight() * 16) + 15;
        Vector corner1 = new Vector(15, chunkHeight, 15);
        Vector corner2 = new Vector(0, chunkHeight, 0);
        for (Section s : sections) {
            if (s.getBlockStatePalette().getPalette().size() > 1) { break; }
            if (ChunkReader.materialFromBlockTag(s.getBlockStateAt(0,0,0))
                    .map(Material::isAir)
                    .orElse(false)) { break; }
            corner2.setY(s.getHeight() * 16);
        }
        return (corner2.getBlockY() == chunkHeight) ? null : new ChunkReader.Range(corner1, corner2, Material.AIR);
    }


    //PRIVATE UTIL
    private static Optional<Material> materialFromBlockTag(CompoundTag tag) {
        return Optional.ofNullable(tag)
                .map(t -> t.getString("Name"))
                .map(Material::matchMaterial);
    }

}
