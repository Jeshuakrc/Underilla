package com.jkantrell.mc.underilla.core.generation;

import java.util.Collections;
import java.util.List;
import com.jkantrell.mc.underilla.core.api.Biome;

public class GenerationConfig {

    // FIELDS
    public String referenceWorldName = "backup";

    public boolean generateCaves = true;

    public boolean vanillaPopulation = true;

    public boolean transferBiomes = true;

    public boolean transferWorldFromCavesWorld = false;

    public String cavesWorldName = "caves_world";

    public List<? extends Biome> transferCavesWorldBiomes = Collections.emptyList();

    public MergeStrategy mergeStrategy = MergeStrategy.RELATIVE;

    public int mergeUpperLimit = 320;

    public int mergeLowerLimit = -64;

    public int mergeDepth = 12;

    public List<? extends Biome> keptUndergroundBiomes = Collections.emptyList();

    public List<String> keptReferenceWorldBlocks = Collections.emptyList();

    public List<? extends Biome> preserveBiomes = Collections.emptyList();

    public List<? extends Biome> ravinBiomes = Collections.emptyList();

    public int mergeLimit = 22;

    public int mergeBlendRange = 8;

    public boolean generateStructures = true;

    public boolean needToMixBiomes() {
        // true if we transfer biomes and we have kept underground biomes and we are using relative merge strategy
        return this.transferBiomes && !this.keptUndergroundBiomes.isEmpty() && MergeStrategy.RELATIVE.equals(this.mergeStrategy);
    }
}
