package com.dotkntrell.mc.underilla.io;

import com.jkantrell.yamlizer.yaml.AbstractYamlConfig;
import com.jkantrell.yamlizer.yaml.ConfigField;
import org.bukkit.block.Biome;

import java.util.List;

public class Config extends AbstractYamlConfig {

    //CONSTRUCTORS
    public Config(String filePath) {
        super(filePath);
    }

    //ASSETS
    public enum MergeStrategy { ABSOLUTE, RELATIVE, NONE }

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

    @ConfigField(path = "absolute.limit")
    public int mergeLimit = 22;

    @ConfigField(path = "blend_range")
    public int mergeBlendRange = 8;
}
