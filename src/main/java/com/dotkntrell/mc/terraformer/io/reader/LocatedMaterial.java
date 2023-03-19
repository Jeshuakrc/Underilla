package com.dotkntrell.mc.terraformer.io.reader;

import org.bukkit.Material;

public record LocatedMaterial(int x, int y, int z, Material material) {}
