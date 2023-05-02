package com.jkantrell.mc.underilla.core.vector;

public enum Direction {
    
    //CONSTANTS
    NORTH(0, 0, -1),
    EAST(1, 0, 0),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    NORTH_EAST(NORTH, EAST),
    NORTH_WEST(NORTH, WEST),
    SOUTH_EAST(SOUTH, EAST),
    SOUTH_WEST(SOUTH, WEST),
    WEST_NORTH_WEST(WEST, NORTH_WEST),
    NORTH_NORTH_WEST(NORTH, NORTH_WEST),
    NORTH_NORTH_EAST(NORTH, NORTH_EAST),
    EAST_NORTH_EAST(EAST, NORTH_EAST),
    EAST_SOUTH_EAST(EAST, SOUTH_EAST),
    SOUTH_SOUTH_EAST(SOUTH, SOUTH_EAST),
    SOUTH_SOUTH_WEST(SOUTH, SOUTH_WEST),
    WEST_SOUTH_WEST(WEST, SOUTH_WEST),
    SELF(0, 0, 0);

    
    //FIELDS
    private final IntVector vector_;

    
    //CONSTRUCTORS
    private Direction(int modX, int modY, int modZ) {
        this.vector_ = new IntVector(modX, modY, modZ);
    }

    private Direction(final Direction face1, final Direction face2) {
        this.vector_ = (IntVector) face1.vector().add(face2.vector());
    }

    
    //GETTER
    public Vector<Integer> vector() {
        return this.vector_.clone();
    }
    public int x() {
        return this.vector_.x();
    }
    public int y() {
        return this.vector_.y();
    }
    public int z() {
        return this.vector_.z();
    }
    

    public Direction opposite() {
        return switch (this) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case NORTH_EAST -> Direction.SOUTH_WEST;
            case NORTH_WEST -> Direction.SOUTH_EAST;
            case SOUTH_EAST -> Direction.NORTH_WEST;
            case SOUTH_WEST -> Direction.NORTH_EAST;
            case WEST_NORTH_WEST -> Direction.EAST_SOUTH_EAST;
            case NORTH_NORTH_WEST -> Direction.SOUTH_SOUTH_EAST;
            case NORTH_NORTH_EAST -> Direction.SOUTH_SOUTH_WEST;
            case EAST_NORTH_EAST -> Direction.WEST_SOUTH_WEST;
            case EAST_SOUTH_EAST -> Direction.WEST_NORTH_WEST;
            case SOUTH_SOUTH_EAST -> Direction.NORTH_NORTH_WEST;
            case SOUTH_SOUTH_WEST -> Direction.NORTH_NORTH_EAST;
            case WEST_SOUTH_WEST -> Direction.EAST_NORTH_EAST;
            case SELF -> Direction.SELF;
            default -> SELF;
        };
    }
}
