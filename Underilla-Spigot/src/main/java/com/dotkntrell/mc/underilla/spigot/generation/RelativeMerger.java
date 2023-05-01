package com.dotkntrell.mc.underilla.spigot.generation;

import com.dotkntrell.mc.underilla.spigot.io.reader.ChunkReader;
import com.dotkntrell.mc.underilla.spigot.io.reader.LocatedMaterial;
import com.dotkntrell.mc.underilla.spigot.io.reader.WorldReader;
import com.dotkntrell.mc.underilla.spigot.util.VectorIterable;
import com.jkantrell.mca.MCAUtil;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.function.Function;

public class RelativeMerger implements Merger {

    //ASSETS
    private static final BlockFace[] SIDE_FACES = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };

    //FIELDS
    private final WorldReader worldReader_;
    private final int upperLimit_, lowerLimit_, depth_, blendRange_;
    private final List<Biome> keptBiomes_;


    //CONSTRUCTORS
    RelativeMerger(WorldReader worldReader, int upperLimit, int lowerLimit, int depth, int transitionRange, List<Biome> keptBiomes) {
        this.worldReader_ = worldReader;
        this.upperLimit_ = upperLimit;
        this.lowerLimit_ = lowerLimit;
        this.depth_ = depth;
        this.blendRange_ = transitionRange;
        this.keptBiomes_ = keptBiomes;
    }


    //OVERWRITES
    @Override
    public void mergeLand(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData) {

        //Extracting chunk
        ChunkReader chunk = this.worldReader_.readChunk(chunkX,chunkZ).orElse(null);
        if (chunk == null) { return; }

        //Getting rid of top air column (KEY FOR PERFORMANCE)
        int airColumn = Math.max(chunk.airColumnBottomHeight(), this.lowerLimit_);
        chunkData.setRegion(0, airColumn, 0, 16, chunkData.getMaxHeight(), 16, Material.AIR);

        //Getting all surrounding chunks from the base world
        List<ChunkReader> chunks = new ArrayList<>(9);
        for (BlockFace f : RelativeMerger.SIDE_FACES) {
            this.worldReader_.readChunk(chunkX + f.getModX(), chunkZ + f.getModZ()).ifPresent(chunks::add);
        }
        chunks.add(chunk);

        //Getting all non solid blocks
        List<Vector> fillVectors = new LinkedList<>();
        for (ChunkReader c : chunks) {
            c.locationsOf(m -> !m.isSolid(), airColumn, chunkData.getMinHeight()).stream()      //Constraining the height up to the air column is also key for performance
                    .map(LocatedMaterial::toVector)
                    .map(v -> RelativeMerger.absoluteCoordinates(c.getX(), c.getZ(), v))
                    .forEach(fillVectors::add);
        }

        //Defining spreader constrains
        Vector  minVector = RelativeMerger.absoluteCoordinates(chunkX, chunkZ, new Vector(-this.depth_, chunkData.getMinHeight(), -this.depth_)),
                maxVector = RelativeMerger.absoluteCoordinates(chunkX, chunkZ, new Vector(15 + this.depth_, chunkData.getMaxHeight(), 15 + this.depth_));
        Spreader spreader = new Spreader()
                .setContainer(minVector, maxVector)
                .setIterationsAmount(this.depth_);

        //Getting rid of all non-used vectors
        fillVectors.removeIf(v -> v.getBlockY() < this.lowerLimit_ || !spreader.contains(v));

        //Including everything above upper limit, and any non-solid block over vanilla noise surface.
        Function<Vector, Material> materialGetter = v -> chunkData.getType(Math.floorMod(v.getBlockX(), 16), v.getBlockY(), Math.floorMod(v.getBlockZ(), 16));
        VectorIterable i = new VectorIterable(minVector.getBlockX(), maxVector.getBlockX(), this.lowerLimit_, airColumn, minVector.getBlockZ(), maxVector.getBlockZ());
        while (i.hasNextColumn()) {
            Vector v = i.nextColumn();
            fillVectors.add(v);
            //if (i.hasNext()) { v = i.next(); }
            if (!RelativeMerger.isInChunk(chunkX, chunkZ, v)) { continue; }
            while (!materialGetter.apply(v).isSolid() || this.upperLimit_ < v.getBlockY()) {
                fillVectors.add(v);
                if (!i.hasNextInColumn()) { break; }
                v = i.next();
            }
        }

        //Spreading filler vectors
        fillVectors = spreader.setRootVectors(fillVectors).spread();

        fillVectors.stream()
                //Cutting everything out of the actual chunk
                .filter(v -> RelativeMerger.isInChunk(chunkX, chunkZ, v))
                //Placing base-world blocks over valina world
                .forEach(v -> {
                    v = RelativeMerger.relativeCoordinates(v);
                    BlockData d = chunk.blockAt(v).orElse(null);
                    if (d == null) { return; }
                    chunkData.setBlock(v.getBlockX(), v.getBlockY(), v.getBlockZ(), d);
                });

    }
    @Override
    public void mergeBiomes(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, AugmentedChunkData chunkData) {
        //Extracting chunk
        ChunkReader chunk = this.worldReader_.readChunk(chunkX,chunkZ).orElse(null);
        if (chunk == null) { return; }

        //Extracting all non-solid blocks from base chunk, and trunking them into 4x4x4 biome cells.
        List<Vector> cells = chunk.locationsOf(m -> !m.isSolid()).stream()
                .map(LocatedMaterial::toVector)
                .peek(v -> {
                    v.setX(v.getBlockX() >> 2);
                    v.setY(v.getBlockY() >> 2);
                    v.setZ(v.getBlockZ() >> 2);
                })
                .distinct()
                .toList();

        //Using a spreader to map cells in a 3D grid
        Spreader spreader = new Spreader()
                .setContainer(0, chunkData.getMinHeight() >> 2, 0, 3, (chunkData.getMaxHeight() >> 2) - 1, 3)
                .setRootVectors(cells)
                .setIterationsAmount(1);
        spreader.spread();

        //Looping through every cell
        VectorIterable i = new VectorIterable(0, 4, chunkData.getMinHeight() >> 2, chunkData.getMaxHeight() >> 2, 0, 4);
        for (Vector v : i) {
            Biome b;
            if (!spreader.isPresent(v)) {
                int     x = v.getBlockX() << 2,
                        y = v.getBlockY() << 2,
                        z = v.getBlockZ() << 2;
                b = chunkData.getBiome(x, y, z);
                if (this.keptBiomes_.contains(b)) { continue; }
            }
            b = chunk.biomeAtCell(v).orElse(null);
            if (b == null) { continue; }
            chunkData.setBiome(v.getBlockX(), v.getBlockY(), v.getBlockZ(), b);
        }
    }


    //PRIVATE UTIL
    private static Vector absoluteCoordinates(int chunkX, int chunkZ, Vector v) {
        v.setX((chunkX * 16) + v.getBlockX());
        v.setZ((chunkZ * 16) + v.getBlockZ());
        return v;
    }
    private static Vector relativeCoordinates(Vector v) {
        v.setX(Math.floorMod(v.getBlockX(), 16));
        v.setZ(Math.floorMod(v.getBlockZ(), 16));
        return v;
    }
    private static boolean isInChunk(int chunkX, int chunkZ, Vector v) {
        boolean x = MCAUtil.blockToChunk(v.getBlockX()) == chunkX,
                z = MCAUtil.blockToChunk(v.getBlockZ()) == chunkZ;
        return x && z;
    }
}
