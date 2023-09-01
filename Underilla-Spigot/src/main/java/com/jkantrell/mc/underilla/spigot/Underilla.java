package com.jkantrell.mc.underilla.spigot;

import java.io.File;
import java.io.FileNotFoundException;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import com.jkantrell.mc.underilla.spigot.generation.UnderillaChunkGenerator;
import com.jkantrell.mc.underilla.spigot.impl.BukkitWorldReader;
import com.jkantrell.mc.underilla.spigot.io.Config;
import com.jkantrell.mc.underilla.spigot.listener.StructureEventListener;

public final class Underilla extends JavaPlugin {

    private static Underilla plugin;
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
        plugin = this;
        // Setting default config
        this.saveResource("config.yml", false);

        // Loading config
        Underilla.CONFIG.setFilePath(this.getDataFolder() + File.separator + "config.yml");
        try {
            Underilla.CONFIG.load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Loading world
        try {
            this.worldReader_ = new BukkitWorldReader(Underilla.CONFIG.referenceWorldName);
            this.getServer().getLogger().info("World + '" + Underilla.CONFIG.referenceWorldName + "' found.");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        // Registering listeners
        if (CONFIG.generateStructures) {
            this.getServer().getPluginManager().registerEvents(new StructureEventListener(CONFIG.structureBlackList), this);
        }
    }

    // @Override
    // public void onDisable() {
    // try {
    // long totalTime = Generator.times.entrySet().stream().mapToLong(Map.Entry::getValue).sum();
    // for (Map.Entry<String, Long> entry : Generator.times.entrySet()) {
    // this.getServer().getLogger()
    // .info(entry.getKey() + " took " + entry.getValue() + "ms (" + (entry.getValue() * 100 / totalTime) + "%)");
    // }
    // } catch (Exception e) {
    // this.getServer().getLogger().info("Fail to print times");
    // }
    // }

    public static Underilla getPlugin() { return plugin; }
}
