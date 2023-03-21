package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.io.reader.ChunkReader;
import com.dotkntrell.mc.terraformer.io.reader.LocatedMaterial;
import com.dotkntrell.mc.terraformer.io.reader.Reader;
import com.dotkntrell.mc.terraformer.io.reader.WorldReader;
import com.dotkntrell.mc.terraformer.util.VectorIterable;
import com.jkantrell.mca.MCAUtil;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RelativeMerger implements Merger {

    //ASSETS
    private static final BlockFace[] SIDE_FACES = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };

    //FIELDS
    private final WorldReader worldReader_;
    private final int upperLimit_, lowerLimit_, depth_, blendRange_;


    //CONSTRUCTORS
    RelativeMerger(WorldReader worldReader, int upperLimit, int lowerLimit, int depth, int transitionRange) {
        this.worldReader_ = worldReader;
        this.upperLimit_ = upperLimit;
        this.lowerLimit_ = lowerLimit;
        this.depth_ = depth;
        this.blendRange_ = transitionRange;
    }


    //OVERWRITES
    @Override
    public void merge(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData, ChunkReader chunkReader) {

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
                    Material m = chunk.materialAt(v).orElse(null);
                    if (m == null) { return; }
                    chunkData.setBlock(v.getBlockX(), v.getBlockY(), v.getBlockZ(), m);
                });

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


    //CLASSES
    private static class BlockSetterIterable implements Iterable<Material>, Iterator<Material> {

        //FIELDS
        private final Reader reader_;
        private final ChunkGenerator.ChunkData chunkData_;
        private final VectorIterable iterable_;
        private Material currMaterial_;
        private Vector currVector_;


        //CONSTRUCTOR
        BlockSetterIterable(Reader reader, ChunkGenerator.ChunkData chunkData, VectorIterable iterable) {
            this.reader_ = reader;
            this.iterable_ = iterable;
            this.chunkData_ = chunkData;
        }
        BlockSetterIterable(Reader reader, ChunkGenerator.ChunkData chunkData, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this(reader, chunkData, new VectorIterable(minX, maxX, minY, maxY, minZ, maxZ));
        }


        @Override
        public boolean hasNext() {
            return this.iterable_.hasNext();
        }

        public boolean hasNextColumn() {
            return this.iterable_.hasNextColumn();
        }

        public boolean hasNextInColumn() {
            return this.iterable_.hasNextInColumn();
        }

        @Override
        public Material next() {
            this.currVector_ = this.iterable_.next();
            return this.setCurrent();
        }

        public Material nextColumn() {
            this.currVector_ = this.iterable_.nextColumn();
            return this.setCurrent();
        }

        public Material current() {
            return this.currMaterial_;
        }

        public Material vanillaCurrent() {
            Vector v = this.currVector_;
            return this.chunkData_.getType(Math.floorMod(v.getBlockX(), 16), v.getBlockY(), Math.floorMod(v.getBlockZ(), 16));
        }

        public Vector currentPosition() {
            return this.currVector_;
        }

        public void set() {
            Vector v = this.currVector_;
            this.chunkData_.setBlock(Math.floorMod(v.getBlockX(), 16), v.getBlockY(), Math.floorMod(v.getBlockZ(), 16), this.currMaterial_);
        }

        private Material setCurrent() {
            Vector v = this.currVector_;
            this.currMaterial_ = this.reader_.materialAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()).orElse(Material.AIR);
            return this.currMaterial_;
        }

        public void doWhileInColumn(Predicate<BlockSetterIterable> check, Consumer<BlockSetterIterable> action) {
            while (check.test(this)) {
                action.accept(this);
                if (!this.hasNextInColumn()) { break; }
                next();
            }
        }

        @Override
        public Iterator<Material> iterator() {
            return this;
        }
    }

}
