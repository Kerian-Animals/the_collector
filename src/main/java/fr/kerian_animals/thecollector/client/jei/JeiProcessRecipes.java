package fr.kerian_animals.thecollector.client.jei;

import fr.kerian_animals.thecollector.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class JeiProcessRecipes {
    public static final CauldronCrystallizationRecipe CAULDRON_CRYSTALLIZATION = new CauldronCrystallizationRecipe(
            new ItemStack(ModItems.DISTILLED_RESONANCE.get()),
            new ItemStack(Items.CAULDRON),
            new ItemStack(ModItems.RESONANCE_LADLE.get()),
            new ItemStack(ModItems.RESONANT_RESIDUE.get(), 3),
            180
    );

    public static final CollectorRitualRecipe COLLECTOR_RITUAL = new CollectorRitualRecipe(
            new ItemStack(ModItems.COLLECTOR_CATALYST.get()),
            new ItemStack(Items.LODESTONE),
            new ItemStack(Items.CRYING_OBSIDIAN, 4),
            new ItemStack(Items.SOUL_LANTERN, 4)
    );
    public static final TraceCaptureRecipe TRACE_CAPTURE = new TraceCaptureRecipe(
            new ItemStack(ModItems.COLLECTOR_TRACE.get()),
            new ItemStack(Items.GLASS_BOTTLE),
            new ItemStack(ModItems.UNSTABLE_RESONANCE.get())
    );

    private JeiProcessRecipes() {
    }

    public static List<CauldronCrystallizationRecipe> cauldronRecipes() {
        return List.of(CAULDRON_CRYSTALLIZATION);
    }

    public static List<CollectorRitualRecipe> ritualRecipes() {
        return List.of(COLLECTOR_RITUAL);
    }

    public static List<TraceCaptureRecipe> traceCaptureRecipes() {
        return List.of(TRACE_CAPTURE);
    }

    public record CauldronCrystallizationRecipe(
            ItemStack input,
            ItemStack cauldron,
            ItemStack tool,
            ItemStack output,
            int restingSeconds
    ) {
        public ItemStack icon() {
            return output.copy();
        }
    }

    public record CollectorRitualRecipe(
            ItemStack catalyst,
            ItemStack lodestone,
            ItemStack cryingObsidian,
            ItemStack soulLantern
    ) {
        public ItemStack icon() {
            return catalyst.copy();
        }
    }

    public record TraceCaptureRecipe(
            ItemStack trace,
            ItemStack bottle,
            ItemStack output
    ) {
        public ItemStack icon() {
            return output.copy();
        }
    }
}
