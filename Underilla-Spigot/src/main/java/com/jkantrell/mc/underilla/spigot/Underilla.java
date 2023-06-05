package com.jkantrell.mc.underilla.spigot;

import com.jkantrell.mc.underilla.spigot.generation.UnderillaChunkGenerator;
import com.jkantrell.mc.underilla.spigot.io.Config;
import com.jkantrell.mc.underilla.spigot.impl.BukkitWorldReader;
import com.jkantrell.mc.underilla.spigot.listener.StructureEventListener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.FileNotFoundException;

public final class Underilla extends JavaPlugin {

    public static final Config CONFIG = new Config("");
    private BukkitWorldReader worldReader_ = null;

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (this.worldReader_ == null) {
            this.getServer().getLogger().warning("No world with name '" + Underilla.CONFIG.referenceWorldName + "' found");
            return super.getDefaultWorldGenerator(worldName, id);
        }
        this.getServer().getLogger().info("Using Underilla as world generator!");
        return new UnderillaChunkGenerator(this.worldReader_);
    }

    @Override
    public void onEnable() {
        //Setting default config
        this.saveResource("config.yml",false);

        //Loading config
        Underilla.CONFIG.setFilePath(this.getDataFolder() + File.separator + "config.yml");
        try {
            Underilla.CONFIG.load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Loading world
        try {
            this.worldReader_ = new BukkitWorldReader(Underilla.CONFIG.referenceWorldName);
            this.getServer().getLogger().info("World + '" + Underilla.CONFIG.referenceWorldName + "' found.");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        //Registering listeners
        if (CONFIG.generateStructures) {
            this.getServer().getPluginManager().registerEvents(new StructureEventListener(CONFIG.structureBlackList), this);
        }
    }
}
