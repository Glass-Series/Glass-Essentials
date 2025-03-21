package net.glasslauncher.glassbrigadier.impl.command.vanilla;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.argument.itemid.ItemId;
import net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelector;
import net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassCommandBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.modificationstation.stationapi.api.util.Formatting;

import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.glasslauncher.glassbrigadier.api.argument.itemid.ItemIdArgumentType.getItemId;
import static net.glasslauncher.glassbrigadier.api.argument.itemid.ItemIdArgumentType.itemId;
import static net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType.entity;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.permission;

public class GiveCommand implements CommandProvider {

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassCommandBuilder.create("give", "Gives the specified player the specified item.", "Gives the specified player the specified item. Loooong long long description way over 129 lets go weee wooo weeeeee aaaaaaa aaaaasddd \nForced linebreak")
                .alias("g")
                .requires(permission("command.give"))
                .then(RequiredArgumentBuilder.<GlassCommandSource, TargetSelector<?>>argument("player", entity())
                        .then(RequiredArgumentBuilder.<GlassCommandSource, ItemId>argument("item", itemId())
                                .executes(this::giveItem)
                                .then(RequiredArgumentBuilder.<GlassCommandSource, Integer>argument("count", integer(1))
                                        .executes(this::giveItemWithCount)
                                        .then(RequiredArgumentBuilder.<GlassCommandSource, Integer>argument("meta", integer())
                                                .executes(this::giveItemWithCountAndMeta)
                                        )
                                )
                        )
                );
    }

    public int giveItem(CommandContext<GlassCommandSource> context) {
        List<PlayerEntity> players = TargetSelectorArgumentType.getPlayers(context, "player").getEntities(context.getSource());
        if (players.isEmpty()) {
            sendFeedbackAndLog(context.getSource(), Formatting.RED + "No player named \"" + context.getArgument("player", TargetSelector.class).getName() + "\" found.");
            return 0;
        }

        players.forEach(playerEntity -> {
            int item = getItemId(context, "item").numericId;
            context.getSource().sendMessage("Giving " + playerEntity.name + " some " + item);
            playerEntity.dropItem(item, 1, 0);

        });
        return 0;

    }

    public int giveItemWithCount(CommandContext<GlassCommandSource> context) {
        List<PlayerEntity> players = TargetSelectorArgumentType.getPlayers(context, "player").getEntities(context.getSource());
        if (players.isEmpty()) {
            sendFeedbackAndLog(context.getSource(), Formatting.RED + "No player named \"" + context.getArgument("player", TargetSelector.class).getName() + "\" found.");
            return 0;
        }

        players.forEach(playerEntity -> {

            int item = getItemId(context, "item").numericId;
            int count = getInteger(context, "count");
            sendFeedbackAndLog(context.getSource(), "Giving " + playerEntity.name + " " + count + " of " + item);
            playerEntity.dropItem(item, count, 0);
        });

        return 0;
    }

    public int giveItemWithCountAndMeta(CommandContext<GlassCommandSource> context) {
        List<PlayerEntity> players = TargetSelectorArgumentType.getPlayers(context, "player").getEntities(context.getSource());
        if (players.isEmpty()) {
            sendFeedbackAndLog(context.getSource(), Formatting.RED + "No player named \"" + context.getArgument("player", TargetSelector.class).getName() + "\" found.");
            return 0;
        }

        players.forEach(playerEntity -> {
            int item = getItemId(context, "item").numericId;
            int count = getInteger(context, "count");
            int meta = getInteger(context, "meta");
            sendFeedbackAndLog(context.getSource(), "Giving " + playerEntity.name + " " + count + " of " + item + ":" + meta);
            playerEntity.dropItem(item, count, meta);
        });

        return 0;
    }
}
