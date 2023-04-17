package com.dotkntrell.mc.terraformer.io.reader;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.util.Vector;

import java.util.Optional;


public interface Reader {

    Optional<Material> materialAt(int x, int y, int z);
    default Optional<Material> materialAt(Vector vector) {
        return this.materialAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    Optional<Biome> biomeAt(int x, int y, int z);
    default Optional<Biome> biomeAt(Vector vector) {
        return this.biomeAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    default Optional<Biome> biomeAtCell(int x, int y, int z) {
        return this.biomeAt(x << 2, y << 2, z << 2);
    }
    default Optional<Biome> biomeAtCell(Vector vector) {
        return this.biomeAtCell(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

}
