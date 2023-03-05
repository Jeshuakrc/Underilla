package com.dotkntrell.mc.terraformer;

import com.dotkntrell.mc.terraformer.generation.TerraformerChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class Terraformer extends JavaPlugin {


    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        this.getLogger().warning("Using Terraformer generation.");
        return new TerraformerChunkGenerator();
    }

    @Override
    public void onEnable() {
        this.getLogger().warning("15 / 16 = " + Integer.toString(15/16));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
