package com.jkantrell.mc.underilla.core.util;

import com.jkantrell.nbt.tag.CompoundTag;

public final class UnderillaUtils {

    public static CompoundTag airBlockTag() {
        CompoundTag tag = new CompoundTag(1);
        tag.putString("Name", "minecraft:air");
        return tag;
    }

}
