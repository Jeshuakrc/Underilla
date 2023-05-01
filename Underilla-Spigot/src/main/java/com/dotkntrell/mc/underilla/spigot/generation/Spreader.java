package com.dotkntrell.mc.underilla.spigot.generation;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

class Spreader {

    //FIELDS
    private List<Vector> initVectors_;
    private boolean[] entityMap_ = null;
    private int minX_, maxX_, minY_, maxY_, minZ_, maxZ_, dimX_, dimY_, dimZ_;
    private int iterations_ = 0;


    //SETTERS
    public Spreader setRootVectors(Collection<Vector> vectors) {
        this.initVectors_ = new LinkedList<>(vectors);
        return this;
    }
    public Spreader setContainer(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX_ = Math.min(x1, x2);
        this.minY_ = Math.min(y1, y2);
        this.minZ_ = Math.min(z1, z2);

        this.maxX_ = Math.max(x1, x2);
        this.maxY_ = Math.max(y1, y2);
        this.maxZ_ = Math.max(z1, z2);

        this.dimX_ = this.maxX_ - this.minX_ + 1;
        this.dimY_ = this.maxY_ - this.minY_ + 1;
        this.dimZ_ = this.maxZ_ - this.minZ_ + 1;

        this.entityMap_ = new boolean[this.dimX_ * this.dimY_ * this.dimZ_];
        return this;
    }
    public Spreader setContainer(Vector corner1, Vector corner2) {
        return this.setContainer(corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ(), corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ());
    }
    public Spreader setIterationsAmount(int amount) {
        this.iterations_ = amount;
        return this;
    }


    //UTIL
    public List<Vector> spread() {
        List<Entity> entities = new LinkedList<>();
        List<Entity> current = new LinkedList<>(), temp;
        for (Vector v : this.initVectors_) {
            if (!this.contains(v)) { continue; }
            try {
                current.add(new Entity(v));
            } catch (IllegalStateException ignored) {}
        }
        for (int i = 0; i < this.iterations_; i++) {
            temp = new LinkedList<>();
            for (Entity e : current) { e.spread(temp); }
            entities.addAll(current);
            current = temp;
        }
        return entities.stream().map(Entity::toVector).toList();
    }
    public boolean contains(int x, int y, int z) {
        boolean inX = this.minX_ <= x && x <= this.maxX_;
        boolean inY = this.minY_ <= y && y <= this.maxY_;
        boolean inZ = this.minZ_ <= z && z <= this.maxZ_;
        return inX && inY && inZ;
    }
    public boolean contains(Vector vector) {
        return this.contains(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    public boolean isPresent(int x, int y, int z) {
        if (!this.contains(x, y, z)) { return false; }
        return this.getMap(x, y, z);
    }
    public boolean isPresent(Vector vector) {
        return this.isPresent(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }


    //PRIVATE UTIL
    private boolean getMap(int x, int y, int z) {
        return this.entityMap_[this.getMapIndex( x, y, z)];
    }
    private void setMap(int x, int y, int z) {
        this.entityMap_[this.getMapIndex(x, y, z)] = true;
    }
    private int getMapIndex(int x, int y, int z) {
        x = x - this.minX_; y = y - this.minY_; z = z - this.minZ_;
        return (y * this.dimX_ * this.dimZ_) + (z * this.dimX_) + x;
    }


    //CLASSES
    private class Entity {

        private static final BlockFace[] FACES = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST };
        final int x_, y_, z_;

        Entity(int x, int y, int z) {
            if (Spreader.this.getMap(x, y, z)) {
                throw new IllegalStateException();
            }
            this.x_ = x;
            this.y_ = y;
            this.z_ = z;
            Spreader.this.setMap(this.x_, this.y_, this.z_);
        }
        Entity(Vector vector) {
            this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        }

        void spread(List<Entity> target) {
            for (BlockFace f : Spreader.Entity.FACES) {
                int     x = this.x_ + f.getModX(),
                        y = this.y_ + f.getModY(),
                        z = this.z_ + f.getModZ();

                if (!Spreader.this.contains(x, y, z)) { continue; }
                if (Spreader.this.getMap(x, y, z)) { continue; }
                target.add(new Entity(x, y, z));
            }
        }
        Vector toVector() {
            return new Vector(this.x_, this.y_, this.z_);
        }
    }
}