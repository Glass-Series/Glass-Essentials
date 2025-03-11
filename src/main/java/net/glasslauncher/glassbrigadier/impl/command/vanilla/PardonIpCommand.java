package net.glasslauncher.glassbrigadier.impl.command.vanilla;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType;
import net.glasslauncher.glassbrigadier.api.command.CommandProvider;
import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.impl.argument.GlassCommandBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.modificationstation.stationapi.api.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelectorArgumentType.getPlayers;
import static net.glasslauncher.glassbrigadier.api.predicate.HasPermission.permission;
import static net.glasslauncher.glassbrigadier.impl.command.vanilla.BanIpCommand.IP_REGEX;

public class PardonIpCommand implements CommandProvider {
    @Override
    public LiteralArgumentBuilder<GlassCommandSource> get() {
        return GlassCommandBuilder.create("pardon-ip", "Unban an IP.")
                .alias("unban-ip")
                .requires(permission("command.pardonip"))
                .then(RequiredArgumentBuilder.argument("player", TargetSelectorArgumentType.player()))
                .executes(this::opPlayer);
    }

    public int opPlayer(CommandContext<GlassCommandSource> context) {
        String ip = getString(context, "ip").toLowerCase().strip();
        if (!ip.matches(IP_REGEX)) {
            context.getSource().sendMessage(Formatting.RED + ip + " isn't a valid IP address!");
            return 0;
        }
        //noinspection deprecation
        PlayerManager playerManager = ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).playerManager;
        if (!playerManager.bannedIps.contains(ip)) {
            context.getSource().sendMessage(Formatting.RED + ip + " isn't banned!");
            return 0;
        }

        playerManager.unbanIp(ip);
        sendFeedbackAndLog(context.getSource(), "Unbanned " + ip + ".");
        return 0;
    }
}
