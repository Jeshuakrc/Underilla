package com.dotkntrell.mc.terraformer;

import com.dotkntrell.mc.terraformer.generation.TerraformerChunkGenerator;
import com.dotkntrell.mc.terraformer.io.Config;
import com.dotkntrell.mc.terraformer.io.reader.WorldReader;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.FileNotFoundException;

public final class Terraformer extends JavaPlugin {

    public static final Config CONFIG = new Config("");
    private WorldReader worldReader_ = null;

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (this.worldReader_ == null) {
            this.getServer().getLogger().warning("No world with name '" + Terraformer.CONFIG.referenceWorldName + "' found");
            return super.getDefaultWorldGenerator(worldName, id);
        }
        this.getServer().getLogger().warning("Using Terraformer as world generator!");
        return new TerraformerChunkGenerator(this.worldReader_);
    }

    @Override
    public void onEnable() {
        //Setting default config
        this.saveResource("config.yml",false);

        //Loading config
        Terraformer.CONFIG.setFilePath(this.getDataFolder() + File.separator + "config.yml");
        try {
            Terraformer.CONFIG.load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Loading world
        try {
            this.worldReader_ = new WorldReader(Terraformer.CONFIG.referenceWorldName);
            this.getServer().getLogger().info("World + '" + Terraformer.CONFIG.referenceWorldName + "' successfully found.");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        //Registering events
        //this.getServer().getPluginManager().registerEvents(new ChunkEventListener(this.worldReader_), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
