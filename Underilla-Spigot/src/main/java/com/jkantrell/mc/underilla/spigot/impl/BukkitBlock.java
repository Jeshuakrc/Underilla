package com.jkantrell.mc.underilla.spigot.impl;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import com.jkantrell.mc.underilla.core.api.Block;

public class BukkitBlock implements Block {

    // FIELDS
    private BlockData blockData_;


    // CONSTRUCTORS
    public BukkitBlock(BlockData blockData) { this.blockData_ = blockData; }


    // GETTERS
    public BlockData getBlockData() { return this.blockData_; }

    // IMPLEMENTATIONS
    @Override
    public boolean isAir() { return this.blockData_.getMaterial().isAir(); }

    @Override
    public boolean isSolid() { return this.blockData_.getMaterial().isSolid(); }

    @Override
    public boolean isLiquid() {
        Material m = this.blockData_.getMaterial();
        return m.equals(Material.WATER) || m.equals(Material.LAVA) || isWaterLogged();
    }

    @Override
    public boolean isWaterloggable() { return this.blockData_ instanceof Waterlogged; }

    public boolean isWaterLogged() { return this.blockData_ instanceof Waterlogged waterlogged && waterlogged.isWaterlogged(); }

    @Override
    public void waterlog() {
        if (this.isAir()) {
            this.blockData_ = Material.WATER.createBlockData();
            return;
        }
        if (!(this.blockData_ instanceof Waterlogged waterlogged)) {
            return;
        }
        waterlogged.setWaterlogged(true);
        this.blockData_ = waterlogged;
    }

    @Override
    public String getName() { return this.blockData_.getMaterial().toString().toLowerCase(); }

    @Override
    public String getNameSpace() { return null; }
}
