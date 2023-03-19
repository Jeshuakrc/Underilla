package com.dotkntrell.mc.terraformer.generation;

import com.dotkntrell.mc.terraformer.io.reader.Reader;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

class BlockSpreader {

    //FIELDS
    private Reader reader_;
    private List<Vector> initVectors_;
    private Predicate<Material> conditioner_ = m -> true;
    private BoundingBox container_ = null;
    private boolean[] entityMap_ = null;
    private int dimX_, dimY_, dimZ_, offsetX_, offsetY_, offsetZ_;
    private int iterations_;


    //CONSTRUCTORS
    BlockSpreader(List<Vector> initialVectors, Reader reader) {
        this.initVectors_ = initialVectors;
        this.reader_ = reader;
    }


    //SETTERS
    public BlockSpreader setCondition(Predicate<Material> condition) {
        this.conditioner_ = condition;
        return this;
    }
    public BlockSpreader setLimits(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.container_ = new BoundingBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        this.entityMap_ = new boolean[(int) this.container_.getVolume()];
        Arrays.fill(this.entityMap_, false);
        this.dimX_ = (int) this.container_.getWidthX();
        this.dimY_ = (int) this.container_.getHeight();
        this.dimZ_ = (int) this.container_.getWidthZ();
        this.offsetX_ = (int) this.container_.getMinX();
        this.offsetY_ = (int) this.container_.getMinY();
        this.offsetZ_ = (int) this.container_.getMinZ();
        return this;
    }
    public BlockSpreader setIterationsAmount(int amount) {
        this.iterations_ = amount;
        return this;
    }


    //UTIL
    public List<Vector> spread() {
        List<Entity> entities = new LinkedList<>();
        List<Entity> current = new LinkedList<>(), temp;
        this.initVectors_.stream().map(Entity::new).forEach(current::add);
        for (int i = 0; i < this.iterations_; i++) {
            temp = new LinkedList<>();
            for (Entity e : current) { e.spread(temp); }
            entities.addAll(current);
            current = temp;
        }
        return entities.stream().map(Entity::toVector).toList();
    }


    //PRIVATE UTIL
    private boolean getMap(int x, int y, int z) {
        return this.entityMap_[this.getMapIndex( x, y, z)];
    }
    private void setMap(int x, int y, int z, boolean b) {
        this.entityMap_[this.getMapIndex(x, y, z)] = b;
    }
    private int getMapIndex(int x, int y, int z) {
        x = x - this.offsetX_; y = y - this.offsetY_; z = z - this.offsetZ_;
        return (y * this.dimX_ * this.dimZ_) + (z * this.dimY_) + x;
    }


    //CLASSES
    private class Entity {

        private static final BlockFace[] FACES = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST };
        final int x_, y_, z_;

        Entity(int x, int y, int z) {
            this.x_ = x;
            this.y_ = y;
            this.z_ = z;
        }
        Entity(Vector vector) {
            this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        }

        void spread(List<Entity> target) {
            for (BlockFace f : BlockSpreader.Entity.FACES) {
                int     x = this.x_ + f.getModX(),
                        y = this.y_ + f.getModY(),
                        z = this.z_ + f.getModZ();

                if (!BlockSpreader.this.container_.contains(x + .5, y +.5, z + .5)) { continue; }
                if (BlockSpreader.this.getMap(x, y, z)) { continue; }
                if (!BlockSpreader.this.reader_.materialAt(x, y, z)
                        .map(BlockSpreader.this.conditioner_::test)
                        .orElse(false)
                ) { continue; }
                target.add(new Entity(x, y, z));
                BlockSpreader.this.setMap(x, y, z, true);
            }
        }
        Vector toVector() {
            return new Vector(this.x_, this.y_, this.z_);
        }
    }
}