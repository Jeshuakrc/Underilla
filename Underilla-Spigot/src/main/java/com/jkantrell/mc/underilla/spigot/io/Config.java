package com.jkantrell.mc.underilla.spigot.io;

import com.jkantrell.mc.underilla.spigot.impl.BukkitBiome;
import com.jkantrell.mc.underilla.core.generation.GenerationConfig;
import com.jkantrell.mc.underilla.core.generation.MergeStrategy;
import com.jkantrell.yamlizer.yaml.AbstractYamlConfig;
import com.jkantrell.yamlizer.yaml.ConfigField;
import com.jkantrell.yamlizer.yaml.YamlElementType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.generator.structure.Structure;
import java.util.Collections;
import java.util.List;

public class Config extends AbstractYamlConfig {

    //CONSTRUCTORS
    public Config(String filePath) {
        super(filePath);
        this.yamlizer.addSerializationRule(Structure.class, (element, type) -> {
            String name = element.get(YamlElementType.STRING).toLowerCase();
            return Registry.STRUCTURE.get(NamespacedKey.minecraft(name));
        });
    }


    //FIELDS
    @ConfigField(path = "reference_world")
    public String referenceWorldName = "backup";

    @ConfigField(path = "generate_noodle_caves")
    public Boolean generateCaves = true;

    @ConfigField(path = "vanilla_population")
    public Boolean vanillaPopulation = true;

    @ConfigField(path = "transfer_biomes")
    public Boolean transferBiomes = true;

    @ConfigField(path = "strategy")
    public MergeStrategy mergeStrategy = MergeStrategy.RELATIVE;

    @ConfigField(path = "relative.upper_limit")
    public int mergeUpperLimit = 320;

    @ConfigField(path = "relative.lower_limit")
    public int mergeLowerLimit = -64;

    @ConfigField(path = "relative.depth")
    public int mergeDepth = 12;

    @ConfigField(path = "relative.keep_underground_biomes")
    public List<Biome> keepUndergroundBiomes = List.of(Biome.DEEP_DARK, Biome.DRIPSTONE_CAVES, Biome.LUSH_CAVES);

    @ConfigField(path = "relative.keep_reference_world_ores")
    public boolean keepReferenceWorldOres = false;

    @ConfigField(path = "absolute.limit")
    public int mergeLimit = 22;

    @ConfigField(path = "blend_range")
    public int mergeBlendRange = 8;

    @ConfigField(path = "preserve_biomes")
    public List<Biome> preserveBiomes = List.of();

    @ConfigField(path = "ravin_biomes")
    public List<Biome> ravinBiomes = List.of();

    @ConfigField(path = "structures.enabled")
    public Boolean generateStructures = true;

    @ConfigField(path = "structures.blacklist")
    public List<Structure> structureBlackList = Collections.emptyList();

    public GenerationConfig toGenerationConfig() {
        GenerationConfig r = new GenerationConfig();

        r.referenceWorldName = this.referenceWorldName;
        r.generateCaves = this.generateCaves;
        r.vanillaPopulation = this.vanillaPopulation;
        r.transferBiomes = this.transferBiomes;
        r.mergeStrategy = this.mergeStrategy;
        r.mergeUpperLimit = this.mergeUpperLimit;
        r.mergeLowerLimit = this.mergeLowerLimit;
        r.mergeDepth = this.mergeDepth;
        r.keepUndergroundBiomes = this.keepUndergroundBiomes.stream().map(BukkitBiome::new).toList();
        r.keepReferenceWorldOres = this.keepReferenceWorldOres;
        r.preserveBiomes = this.preserveBiomes.stream().map(BukkitBiome::new).toList();
        r.ravinBiomes = this.ravinBiomes.stream().map(BukkitBiome::new).toList();
        r.mergeLimit = this.mergeLimit;
        r.mergeBlendRange = this.mergeBlendRange;
        r.generateStructures = this.generateStructures;

        return r;
    }
}
