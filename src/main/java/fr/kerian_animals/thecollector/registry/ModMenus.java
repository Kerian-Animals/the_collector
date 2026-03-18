package fr.kerian_animals.thecollector.registry;

import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.menu.AlembicMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TheCollectorMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<AlembicMenu>> ALEMBIC = MENUS.register(
            "alembic",
            () -> IMenuTypeExtension.create(AlembicMenu::new)
    );

    private ModMenus() {
    }
}
