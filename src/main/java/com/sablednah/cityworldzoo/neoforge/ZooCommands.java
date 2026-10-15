package com.sablednah.cityworldzoo.neoforge;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sablednah.cityworldzoo.BuildInfo;
import com.sablednah.cityworldzoo.ZooEntities;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.fml.ModList;

/**
 * {@code /zoo}: administering the mod, nothing else. {@code status} says what is registered and which build
 * this is; {@code count [radius]} says what is loaded near you (the "correctly withheld looks exactly like
 * broken" rule: a zoo with no animals must be askable). Operators only.
 */
public final class ZooCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zoo")
                .then(Commands.literal("status")
                        .requires(src -> src.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ZooCommands::status))
                .then(Commands.literal("count")
                        .requires(src -> src.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> count(ctx, 64))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 512))
                                .executes(ctx -> count(ctx, IntegerArgumentType.getInteger(ctx, "radius"))))));
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        long tagged = ZooEntities.ALL.stream().filter(s -> ZooEntities.isZooAnimal(s.entityType())).count();
        boolean cityworld = ModList.get().isLoaded("cityworld");
        ctx.getSource().sendSuccess(() -> Feedback.colored(Lang.fmt("msg.status",
                "count", ZooEntities.ALL.size(), "tagged", tagged,
                "cityworld", Lang.get(cityworld ? "msg.status.cityworld.present" : "msg.status.cityworld.absent"))), false);
        for (ZooEntities.Species<? extends ZooAnimal> s : ZooEntities.ALL) {
            AttributeSupplier a = DefaultAttributes.getSupplier(s.entityType());
            String line = Lang.fmt("msg.status.animal", "id", s.identifier(), "name", s.entityType().getDescription().getString(),
                    "health", trim(a.getBaseValue(Attributes.MAX_HEALTH)), "speed", trim(a.getBaseValue(Attributes.MOVEMENT_SPEED)),
                    "damage", trim(a.getBaseValue(Attributes.ATTACK_DAMAGE)),
                    "tall", trim(s.entityType().getHeight()) + " tall");
            ctx.getSource().sendSuccess(() -> Feedback.colored(line), false);
        }
        ctx.getSource().sendSuccess(() -> Feedback.colored(Lang.fmt("msg.status.build", "build", BuildInfo.describe())), false);
        return ZooEntities.ALL.size();
    }

    private static int count(CommandContext<CommandSourceStack> ctx, int radius) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Map<EntityType<?>, Integer> counts = new LinkedHashMap<>();
        for (ZooAnimal a : player.level().getEntitiesOfClass(ZooAnimal.class, player.getBoundingBox().inflate(radius)))
            counts.merge(a.getType(), 1, Integer::sum);
        if (counts.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Feedback.colored(Lang.fmt("msg.count.none", "radius", radius)), false);
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Feedback.colored(Lang.fmt("msg.count.header", "radius", radius)), false);
        counts.forEach((type, n) -> ctx.getSource().sendSuccess(() -> Feedback.colored(
                Lang.fmt("msg.count.entry", "name", type.getDescription().getString(), "count", n)), false));
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private ZooCommands() {}
}
