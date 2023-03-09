package com.dotkntrell.mc.terraformer.utils.coordinate;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CoordinateIterable implements Iterable<Coordinate> {

    //FIELDS
    private final int minX_, minY_, minZ_, maxX_, maxY_, maxZ_;

    //CONSTRUCTORS
    public CoordinateIterable(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.minX_ = minX; this.maxX_ = maxX;
        this.minY_ = minY; this.maxY_ = maxY;
        this.minZ_ = minZ; this.maxZ_ = maxZ;
    }
    public CoordinateIterable(int maxX, int maxY, int maxZ) {
        this(0, maxX, 0, maxY, 0, maxZ);
    }

    //OVERWRITES
    @Override
    public Iterator<Coordinate> iterator() {
        return new CoordinateIterator();
    }

    private class CoordinateIterator implements Iterator<Coordinate> {
        private int currentX_, currentY_, currentZ_;

        private CoordinateIterator() {
            this.currentX_ = CoordinateIterable.this.minX_;
            this.currentY_ = CoordinateIterable.this.minY_;
            this.currentZ_ = CoordinateIterable.this.minZ_;
        }

        @Override
        public boolean hasNext() {
            return this.currentY_ < CoordinateIterable.this.maxY_;
        }

        @Override
        public Coordinate next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            Coordinate r = new Coordinate(this.currentX_, this.currentY_, this.currentZ_);

            this.currentX_++;
            if (this.currentX_ >= CoordinateIterable.this.maxX_) {
                this.currentX_ = 0;
                this.currentZ_++;
            }
            if (this.currentZ_ >= CoordinateIterable.this.maxZ_) {
                this.currentZ_ = 0;
                this.currentY_++;
            }

            return r;
        }
    }
}