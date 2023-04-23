package com.dotkntrell.mc.underilla.io.reader;

import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.MCAUtil;
import com.jkantrell.mca.Section;
import com.jkantrell.nbt.tag.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ChunkReader implements Reader {

    //ASSETS
    public static record Range(Vector corner1, Vector corner2, Material material) {}
    public static final int MAXIMUM_SECTION_HEIGHT = 20, MINIMUM_SECTION_HEIGHT = -4;
    public static final int MAXIMUM_HEIGHT = MAXIMUM_SECTION_HEIGHT * 16, MINIMUM_HEIGHT = MINIMUM_SECTION_HEIGHT * 16;


    //FIELDS
    private final Chunk chunk_;
    private Integer airColumnHeight_ = null;


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
    @Override
    public Optional<BlockData> blockAt(int x, int y, int z) {
        if (this.chunk_ == null) { return Optional.empty(); }
        CompoundTag tag = this.chunk_.getBlockStateAt(x, y, z);
        Optional<Material> material = ChunkReader.materialFromTag(tag);
        if (material.isEmpty()) { return Optional.empty(); }
        return Optional.of(ChunkReader.blockDataFromTag(material.get(), tag));
    }
    @Override
    public Optional<Material> materialAt(int x, int y, int z) {
        if (this.chunk_ == null) { return Optional.empty(); }
        CompoundTag tag = this.chunk_.getBlockStateAt(x, y, z);
        return ChunkReader.materialFromTag(tag);
    }
    @Override
    public Optional<Biome> biomeAt(int x, int y, int z) {
        //Declaring variables
        Map<Integer, Section> sectionMap = this.chunk_.getSectionMap();
        int height = MCAUtil.blockToChunk(y);
        Section section = sectionMap.get(height);

        //Retuning biome if section exists
        if (section != null) {
            return ChunkReader.biomeFromTag(section.getBiomeAt(x, Math.floorMod(y, 16), z));
        }

        //If sections doesn't exist trying to pull it from the above section
        int i = height + 1;
        while (i < MAXIMUM_SECTION_HEIGHT) {
            section = sectionMap.get(i);
            if (section != null) {
                return ChunkReader.biomeFromTag(section.getBiomeAt(x, 0, z));
            }
            i++;
        }

        //If no above section exists, trying to pull it from the under section
        i = height - 1;
        while (i >= MINIMUM_SECTION_HEIGHT) {
            section = sectionMap.get(i);
            if (section != null) {
                return ChunkReader.biomeFromTag(section.getBiomeAt(x, 15, z));
            }
            i--;
        }

        //Returning empty
        return Optional.empty();
    }
    public int airColumnBottomHeight() {
        if (this.airColumnHeight_ != null) {
            return this.airColumnHeight_;
        }

        Predicate<Section> isAir = s -> {
            if (s.getBlockStatePalette().getPalette().size() > 1) { return false; }
            CompoundTag block = s.getBlockStateAt(0,0,0);
            return ChunkReader.materialFromTag(block).map(Material::isAir).orElse(true);
        };

        Section s;
        int lowest = MAXIMUM_HEIGHT - 1;
        while (lowest > MINIMUM_HEIGHT - 1) {
            s = this.chunk_.getSection(lowest);
            if (s == null || isAir.test(s)) { lowest--; } else { break; }
        }

        this.airColumnHeight_ = (lowest + 1) * 16;
        return this.airColumnHeight_;
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
    public List<LocatedMaterial> locationsOf(Material... materials) {
        List<Material> materialList = Arrays.asList(materials);
        return this.locationsOf(materialList::contains);
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
                        .map(l -> new LocatedMaterial(l.x(), l.y() + (s.getHeight()*16), l.z(), ChunkReader.materialFromTag(l.tag()).orElse(Material.AIR)))
                )
                .filter(secondChecker)
                .toList();
    }
    private static Optional<Material> materialFromTag(CompoundTag tag) {
        return Optional.ofNullable(tag)
                .map(t -> t.getString("Name"))
                .map(Material::matchMaterial);
    }
    private static Optional<Biome> biomeFromTag(StringTag tag) {
        String[] raw = tag.getValue().split(":");
        String name = raw.length > 1
                ? raw[1]
                : raw[0];
        try {
            return Optional.of(Biome.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Could not resolve biome '" + name + "'");
            return Optional.empty();
        }
    }
    private static BlockData blockDataFromTag(Material material, CompoundTag tag) {
        CompoundTag properties = tag.getCompoundTag("Properties");
        if (properties == null) { return material.createBlockData(); }
        String dataString = TagInterpreter.COMPOUND.interpretBlockDataString(properties);
        return material.createBlockData(dataString);
    }
}
