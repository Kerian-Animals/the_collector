package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.block.CollectorTraceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheCollectorMod.MOD_ID);

    public static final DeferredBlock<Block> COLLECTOR_TRACE = BLOCKS.register(
            "collector_trace",
            () -> new CollectorTraceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .replaceable()
                    .noCollission()
                    .ignitedByLava())
    );

    private ModBlocks() {
    }
}
