package net.glasslauncher.glassbrigadier.mixin.commandsource;

import net.glasslauncher.glassbrigadier.api.command.GlassCommandSource;
import net.glasslauncher.glassbrigadier.api.permission.PermissionNode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.gui.DedicatedServerGui;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(DedicatedServerGui.class)
public class ExtendedDedicatedServerGUI implements GlassCommandSource {

    @Shadow private MinecraftServer server;

    @Shadow @Override public String getName() {return "";}
    @Shadow @Override public void sendMessage(String message) {}

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public Vec3d getPosition() {
        PlayerEntity player = getPlayer();
        if (player != null) {
            return Vec3d.createCached(player.x, player.y, player.z);
        }

        return Vec3d.createCached(0, 0, 0);
    }

    @Override
    public Vector2f getRotation() {
        PlayerEntity player = getPlayer();
        if (player != null) {
            return new Vector2f(player.pitch, player.yaw);
        }
        return new Vector2f(0f, 0f);
    }

    @Override
    public Set<PermissionNode> getPermissions() {
        return Set.of(PermissionNode.ROOT);
    }

    @Override
    public Entity getEntity() {
        return null;
    }

    @Nullable
    @Override
    public PlayerEntity getPlayer() {
        return null;
    }

    @Override
    public @Nullable PlayerEntity getPlayerByName(String playerName) {
        return server.playerManager.getPlayer(playerName);
    }

    @Override
    public List<PlayerEntity> getAllPlayers() {
        //noinspection unchecked
        return new ArrayList<PlayerEntity>(server.playerManager.players);
    }
}
