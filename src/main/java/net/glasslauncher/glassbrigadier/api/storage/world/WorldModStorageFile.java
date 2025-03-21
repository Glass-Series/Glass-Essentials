package net.glasslauncher.glassbrigadier.api.storage.world;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.glasslauncher.glassbrigadier.GlassBrigadier;
import net.glasslauncher.glassbrigadier.api.storage.StorageFile;
import net.glasslauncher.glassbrigadier.api.storage.StorageUtils;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.MemoryConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class WorldModStorageFile extends StorageFile {
    public static final Configuration WORLD_DATA_DEFAULTS = new MemoryConfiguration();

    private static final @NotNull Cache<@NotNull Identifier, @NotNull WorldModStorageFile> CACHE = Caffeine.newBuilder().softValues().build();
    private static final @NotNull Function<@NotNull Identifier, @NotNull WorldModStorageFile> PLAYER_STORAGE_FILE_FACTORY = (identifier) -> {
        File modFile = StorageUtils.getModStorageFile(identifier);
        if (!modFile.getParentFile().exists()) {
            modFile.getParentFile().mkdirs();
        }
        WorldModStorageFile file = new WorldModStorageFile(modFile);
        try {
            if (file.exists()) {
                file.load();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    };

    public static void invalidateAll() {
        CACHE.invalidateAll();
    }

    public static WorldModStorageFile of(Namespace namespace) {
        return of(namespace.id("storage"));
    }

    public static WorldModStorageFile of(Identifier identifier) {
        return CACHE.get(identifier, PLAYER_STORAGE_FILE_FACTORY);
    }

    protected WorldModStorageFile(File file) {
        super(file);

        setHeader(GlassBrigadier.NAMESPACE.getName().toUpperCase() + """
                 PLAYER DATA FILE
                DO NOT EDIT WITHOUT BACKING UP FIRST
                """);

        setDefaults(WORLD_DATA_DEFAULTS);
    }
}
