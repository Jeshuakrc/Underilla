package com.dotkntrell.mc.underilla.io.reader;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public record LocatedMaterial(int x, int y, int z, Material material) {
    public Vector toVector() {
        return new Vector(x, y, z);
    }
}
