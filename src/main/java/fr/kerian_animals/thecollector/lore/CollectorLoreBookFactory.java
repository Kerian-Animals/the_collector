package fr.kerian_animals.thecollector.lore;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CollectorLoreBookFactory {
    private static final String AUTHOR = "Hand and Ink";
    private static final String PAGE_KEY_PREFIX = "lore.the_collector.page.";

    private CollectorLoreBookFactory() {
    }

    public static ItemStack createRandomFragment(RandomSource random) {
        LoreFragment fragment = LoreFragment.values()[random.nextInt(LoreFragment.values().length)];
        return createFragment(fragment);
    }

    public static List<ItemStack> createAllFragments() {
        return java.util.Arrays.stream(LoreFragment.values())
                .map(CollectorLoreBookFactory::createFragment)
                .toList();
    }

    private static ItemStack createFragment(LoreFragment fragment) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(fragment.title()),
                AUTHOR,
                0,
                fragment.pages(),
                true
        ));
        return book;
    }

    private enum LoreFragment {
        LOST_LEAF("lost_leaf", 3),
        SURVEYOR_JOURNAL("surveyor_journal", 3),
        SCHOLAR_NOTE("scholar_note", 3),
        FINAL_PAGE("final_page", 3);

        private final String key;
        private final int pageCount;

        LoreFragment(String key, int pageCount) {
            this.key = key;
            this.pageCount = pageCount;
        }

        private List<Filterable<Component>> pages() {
            return java.util.stream.IntStream.rangeClosed(1, pageCount)
                    .mapToObj(index -> page(PAGE_KEY_PREFIX + key + "." + index))
                    .toList();
        }

        private @NotNull String title() {
            return switch (this) {
                case LOST_LEAF -> "Lost Leaf";
                case SURVEYOR_JOURNAL -> "Surveyor Journal";
                case SCHOLAR_NOTE -> "Scholar Note";
                case FINAL_PAGE -> "Final Page";
            };
        }

        private static Filterable<Component> page(String translationKey) {
            return Filterable.passThrough(Component.translatable(translationKey).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
