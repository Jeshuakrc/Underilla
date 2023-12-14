package com.jkantrell.mc.underilla.core.generation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import com.jkantrell.mc.underilla.core.reader.ChunkReader;
import com.jkantrell.mc.underilla.core.reader.WorldReader;
import com.jkantrell.mc.underilla.core.vector.Direction;
import com.jkantrell.mc.underilla.core.vector.IntVector;
import com.jkantrell.mc.underilla.core.vector.LocatedBlock;
import com.jkantrell.mc.underilla.core.vector.Vector;
import com.jkantrell.mc.underilla.core.vector.VectorIterable;
import com.jkantrell.mca.MCAUtil;

public class RelativeMerger implements Merger {

    // ASSETS
    private static final Direction[] DIRECTIONS = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, Direction.NORTH_EAST,
            Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.SOUTH_WEST};


    // FIELDS
    private final WorldReader worldReader_;
    private final int upperLimit_, lowerLimit_, depth_, blendRange_;
    private final List<? extends Biome> keptBiomes_, preserveBiomes_;
    private final boolean keepReferenceWorldBlocks_;
    private final List<String> keptReferenceWorldBlocks_;


    // CONSTRUCTORS
    RelativeMerger(WorldReader worldReader, int upperLimit, int lowerLimit, int depth, int transitionRange,
            List<? extends Biome> keptBiomes, List<? extends Biome> preservedBiomes, List<String> keptReferenceWorldBlocks) {
        this.worldReader_ = worldReader;
        this.upperLimit_ = upperLimit;
        this.lowerLimit_ = lowerLimit;
        this.depth_ = depth;
        this.blendRange_ = transitionRange;
        this.keptBiomes_ = keptBiomes;
        this.preserveBiomes_ = preservedBiomes;
        this.keepReferenceWorldBlocks_ = !keptReferenceWorldBlocks.isEmpty();
        this.keptReferenceWorldBlocks_ = keptReferenceWorldBlocks;
    }


    // OVERWRITES
    @Override
    public void merge(ChunkReader reader, ChunkData chunkData) {
        this.mergeLand(reader, chunkData);
        this.mergeBiomes(reader, chunkData);
    }
    @Override
    public void mergeLand(ChunkReader reader, ChunkData chunkData) {
        Function<Vector<Integer>, Block> blockGetter = v -> chunkData.getBlock(RelativeMerger.relativeCoordinates(v.clone()));

        // Getting rid of top air column (KEY FOR PERFORMANCE)
        int airColumn = Math.max(reader.airSectionsBottom(), this.lowerLimit_);
        Block airBlock = reader.blockFromTag(MCAUtil.airBlockTag()).get();
        chunkData.setRegion(0, airColumn, 0, 16, chunkData.getMaxHeight(), 16, airBlock);

        // Getting all surrounding chunks from the base world
        List<ChunkReader> chunks = new ArrayList<>(9);
        int chunkX = reader.getX(), chunkZ = reader.getZ();
        for (Direction d : RelativeMerger.DIRECTIONS) {
            this.worldReader_.readChunk(chunkX + d.x(), chunkZ + d.z()).ifPresent(chunks::add);
        }
        chunks.add(reader);

        // Getting all non solid blocks (and ores)
        List<Vector<Integer>> fillVectors = new LinkedList<>();
        for (ChunkReader c : chunks) {
            if (keepReferenceWorldBlocks_) {
                c.locationsOf(m -> !m.isSolid() || isCustomWorldOreOutOfVanillaCaves(m, null), airColumn, chunkData.getMinHeight()).stream()
                        // Constraining the height up to the air column is also key for performance
                        .filter(m -> !m.value().isSolid()
                                || isCustomWorldOreOutOfVanillaCaves(m.value(), blockGetter.apply(((LocatedBlock) m).vector())))
                        .map(LocatedBlock::vector).map(v -> RelativeMerger.absoluteCoordinates(c.getX(), c.getZ(), v))
                        .forEach(fillVectors::add);
            } else {
                c.locationsOf(m -> !m.isSolid(), airColumn, chunkData.getMinHeight()).stream() // Constraining the height up to the air
                                                                                               // column is also key for performance
                        .map(LocatedBlock::vector).map(v -> RelativeMerger.absoluteCoordinates(c.getX(), c.getZ(), v))
                        .forEach(fillVectors::add);
            }
        }

        // Defining spreader constrains
        Vector<Integer> minVector = RelativeMerger.absoluteCoordinates(chunkX, chunkZ,
                new IntVector(-this.depth_, chunkData.getMinHeight(), -this.depth_)),
                maxVector = RelativeMerger.absoluteCoordinates(chunkX, chunkZ,
                        new IntVector(15 + this.depth_, chunkData.getMaxHeight(), 15 + this.depth_));
        Spreader spreader = new Spreader().setContainer(minVector, maxVector).setIterationsAmount(this.depth_);

        // Getting rid of all non-used vectors
        fillVectors.removeIf(v -> v.y() < this.lowerLimit_ || !spreader.contains(v));
        // fillVectors.removeIf(v -> (v.y() < this.lowerLimit_ || !spreader.contains(v)) && !isPreservedBiome(reader, v));

        // Including everything above upper limit, and any non-solid block over vanilla noise surface.
        VectorIterable i = new VectorIterable(minVector.x(), maxVector.x(), this.lowerLimit_, airColumn + 1, minVector.z(), maxVector.z());
        while (i.hasNextColumn()) {
            Vector<Integer> v = i.nextColumn();
            fillVectors.add(v);
            // if (i.hasNext()) { v = i.next(); }
            if (!RelativeMerger.isInChunk(chunkX, chunkZ, v)) {
                continue;
            }
            boolean isPreservedBiome = isPreservedBiome(reader, v);
            while (!blockGetter.apply(v).isSolid() || this.upperLimit_ < v.y() || isPreservedBiome) {
                fillVectors.add(v);
                if (!i.hasNextInColumn()) {
                    break;
                }
                v = i.next();
            }
        }

        // Spreading filler vectors
        fillVectors = spreader.setRootVectors(fillVectors).spread();

        fillVectors.stream()
                // Cutting everything out of the actual chunk
                .filter(v -> RelativeMerger.isInChunk(chunkX, chunkZ, v))
                // Placing base-world blocks over valina world
                .forEach(v -> {
                    v = RelativeMerger.relativeCoordinates(v);
                    Block b = reader.blockAt(v).orElse(null);
                    if (b == null) {
                        return;
                    }
                    chunkData.setBlock(v.x(), v.y(), v.z(), b);
                });

    }
    /** Return true if this biome need to be only custom world */
    private boolean isPreservedBiome(ChunkReader reader, Vector v) {
        return this.preserveBiomes_.contains(reader.biomeAt(relativeCoordinates(v)).orElse(null));
    }
    /**
     * Return true if this block is a block to preserve from the custom world and a solid block in vanilla world (This avoid to have ores
     * floating in the air in vanilla caves).
     * 
     * @param b            the block to check if is ore
     * @param vanillaBlock the block in vanilla world
     */
    private boolean isCustomWorldOreOutOfVanillaCaves(Block b, Block vanillaBlock) {
        return keptReferenceWorldBlocks_.contains(b.getName().toUpperCase()) && (vanillaBlock == null || vanillaBlock.isSolid());
    }

    @Override
    public void mergeBiomes(ChunkReader reader, ChunkData chunkData) {

        // Extracting all non-solid blocks from base chunk, and trunking them into 4x4x4 biome cells.
        List<Vector<Integer>> cells = reader.locationsOf(m -> !m.isSolid()).stream().map(LocatedBlock::vector).peek(v -> {
            v.setX(v.x() >> 2);
            v.setY(v.y() >> 2);
            v.setZ(v.z() >> 2);
        }).distinct().toList();

        // Using a spreader to map cells in a 3D grid
        Spreader spreader = new Spreader().setContainer(0, chunkData.getMinHeight() >> 2, 0, 3, (chunkData.getMaxHeight() >> 2) - 1, 3)
                .setRootVectors(cells).setIterationsAmount(1);
        spreader.spread();

        // Looping through every cell
        VectorIterable i = new VectorIterable(0, 4, chunkData.getMinHeight() >> 2, chunkData.getMaxHeight() >> 2, 0, 4);
        for (Vector<Integer> v : i) {
            Biome b;
            if (!spreader.isPresent(v)) {
                int x = v.x() << 2, y = v.y() << 2, z = v.z() << 2;
                b = chunkData.getBiome(x, y, z);
                if (this.keptBiomes_.contains(b)) {
                    continue;
                }
            }
            b = reader.biomeAtCell(v).orElse(null);
            if (b == null) {
                continue;
            }
            chunkData.setBiome(v, b);
        }
    }


    // PRIVATE UTIL
    private static Vector<Integer> absoluteCoordinates(int chunkX, int chunkZ, Vector<Integer> v) {
        v.addX(chunkX * 16);
        v.addZ(chunkZ * 16);
        return v;
    }
    private static Vector<Integer> relativeCoordinates(Vector<Integer> v) {
        v.setX(Math.floorMod(v.x(), 16));
        v.setZ(Math.floorMod(v.z(), 16));
        return v;
    }
    private static boolean isInChunk(int chunkX, int chunkZ, Vector<Integer> v) {
        boolean x = MCAUtil.blockToChunk(v.x()) == chunkX, z = MCAUtil.blockToChunk(v.z()) == chunkZ;
        return x && z;
    }
}
