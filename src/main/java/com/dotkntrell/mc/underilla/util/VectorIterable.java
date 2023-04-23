package com.dotkntrell.mc.underilla.util;

import org.bukkit.util.Vector;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class VectorIterable implements Iterable<Vector>, Iterator<Vector> {

    //FIELDS
    private final int minX_, minY_, minZ_, maxX_, maxY_, maxZ_;
    private int currX_, currY_, currZ_;

    //CONSTRUCTORS
    public VectorIterable(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.minX_ = minX; this.maxX_ = maxX;
        this.minY_ = minY; this.maxY_ = maxY;
        this.minZ_ = minZ; this.maxZ_ = maxZ;
        this.currX_ = this.maxX_ - 1;
        this.currZ_ = this.minZ_ - 1;
        this.currY_ = this.minY_;
    }
    public VectorIterable(int maxX, int maxY, int maxZ) {
        this(0, maxX, 0, maxY, 0, maxZ);
    }
    public VectorIterable(Vector maxVector) {
        this(maxVector.getBlockX(), maxVector.getBlockY(), maxVector.getBlockZ());
    }
    public VectorIterable(Vector minVector, Vector maxVector) {
        this(minVector.getBlockX(), minVector.getBlockY(), minVector.getBlockZ(), maxVector.getBlockX(), maxVector.getBlockY(), maxVector.getBlockZ());
    }

    //OVERWRITES
    @Override
    public Iterator<Vector> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return hasNextColumn() || this.currY_ > this.minY_;
    }
    public boolean hasNextColumn() {
        return (this.currZ_ < this.maxZ_ - 1) || (this.currX_ < this.maxX_ - 1);
    }
    public boolean hasNextInColumn() {
        return this.currY_ > this.minY_;
    }

    @Override
    public Vector next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.moveForward();
    }
    public Vector nextColumn() {
        if (!this.hasNextColumn()) {
            throw new NoSuchElementException();
        }
        this.currY_ = this.minY_;
        return this.moveForward();
    }


    //PRIVATE UTIL
    private Vector moveForward() {
        this.currY_--;
        if (this.currY_ < VectorIterable.this.minY_) {
            this.currY_ = this.maxY_ - 1;
            this.currX_++;
        }
        if (this.currX_ >= VectorIterable.this.maxX_) {
            this.currX_ = this.minX_;
            this.currZ_++;
        }
        return new Vector(this.currX_, this.currY_, this.currZ_);
    }
}