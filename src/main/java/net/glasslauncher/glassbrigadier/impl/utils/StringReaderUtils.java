package net.glasslauncher.glassbrigadier.impl.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.glassbrigadier.api.argument.coordinate.Coordinate;
import net.glasslauncher.glassbrigadier.api.argument.playerselector.TargetSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.*;

public final class StringReaderUtils {
    private static final char SYNTAX_ESCAPE = '\\';

    private static boolean isAllowedInTargetSelector(final char c) {
        return StringReader.isAllowedInUnquotedString(c) || c == '@';
    }

    private static boolean isAllowedInId(final char c) {
        return StringReader.isAllowedInUnquotedString(c) || c == ':';
    }

    private static boolean isAllowedInPermissionNode(final char c) {
        return StringReader.isAllowedInUnquotedString(c) || c == '*';
    }

    public static String readStringUntilOrEnd(StringReader reader, Character... terminator) throws CommandSyntaxException {
        final StringBuilder result = new StringBuilder();
        final Set<Character> terminators = new HashSet<>(Arrays.asList(terminator));
        boolean escaped = false;
        while (reader.canRead()) {
            final char c = reader.read();
            if (escaped) {
                if (terminators.contains(c) || c == SYNTAX_ESCAPE) {
                    result.append(c);
                    escaped = false;
                } else {
                    reader.setCursor(reader.getCursor() - 1);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(reader, String.valueOf(c));
                }
            } else if (c == SYNTAX_ESCAPE) {
                escaped = true;
            } else if (terminators.contains(c)) {
                return result.toString();
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public static TargetSelector<?> readTargetSelector(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        if (reader.peek() == '@') {
            reader.skip();
            final char selectorType = reader.read();
            final String options;
            if (reader.canRead() && reader.peek() == '[') {
                reader.skip();
                options = reader.readStringUntil(']');
            } else {
                options = "";
            }
            return TargetSelector.create(selectorType, options);
        }

        while (reader.canRead() && isAllowedInTargetSelector(reader.peek())) {
            reader.skip();
        }

        String playerName = reader.getString().substring(start, reader.getCursor());

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            if (Minecraft.INSTANCE.player.name.equalsIgnoreCase(playerName)) {
                return TargetSelector.player(playerName);
            }
        }
        else if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            //noinspection deprecation
            MinecraftServer minecraftServer = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
            PlayerEntity player = minecraftServer.playerManager.getPlayer(playerName);
            if (player != null) {
                return TargetSelector.player(playerName);
            }
        }

        return TargetSelector.literal(playerName);
    }

    public static Map<String, String> readTargetSelectorOptions(StringReader reader) throws CommandSyntaxException {
        final Map<String, String> options = new HashMap<>();
        while (reader.canRead()) {
            String key = reader.readStringUntil('=');
            String value = readStringUntilOrEnd(reader, ',');
            options.put(key, value);
        }
        return options;
    }

    public static String readId(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedInId(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    public static String readPermissionNode(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && isAllowedInPermissionNode(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    public static Coordinate.CoordinatePart readCoordinatePart(StringReader reader, boolean isIntOnly) throws CommandSyntaxException {
        Coordinate.CoordinateType type = Coordinate.CoordinateType.ABSOLUTE;
        if (reader.peek() == '~') {
            type = Coordinate.CoordinateType.RELATIVE;
            reader.skip();
            if (!reader.canRead() || reader.peek() == ' ')
                return new Coordinate.CoordinatePart(0, type);
        } else if (reader.peek() == '^') {
            type = Coordinate.CoordinateType.LOCAL;
            reader.skip();
            if (!reader.canRead() || reader.peek() == ' ')
                return new Coordinate.CoordinatePart(0, type);
        }

        double coordinate = isIntOnly ? reader.readInt() : reader.readDouble();

        return new Coordinate.CoordinatePart(coordinate, type);
    }
}
