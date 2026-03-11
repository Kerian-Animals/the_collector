package fr.kerian_animals.thecollector.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fr.kerian_animals.thecollector.TheCollectorMod;
import fr.kerian_animals.thecollector.stash.CollectorSavedData;
import fr.kerian_animals.thecollector.stash.CollectorStash;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = TheCollectorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CollectorCommandHandler {
    private CollectorCommandHandler() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(rootCommand("thecollector"));
        dispatcher.register(rootCommand("collector"));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> rootCommand(String literal) {
        return Commands.literal(literal)
                .then(Commands.literal("locate")
                        .executes(CollectorCommandHandler::locateLatest)
                        .then(Commands.literal("latest").executes(CollectorCommandHandler::locateLatest))
                        .then(Commands.literal("nearest").executes(CollectorCommandHandler::locateNearest))
                        .then(Commands.literal("all").executes(CollectorCommandHandler::locateAll))
                );
    }

    private static int locateLatest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getServer().overworld();
        CollectorSavedData data = CollectorSavedData.get(level);

        Optional<CollectorStash> stashOptional = data.getLatestStash();
        if (stashOptional.isEmpty()) {
            source.sendFailure(Component.translatable("command.the_collector.locate.none"));
            return 0;
        }

        CollectorStash stash = stashOptional.get();
        source.sendSuccess(() -> formatLocateLine("command.the_collector.locate.latest", stash), false);
        return 1;
    }

    private static int locateNearest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getServer().overworld();
        CollectorSavedData data = CollectorSavedData.get(level);

        List<CollectorStash> stashes = data.getAllStashes().stream().toList();
        if (stashes.isEmpty()) {
            source.sendFailure(Component.translatable("command.the_collector.locate.none"));
            return 0;
        }

        Optional<ServerPlayer> playerOptional = Optional.ofNullable(source.getPlayer());
        if (playerOptional.isEmpty()) {
            return locateLatest(context);
        }

        ServerPlayer player = playerOptional.get();
        Vec3 pos = player.position();
        CollectorStash nearest = stashes.stream()
                .min(Comparator.comparingDouble(stash -> distanceScore(stash, player.serverLevel(), pos)))
                .orElse(stashes.getFirst());

        source.sendSuccess(() -> formatLocateLine("command.the_collector.locate.nearest", nearest), false);
        return 1;
    }

    private static int locateAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getServer().overworld();
        CollectorSavedData data = CollectorSavedData.get(level);
        List<CollectorStash> stashes = data.getAllStashes().stream()
                .sorted(Comparator.comparingLong(CollectorStash::createdTick).reversed())
                .toList();

        if (stashes.isEmpty()) {
            source.sendFailure(Component.translatable("command.the_collector.locate.none"));
            return 0;
        }

        int limit = Math.min(10, stashes.size());
        source.sendSuccess(() -> Component.translatable("command.the_collector.locate.all_header", stashes.size(), limit)
                .withStyle(ChatFormatting.GRAY), false);
        for (int i = 0; i < limit; i++) {
            CollectorStash stash = stashes.get(i);
            int index = i + 1;
            source.sendSuccess(() -> Component.literal(index + ". ")
                    .append(formatLocateLine("command.the_collector.locate.entry", stash)), false);
        }
        return limit;
    }

    private static MutableComponent formatLocateLine(String key, CollectorStash stash) {
        String teleportCmd = "/execute in " + stash.dimension().location() + " run tp @s "
                + stash.pos().getX() + " " + stash.pos().getY() + " " + stash.pos().getZ();

        return Component.translatable(
                key,
                stash.pos().getX(),
                stash.pos().getY(),
                stash.pos().getZ(),
                stash.dimension().location().toString()
        ).withStyle(style -> style
                .withColor(ChatFormatting.GOLD)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, teleportCmd)));
    }

    private static double distanceScore(CollectorStash stash, ServerLevel level, Vec3 from) {
        double dimPenalty = stash.dimension() == level.dimension() ? 0.0D : 1_000_000_000.0D;
        double dx = stash.pos().getX() + 0.5D - from.x;
        double dz = stash.pos().getZ() + 0.5D - from.z;
        return dimPenalty + dx * dx + dz * dz;
    }
}
