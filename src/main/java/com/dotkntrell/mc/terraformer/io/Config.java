package com.dotkntrell.mc.terraformer.io;

import com.jkantrell.yamlizer.yaml.AbstractYamlConfig;
import com.jkantrell.yamlizer.yaml.ConfigField;

public class Config extends AbstractYamlConfig {
    public Config(String filePath) {
        super(filePath);
    }

    @ConfigField
    public String worldName = "backup";
}
