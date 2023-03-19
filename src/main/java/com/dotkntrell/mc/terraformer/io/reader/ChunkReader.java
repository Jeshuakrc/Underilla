package com.dotkntrell.mc.terraformer.io.reader;

import com.google.common.collect.Lists;
import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.Section;
import com.jkantrell.nbt.tag.CompoundTag;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ChunkReader implements Reader {

    //ASSETS
    public static record Range(Vector corner1, Vector corner2, Material material) {}


    //FIELDS
    private final Chunk chunk_;


    //CONSTRUCTORS
    ChunkReader(Chunk chunk) {
        this.chunk_ = chunk;
    }


    //UTIL
    public Optional<Material> materialAt(int x, int y, int z) {
        if (this.chunk_ == null) { return Optional.empty(); }
        CompoundTag tag = this.chunk_.getBlockStateAt(x, y, z);
        return ChunkReader.materialFromBlockTag(tag);
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
    public List<LocatedMaterial> locationsOf(Predicate<Material> checker) {
        return this.chunk_.locationsOf(t -> {
            String n;
            try { n = t.getString("Name"); } catch (ClassCastException e) { return false; }
            if (n == null) { return false; }
            Material m = Material.matchMaterial(n);
            if (m == null) { return false; }
            return checker.test(m);
        }).stream()
                .map(l -> new LocatedMaterial(l.x(), l.y(), l.z(), Material.matchMaterial(l.tag().getString("Name"))))
                .toList();
    }
    public List<LocatedMaterial> locationsOf(Material material) {
        return this.locationsOf(material::equals);
    }

    //PRIVATE UTIL
    private static Optional<Material> materialFromBlockTag(CompoundTag tag) {
        return Optional.ofNullable(tag)
                .map(t -> t.getString("Name"))
                .map(Material::matchMaterial);
    }

}
