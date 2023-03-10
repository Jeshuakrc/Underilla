package com.dotkntrell.mc.terraformer.io;

import com.jkantrell.yamlizer.yaml.AbstractYamlConfig;
import com.jkantrell.yamlizer.yaml.ConfigField;

public class Config extends AbstractYamlConfig {

    //CONSTRUCTORS
    public Config(String filePath) {
        super(filePath);
    }

    //ASSETS
    public enum YMergeType { ABSOLUTE, RELATIVE }

    //FIELDS
    @ConfigField(path = "reference_world")
    public String referenceWorldName = "backup";

    @ConfigField(path = "y_merge.")
    public Boolean generateCaves = true;

    @ConfigField(path = "y_merge.enabled")
    public Boolean yMergeEnabled = false;

    @ConfigField(path = "y_merge.type")
    public YMergeType yMergeType = YMergeType.ABSOLUTE;

    @ConfigField(path = "y_merge.height")
    public int yMergeHeight = 22;

    @ConfigField(path = "y_merge.blend_range")
    public int yMergeBlendRange = 8;

    @ConfigField(path = "y_merge.absolute_height")
    public int yMergeAbsoluteHeight = -64;
}
