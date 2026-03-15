package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.lore.CollectorLoreBookFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheCollectorMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_COLLECTOR_TAB = CREATIVE_MODE_TABS.register(
            "the_collector",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.the_collector"))
                    .icon(() -> new ItemStack(ModItems.COLLECTOR_CATALYST.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.COLLECTOR_COMPASS.get());
                        output.accept(ModItems.COLLECTOR_CATALYST.get());
                        output.accept(ModItems.NETHER_RELIC_FRAGMENT.get());
                        output.accept(ModItems.CAVERN_RELIC_FRAGMENT.get());
                        output.accept(ModItems.ECHO_RELIC_FRAGMENT.get());
                        CollectorLoreBookFactory.createAllFragments().forEach(output::accept);
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }
}
