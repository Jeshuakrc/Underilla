package com.jkantrell.mc.underilla.spigot.impl;

import com.jkantrell.mc.underilla.core.api.Biome;

public class BukkitBiome implements Biome {

    //FIELDS
    private org.bukkit.block.Biome biome_;


    //CONSTRUCTORS
    public BukkitBiome(org.bukkit.block.Biome biome) {
        this.biome_ = biome;
    }


    //GETTERS
    public org.bukkit.block.Biome getBiome() {
        return this.biome_;
    }


    //IMPLEMENTATIONS
    @Override
    public String getName() {
        return this.biome_.name();
    }
    @Override
    public boolean equals(Object o) {
        if (o == null) { return false; }
        if (this == o) { return true; }
        if (!(o instanceof BukkitBiome bukkitBiome)) { return false; }
        return this.biome_.equals(bukkitBiome.biome_);
    }
}
