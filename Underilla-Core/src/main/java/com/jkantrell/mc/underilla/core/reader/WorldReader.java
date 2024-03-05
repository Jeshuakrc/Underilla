package com.jkantrell.mc.underilla.core.reader;

import java.io.File;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mca.Chunk;
import com.jkantrell.mca.MCAFile;
import com.jkantrell.mca.MCAUtil;

public abstract class WorldReader implements Reader {

    // CONSTANTS
    private static final String REGION_DIRECTORY = "region";

    // FIELDS
    private final File world_;
    private final File regions_;
    private final RLUCache<MCAFile> regionCache_;
    private final RLUCache<ChunkReader> chunkCache_;


    // CONSTRUCTORS
    public WorldReader(String worldPath) throws NoSuchFieldException { this(new File(worldPath)); }
    public WorldReader(String worldPath, int cacheSize) throws NoSuchFieldException { this(new File(worldPath), cacheSize); }
    public WorldReader(File worldDir) throws NoSuchFieldException { this(worldDir, 16); }
    public WorldReader(File worldDir, int cacheSize) throws NoSuchFieldException {
        if (!(worldDir.exists() && worldDir.isDirectory())) {
            throw new NoSuchFieldException("World directory '" + worldDir.getPath() + "' does not exist.");
        }
        File regionDir = new File(worldDir, WorldReader.REGION_DIRECTORY);
        if (!(regionDir.exists() && regionDir.isDirectory())) {
            throw new NoSuchFieldException("World '" + worldDir.getName() + "' doesn't have a 'region' directory.");
        }
        this.world_ = worldDir;
        this.regions_ = regionDir;
        this.regionCache_ = new RLUCache<>(cacheSize);
        this.chunkCache_ = new RLUCache<>(cacheSize * 8);
    }


    // GETTERS
    public String getWorldName() { return this.world_.getName(); }


    // UTIL
    @Override
    public Optional<Block> blockAt(int x, int y, int z) {
        int chunkX = MCAUtil.blockToChunk(x), chunkZ = MCAUtil.blockToChunk(z);
        return this.readChunk(chunkX, chunkZ).flatMap(c -> c.blockAt(Math.floorMod(x, 16), y, Math.floorMod(z, 16)));
    }
    @Override
    public Optional<Biome> biomeAt(int x, int y, int z) {
        int chunkX = MCAUtil.blockToChunk(x), chunkZ = MCAUtil.blockToChunk(z);
        return this.readChunk(chunkX, chunkZ).flatMap(c -> c.biomeAt(Math.floorMod(x, 16), y, Math.floorMod(z, 16)));
    }
    public Optional<ChunkReader> readChunk(int x, int z) {
        // This is the step were we read the chunk from the region file.
        // by setting the x and z to 0, we are able to read only 1 chunk.
        // x = 0;
        // z = 0;
        ChunkReader chunkReader = this.chunkCache_.get(x, z);
        if (chunkReader != null) {
            return Optional.of(chunkReader);
        }
        MCAFile r = this.readRegion(x >> 5, z >> 5);
        if (r == null) {
            return Optional.empty();
        }
        Chunk chunk = r.getChunk(Math.floorMod(x, 32), Math.floorMod(z, 32));
        if (chunk == null) {
            return Optional.empty();
        }
        chunkReader = this.newChunkReader(chunk);
        this.chunkCache_.put(x, z, chunkReader);
        return Optional.of(chunkReader);
    }


    // ABSTRACT
    protected abstract ChunkReader newChunkReader(Chunk chunk);


    // PRIVATE UTIL
    private MCAFile readRegion(int x, int z) {
        MCAFile region = this.regionCache_.get(x, z);
        if (region != null) {
            return region;
        }
        File regionFile = new File(this.regions_, "r." + x + "." + z + ".mca");
        if (!regionFile.exists()) {
            return null;
        }
        try {
            region = MCAUtil.read(regionFile);
            this.regionCache_.put(x, z, region);
            return region;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // CLASSES
    private static class RLUCache<T> {

        // FIELDS
        private final Map<Pair<Integer, Integer>, T> map_ = new HashMap<>();
        private final Deque<Pair<Integer, Integer>> queue_ = new LinkedList<>();
        private final int capacity_;


        // CONSTRUCTOR
        RLUCache(int capacity) { this.capacity_ = capacity; }


        // UTIL
        // We synchronized the methode to avoid concurrent access to the cache.
        // Concurrent access cause queue_ and map_ to grow without never being reduced.
        // We might win few ms by reducing the part of the code that is synchronized, but I don't think it's worth the potential bugs.
        T get(int x, int z) {
            Pair<Integer, Integer> pair = ImmutablePair.of(x, z);
            synchronized (this) {
                T cached = this.map_.get(pair);
                if (cached == null) {
                    return null;
                }
                this.queue_.remove(pair);
                this.queue_.addFirst(pair);
                return cached;
            }
        }
        void put(int x, int z, T file) {
            Pair<Integer, Integer> pair = ImmutablePair.of(x, z);
            synchronized (this) {
                if (map_.containsKey(pair)) {
                    this.queue_.remove(pair);
                } else if (this.queue_.size() >= this.capacity_) {
                    try {
                        Pair<Integer, Integer> temp = this.queue_.removeLast();
                        this.map_.remove(temp);
                    } catch (NoSuchElementException ignored) {}
                }
                this.map_.put(pair, file);
                this.queue_.addFirst(pair);
            }
        }
    }
}
