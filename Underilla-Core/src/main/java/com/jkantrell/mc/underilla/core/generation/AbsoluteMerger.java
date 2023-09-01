package com.jkantrell.mc.underilla.core.generation;

import java.util.List;
import com.jkantrell.mc.underilla.core.api.Biome;
import com.jkantrell.mc.underilla.core.api.Block;
import com.jkantrell.mc.underilla.core.api.ChunkData;
import com.jkantrell.mc.underilla.core.reader.ChunkReader;
import com.jkantrell.mc.underilla.core.reader.Reader;
import com.jkantrell.mc.underilla.core.reader.WorldReader;
import com.jkantrell.mc.underilla.core.vector.IntVector;
import com.jkantrell.mc.underilla.core.vector.Vector;
import com.jkantrell.mc.underilla.core.vector.VectorIterable;
import com.jkantrell.mca.MCAUtil;

class AbsoluteMerger implements Merger {

    // FIELDS
    private final WorldReader worldReader_;
    private final int height_;
    private final List<? extends Biome> preserveBiomes_, ravinBiomes_;

    // CONSTRUCTORS
    AbsoluteMerger(WorldReader worldReader, int height, List<? extends Biome> preserveBiomes, List<? extends Biome> ravinBiomes) {
        this.worldReader_ = worldReader;
        this.height_ = height;
        this.preserveBiomes_ = preserveBiomes;
        this.ravinBiomes_ = ravinBiomes;
    }


    // IMPLEMENTATIONS
    @Override
    public void merge(ChunkReader reader, ChunkData chunkData) {
        this.mergeLand(reader, chunkData);
        this.mergeBiomes(reader, chunkData);
    }
    @Override
    public void mergeLand(ChunkReader reader, ChunkData chunkData) {
        Block airBlock = reader.blockFromTag(MCAUtil.airBlockTag()).get();
        // int airColumn = Math.max(reader.airSectionsBottom(), -64);
        int airColumn = reader.airSectionsBottom();
        chunkData.setRegion(0, airColumn, 0, 16, chunkData.getMaxHeight(), 16, airBlock);

        VectorIterable iterable = new VectorIterable(0, 16, -64, airColumn, 0, 16);
        int columnHeigth = this.height_;
        int lastX = -1;
        int lastZ = -1;
        for (Vector<Integer> v : iterable) {
            Block b = reader.blockAt(v).orElse(airBlock);
            Block vanillaBlock = chunkData.getBlock(v);

            // For every collumn of bloc calculate the lower block to remove that migth be lower than height_.
            if (v.x() != lastX || v.z() != lastZ) {
                lastX = v.x();
                lastZ = v.z();
                columnHeigth = (isPreservedBiome(reader, v) ? -64 : getLowerBlockToRemove(reader, v.x(), v.z(), airBlock));
            }

            // Place the custom world bloc over 55 (or -64 if is preseved biome) or if it is a custom ore or if it is air, or if vanilla
            // world have watter or grass over 30
            // and do not replace liquid vanilla blocks by air. (to preserve water and lava lackes)
            if (((v.y() > columnHeigth) || (isCustomWorldOreOutOfVanillaCaves(b, vanillaBlock))
                    || (v.y() > 30 && (vanillaBlock.isLiquid() || vanillaBlock.getName().equalsIgnoreCase("GRASS_BLOCK"))))
                    || (b.isAir() && !vanillaBlock.isLiquid())) {
                chunkData.setBlock(v, b);
            }

            // create ravines
            if (v.y() == columnHeigth && vanillaBlock.isAir() && isRavinBiome(reader, v) && isAirCollumn(chunkData, v, 30)) {
                for (int i = columnHeigth; i < airColumn; i++) {
                    chunkData.setBlock(new IntVector(v.x(), i, v.z()), vanillaBlock);
                }
            }
        }
    }

    /** return the 1st block 4 blocks under surface or heigth_ */
    private int getLowerBlockToRemove(Reader reader, int x, int z, Block defaultBlock) {
        int lbtr = this.height_ + 4;
        while (!reader.blockAt(x, lbtr, z).orElse(defaultBlock).isSolid() && lbtr > -64) {
            lbtr--;
        }
        return lbtr - 4;
    }

    @Override
    public void mergeBiomes(ChunkReader reader, ChunkData chunkData) {
        // VectorIterable iterable = new VectorIterable(0, 16, -64, 256, 0, 16);
        // // Chunk chunk = Bukkit.getWorld(reader.getWorldName()).getChunkAt(reader.getX(), reader.getZ());
        // for (Vector<Integer> v : iterable) {
        // Biome biome = reader.biomeAt(v).orElse(null);
        // if (biome == null) { continue; }
        // chunkData.setBiome(v, biome);
        // }
        // Instead of taking 5% of merge time, iterate over every block take 60% of merge time.
        // biome set block by block
        // VectorIterable iterable = new VectorIterable(0, 16, -64, 256, 0, 16);
        // for (Vector<Integer> v : iterable) {
        // Biome biome = reader.biomeAt(v).orElse(null);
        // if (biome == null) { continue; }
        // chunkData.setBiome(v, biome);
        // }
        // biome set by 4*4 at same y (default)
        VectorIterable iterable = new VectorIterable(0, 4, -64, chunkData.getMaxHeight() >> 2, 0, 4);
        for (Vector<Integer> v : iterable) {
            Biome biome = reader.biomeAtCell(v).orElse(null);
            if (biome == null) {
                continue;
            }
            chunkData.setBiome(v, biome);
        }
        // biome set for the column
        // Biome [] biomes = new Biome[256];
        // VectorIterable iterable = new VectorIterable(0, 16, 64, 65, 0, 16);
        // // int lastX = -1;
        // // int lastZ = -1;
        // // Biome biome = null;
        // int k=0;
        // for (Vector<Integer> v : iterable) {
        // biomes[k]=reader.biomeAt(v).orElse(null);
        // k++;
        // // // Change biome for column
        // // if(v.x()!=lastX || v.z()!=lastZ || biome==null){
        // // lastX = v.x();
        // // lastZ = v.z();
        // // // v.setY(-64);
        // // biome = reader.biomeAt(v).orElse(null);
        // // System.out.println("set biome for "+absoluteCoordinates(reader.getX(), reader.getZ(), v.clone())+" to "+biome);
        // // }
        // // if (biome == null) { continue; }
        // // chunkData.setBiome(v, biome);
        // }
        // chunkData.setBiomes(biomes, reader.getX(), reader.getZ());
    }


    // private --------------------------------------------------------------------------------------------------------
    /**
     * Return true if this block is an ore in the custom world and a solid block in vanilla world (This avoid to have ores floating in the
     * air in vanilla caves).
     * 
     * @param b            the block to check if is ore
     * @param vanillaBlock the block in vanilla world
     */
    private static List<String> notToRemove = List.of("COAL_ORE", "COPPER_ORE", "DIAMOND_ORE", "EMERALD_ORE", "GOLD_ORE", "IRON_ORE",
            "LAPIS_ORE", "REDSTONE_ORE", "DEEPSLATE_COAL_ORE", "DEEPSLATE_COPPER_ORE", "DEEPSLATE_DIAMOND_ORE", "DEEPSLATE_EMERALD_ORE",
            "DEEPSLATE_GOLD_ORE", "DEEPSLATE_IRON_ORE", "DEEPSLATE_LAPIS_ORE", "DEEPSLATE_REDSTONE_ORE", "ANDESITE", "DIORITE", "GRANITE",
            "QUARTZ_BLOCK", "GRAVEL", "NETHER_QUARTZ_ORE");
    private boolean isCustomWorldOreOutOfVanillaCaves(Block b, Block vanillaBlock) {
        return notToRemove.contains(b.getName().toUpperCase()) && (vanillaBlock == null || vanillaBlock.isSolid());
    }
    /** Return true if this biome need to be only custom world */
    private boolean isPreservedBiome(ChunkReader reader, Vector<Integer> v) {
        return this.preserveBiomes_.contains(reader.biomeAt(v).orElse(null));
    }

    private boolean isRavinBiome(ChunkReader reader, Vector<Integer> v) {
        return this.ravinBiomes_.contains(reader.biomeAt(v).orElse(null));
    }

    /** Return true if all the collumn is air. */
    private boolean isAirCollumn(ChunkData chunkData, Vector<Integer> v, int len) {
        for (int i = 0; i < len; i++) {
            if (!chunkData.getBlock(new IntVector(v.x(), v.y() - i, v.z())).isAir()) {
                return false;
            }
        }
        return true;
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
