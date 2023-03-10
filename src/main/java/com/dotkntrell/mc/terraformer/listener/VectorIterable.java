package com.dotkntrell.mc.terraformer.listener;

import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class VectorIterable implements Iterable<Vector>, Iterator<Vector> {

    //FIELDS
    private final int maxX_, maxY_, maxZ_;
    private int currX_, currY_, currZ_;

    //CONSTRUCTORS
    public VectorIterable(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.currX_ = minX; this.maxX_ = maxX;
        this.currY_ = minY; this.maxY_ = maxY;
        this.currZ_ = minZ; this.maxZ_ = maxZ;
    }
    public VectorIterable(int maxX, int maxY, int maxZ) {
        this(0, maxX, 0, maxY, 0, maxZ);
    }

    //OVERWRITES
    @Override
    public Iterator<Vector> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.currY_ < VectorIterable.this.maxY_;
    }

    @Override
    public Vector next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        Vector r = new Vector(this.currX_, this.currY_, this.currZ_);

        this.currX_++;
        if (this.currX_ >= VectorIterable.this.maxX_) {
            this.currX_ = 0;
            this.currZ_++;
        }
        if (this.currZ_ >= VectorIterable.this.maxZ_) {
            this.currZ_ = 0;
            this.currY_++;
        }

        return r;
    }
}