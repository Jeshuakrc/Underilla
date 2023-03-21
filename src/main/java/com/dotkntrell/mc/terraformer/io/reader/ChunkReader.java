package com.dotkntrell.mc.terraformer.io.reader;

import com.google.common.collect.Lists;
import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.Section;
import com.jkantrell.nbt.tag.CompoundTag;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ChunkReader implements Reader {

    //ASSETS
    public static record Range(Vector corner1, Vector corner2, Material material) {}


    //FIELDS
    private final Chunk chunk_;


    //CONSTRUCTORS
    ChunkReader(Chunk chunk) {
        this.chunk_ = chunk;
    }


    //GETTERS
    public int getX() {
        return this.chunk_.getX();
    }
    public int getZ() {
        return this.chunk_.getZ();
    }


    //UTIL
    public Optional<Material> materialAt(int x, int y, int z) {
        if (this.chunk_ == null) { return Optional.empty(); }
        CompoundTag tag = this.chunk_.getBlockStateAt(x, y, z);
        return ChunkReader.materialFromBlockTag(tag);
    }
    public int airColumnBottomHeight() {
        Predicate<Section> isAir = s -> {
            if (s.getBlockStatePalette().getPalette().size() > 1) { return false; }
            CompoundTag block = s.getBlockStateAt(0,0,0);
            return ChunkReader.materialFromBlockTag(block).map(Material::isAir).orElse(true);
        };

        Section s;
        int lowest = 19;
        while (lowest > -5) {
            s = this.chunk_.getSection(lowest);
            if (s == null || isAir.test(s)) { lowest--; } else { break; }
        }

        return (lowest + 1) * 16;
    }
    public List<LocatedMaterial> locationsOf(Predicate<Material> checker, int under, int above) {
        Stream<Section> sectionStream = this.chunk_.getSections().stream().filter(s -> {
            int lower = s.getHeight() * 16, upper = lower + 15;
            if (under <= lower) { return false; }
            return above < upper;
        });
        Predicate<LocatedMaterial> locationCheck = l -> l.y() > above && l.y() < under;
        return this.locationsOf(checker, locationCheck, sectionStream);

    }
    public List<LocatedMaterial> locationsOf(Predicate<Material> checker) {
        return this.locationsOf(checker, l -> true, this.chunk_.getSections().stream());

    }
    public List<LocatedMaterial> locationsOf(Material material) {
        return this.locationsOf(material::equals);
    }

    //PRIVATE UTIL
    private List<LocatedMaterial> locationsOf(Predicate<Material> checker, Predicate<LocatedMaterial> secondChecker, Stream<Section> sections) {
        return sections
                .flatMap(s -> s.getBlockLocations(t -> {
                        String n;
                        try { n = t.getString("Name"); } catch (ClassCastException e) { return false; }
                        if (n == null) { return false; }
                        Material m = Material.matchMaterial(n);
                        if (m == null) { return false; }
                        return checker.test(m);
                    }).stream()
                        .map(l -> new LocatedMaterial(l.x(), l.y() + (s.getHeight()*16), l.z(), ChunkReader.materialFromBlockTag(l.tag()).orElse(Material.AIR)))
                )
                .filter(secondChecker)
                .toList();
    }
    private static Optional<Material> materialFromBlockTag(CompoundTag tag) {
        return Optional.ofNullable(tag)
                .map(t -> t.getString("Name"))
                .map(Material::matchMaterial);
    }
}
