package com.knightcode.appliedstoragesorter.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Decorative ME Controller shell with dark_matter_v2 styling. No network behavior.
 */
public class DarkMatterControllerBlock extends Block {
    public static final MapCodec<DarkMatterControllerBlock> CODEC = simpleCodec(DarkMatterControllerBlock::new);

    public DarkMatterControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
