package net.glasslauncher.glassbrigadier.impl.command.vanilla;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.minecraft.world.World;

import java.util.function.Function;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.permission;

public class TimeCommand implements CommandProvider {

    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return LiteralArgumentBuilder.<GlassCommandSource>literal("time")
                .requires(permission("command.time"))
                .then(
                        LiteralArgumentBuilder.<GlassCommandSource>literal("set")
                                .then(RequiredArgumentBuilder.<GlassCommandSource, Long>argument("time", longArg(0))
                                        .executes(setTime(context -> getLong(context, "time")))
                                ).then(LiteralArgumentBuilder.<GlassCommandSource>literal("day")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 0)))
                                ).then(LiteralArgumentBuilder.<GlassCommandSource>literal("noon")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 6000L)))
                                ).then(LiteralArgumentBuilder.<GlassCommandSource>literal("night")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 12000L)))
                                ).then(LiteralArgumentBuilder.<GlassCommandSource>literal("midnight")
                                        .executes(setTime(a -> nextDayIfPast(a.getSource().getWorld().getTime(), 18000L)))
                                )
                )
                .then(
                        LiteralArgumentBuilder.<GlassCommandSource>literal("get")
                                .executes(context -> {
                                    long time = context.getSource().getWorld().getTime();

                                    long hours = ((time / 1000) + 6) % 24;
                                    long minutes = (time % 1000) * 60 / 1000;

                                    context.getSource().sendMessage("It is day " + (int) Math.floor((double) time / 24000L) + " at " + hours + ":" + (String.valueOf(minutes).length() == 1 ? "0" + minutes : minutes) + ".");
                                    return 0;
                                })
                )
                .then(
                        LiteralArgumentBuilder.<GlassCommandSource>literal("add")
                                .then(RequiredArgumentBuilder.<GlassCommandSource, Long>argument("time", longArg())
                                        .executes(context -> {
                                            World level = (context.getSource()).getWorld();
                                            level.setTime(level.getTime()+getLong(context, "time"));
                                            sendFeedbackAndLog(context.getSource(), "Set time to " + level.getTime());
                                            return 0;
                                        }))
                );
    }

    public Command<GlassCommandSource> setTime(Function<CommandContext<GlassCommandSource>, Long> time) {
        return context -> {
            logWarn("Time was " + context.getSource().getWorld().getTime());
            long timeValue = time.apply(context);
            context.getSource().getWorld().setTime(timeValue);
            sendFeedbackAndLog(context.getSource(), "Set time to " + timeValue);
            return 0;
        };
    }

    long nextDayIfPast(long time, long targetTimeOfDay) {
        long remainder = time % 24000;

        if (remainder > targetTimeOfDay) {
            return time + 24000 - remainder + targetTimeOfDay;
        }

        return time - remainder + targetTimeOfDay;
    }
}
