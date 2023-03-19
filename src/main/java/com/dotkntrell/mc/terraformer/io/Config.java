package com.dotkntrell.mc.terraformer.io;

import com.jkantrell.yamlizer.yaml.AbstractYamlConfig;
import com.jkantrell.yamlizer.yaml.ConfigField;

public class Config extends AbstractYamlConfig {

    //CONSTRUCTORS
    public Config(String filePath) {
        super(filePath);
    }

    //ASSETS
    public enum YMergeStrategy { ABSOLUTE, RELATIVE }

    //FIELDS
    @ConfigField(path = "reference_world")
    public String referenceWorldName = "backup";

    @ConfigField(path = "generate_caves")
    public Boolean generateCaves = true;

    @ConfigField(path = "y_merge.enabled")
    public Boolean yMergeEnabled = false;

    @ConfigField(path = "y_merge.strategy")
    public YMergeStrategy yMergeStrategy = YMergeStrategy.ABSOLUTE;

    @ConfigField(path = "y_merge.relative.upper_limit")
    public int yMergeUpperLimit = 320;

    @ConfigField(path = "y_merge.relative.lower_limit")
    public int yMergeLowerLimit = -64;

    @ConfigField(path = "y_merge.relative.depth")
    public int yMergeDepth = 12;

    @ConfigField(path = "y_merge.relative.lower_limit")
    public int yMergeHeight = 22;

    @ConfigField(path = "y_merge.blend_range")
    public int yMergeBlendRange = 8;
}
