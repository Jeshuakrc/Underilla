package com.jkantrell.mc.underilla.core.generation;

import com.jkantrell.mc.underilla.core.api.Biome;
import java.util.Collections;
import java.util.List;

public class GenerationConfig {

    //FIELDS
    public String referenceWorldName = "backup";

    public Boolean generateCaves = true;

    public Boolean vanillaPopulation = true;

    public Boolean transferBiomes = true;

    public MergeStrategy mergeStrategy = MergeStrategy.RELATIVE;

    public int mergeUpperLimit = 320;

    public int mergeLowerLimit = -64;

    public int mergeDepth = 12;

    public List<? extends Biome> keepUndergroundBiomes = Collections.emptyList();

    public List<? extends Biome> preserveBiomes = Collections.emptyList();

    public int mergeLimit = 22;

    public int mergeBlendRange = 8;

    public Boolean generateStructures = true;
}
