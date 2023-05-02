package com.jkantrell.mc.underilla.spigot.impl;

import com.jkantrell.mc.underilla.core.api.Block;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

public class BukkitBlock implements Block {

    //FIELDS
    private BlockData blockData_;


    //CONSTRUCTORS
    public BukkitBlock(BlockData blockData) {
        this.blockData_ = blockData;
    }


    //GETTERS
    public BlockData getBlockData() {
        return this.blockData_;
    }

    //IMPLEMENTATIONS
    @Override
    public boolean isAir() {
        return this.blockData_.getMaterial().isAir();
    }

    @Override
    public boolean isSolid() {
        return this.blockData_.getMaterial().isSolid();
    }

    @Override
    public boolean isLiquid() {
        Material m = this.blockData_.getMaterial();
        return m.equals(Material.WATER) || m.equals(Material.LAVA);
    }

    @Override
    public boolean isWaterloggable() {
        return this.blockData_ instanceof Waterlogged;
    }

    @Override
    public void waterlog() {
        if (this.isAir()) {
            this.blockData_ = Material.AIR.createBlockData();
            return;
        }
        if (!(this.blockData_ instanceof Waterlogged waterlogged)) { return; }
        waterlogged.setWaterlogged(true);
        this.blockData_ = waterlogged;
    }

    @Override
    public String getName() {
        return this.blockData_.getMaterial().toString().toLowerCase();
    }

    @Override
    public String getNameSpace() {
        return null;
    }
}
