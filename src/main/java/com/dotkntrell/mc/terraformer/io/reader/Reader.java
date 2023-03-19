package com.dotkntrell.mc.terraformer.io.reader;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Optional;


public interface Reader {

    Optional<Material> materialAt(int x, int y, int z);
    default Optional<Material> materialAt(Vector vector) {
        return this.materialAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

}
