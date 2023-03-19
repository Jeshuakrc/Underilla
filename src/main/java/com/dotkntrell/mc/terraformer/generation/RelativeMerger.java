package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.io.reader.ChunkReader;
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
import java.util.function.Predicate;

public class RelativeMerger implements Merger {

    //ASSETS
    private static final BlockFace[] SIDE_FACES = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };

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

        //Defining max height to read the chunk;
        int maxHeight;
        ChunkReader.Range airColumn = chunkReader.airColumn();
        if (airColumn != null) {
            Vector c1 = airColumn.corner1(), c2 = airColumn.corner2();
            chunkData.setRegion(c1.getBlockX(), c1.getBlockY(), c1.getBlockZ(), c2.getBlockX(), c2.getBlockY(), c2.getBlockZ(), Material.AIR);
            maxHeight = c1.getBlockY();
        } else {
            maxHeight = this.upperLimit_;
        }

        Vector  minVector = RelativeMerger.absoluteCoordinates(chunkX, chunkZ, new Vector(-this.depth_, worldInfo.getMinHeight(), -this.depth_)),
                maxVector = RelativeMerger.absoluteCoordinates(chunkX, chunkZ, new Vector(15 + this.depth_, maxHeight, 15 + this.depth_));
        List<Vector> toSpread = new LinkedList<>();

        BlockSetterIterable i = new BlockSetterIterable(this.worldReader_, chunkData, minVector.getBlockX(), maxVector.getBlockX(), minVector.getBlockY(), maxVector.getBlockY(), minVector.getBlockZ(), maxVector.getBlockZ());

        while (i.hasNextColumn()) {
            i.nextColumn();
            boolean inCHunk = RelativeMerger.isInChunk(chunkX, chunkZ, i.currentPosition());

            if (inCHunk) {
                i.doWhileInColumn(it -> !it.vanillaCurrent().isSolid(), BlockSetterIterable::set);
                if (!i.hasNextInColumn()) { continue; }
                if (i.current().isSolid()) {
                    toSpread.add(i.currentPosition());
                    continue;
                }
            }
            Consumer<BlockSetterIterable> action = inCHunk ? BlockSetterIterable::set : it -> {};
            i.doWhileInColumn(it -> !it.current().isSolid(), action.andThen(it -> {
                for (BlockFace f : RelativeMerger.SIDE_FACES) {
                    Vector v = it.currentPosition();
                    Optional<Material> m = this.worldReader_.materialAt(v.getBlockX() + f.getModX(), v.getBlockY(), v.getBlockZ() + f.getModZ());
                    if (m.map(Material::isSolid).orElse(false)) {
                        toSpread.add(it.currentPosition());
                        return;
                    }
                }
            }));
            if (i.hasNextInColumn()) { toSpread.add(i.currentPosition().add(BlockFace.UP.getDirection())); }
        }

        BlockSpreader spreader = new BlockSpreader(toSpread, this.worldReader_)
                .setLimits(minVector.getBlockX(), maxVector.getBlockX(), minVector.getBlockY(), maxVector.getBlockY(), minVector.getBlockZ(), maxVector.getBlockZ())
                .setCondition(m -> m.isSolid())
                .setIterationsAmount(this.depth_);
        spreader.spread().stream()
                .filter(v -> RelativeMerger.isInChunk(chunkX, chunkZ, v))
                .forEach(v ->
                        this.worldReader_.materialAt(v).ifPresent(m ->
                            chunkData.setBlock(Math.floorMod(v.getBlockX(),16), v.getBlockY(), Math.floorMod(v.getBlockZ(), 16), Material.IRON_BLOCK)
                ));
    }


    //PRIVATE UTIL
    private static Vector absoluteCoordinates(int chunkX, int chunkZ, Vector v) {
        v.setX((chunkX * 16) + v.getBlockX());
        v.setZ((chunkZ * 16) + v.getBlockZ());
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
