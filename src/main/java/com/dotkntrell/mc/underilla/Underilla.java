package com.dotkntrell.mc.underilla;

import com.dotkntrell.mc.underilla.generation.UnderillaChunkGenerator;
import com.dotkntrell.mc.underilla.io.Config;
import com.dotkntrell.mc.underilla.io.reader.WorldReader;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.FileNotFoundException;

public final class Underilla extends JavaPlugin {

    public static final Config CONFIG = new Config("");
    private WorldReader worldReader_ = null;

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
            this.worldReader_ = new WorldReader(Underilla.CONFIG.referenceWorldName);
            this.getServer().getLogger().info("World + '" + Underilla.CONFIG.referenceWorldName + "' found.");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
