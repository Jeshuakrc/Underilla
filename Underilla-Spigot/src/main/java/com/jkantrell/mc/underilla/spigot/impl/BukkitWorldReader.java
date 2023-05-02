package com.jkantrell.mc.underilla.spigot.impl;

import com.jkantrell.mc.underilla.core.reader.ChunkReader;
import com.jkantrell.mca.Chunk;

import java.io.File;

public class BukkitWorldReader extends com.jkantrell.mc.underilla.core.reader.WorldReader {

    //CONSTRUCTORS
    public BukkitWorldReader(String worldPath) throws NoSuchFieldException {
        super(worldPath);
    }
    public BukkitWorldReader(String worldPath, int cacheSize) throws NoSuchFieldException {
        super(worldPath, cacheSize);
    }
    public BukkitWorldReader(File worldDir) throws NoSuchFieldException {
        super(worldDir);
    }
    public BukkitWorldReader(File worldDir, int cacheSize) throws NoSuchFieldException {
        super(worldDir, cacheSize);
    }


    //IMPLEMENTATIONS
    @Override
    protected ChunkReader newChunkReader(Chunk chunk) {
        return new BukkitChunkReader(chunk);
    }
}
