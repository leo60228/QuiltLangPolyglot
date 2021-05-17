package dev.vriska.quiltlangpolyglot;

import dev.vriska.quiltlangpolyglot.entrypoint.PolyglotClientModInitializer;
import dev.vriska.quiltlangpolyglot.entrypoint.PolyglotDedicatedServerModInitializer;
import dev.vriska.quiltlangpolyglot.entrypoint.PolyglotModInitializer;
import dev.vriska.quiltlangpolyglot.entrypoint.PolyglotRunnable;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import java.nio.file.Path;
import java.io.IOException;

public class PolyglotLangAdapter implements LanguageAdapter {
    public static final String FILE_SUFFIX = System.getProperty("os.name").contains("Win") ? ".dll" : ".so";

    @Override
    public <T> T create(ModContainer mod, String entrypointName, Class<T> type) throws LanguageAdapterException {
        Path source = mod.getPath(entrypointName);

        try {
            switch (type.getSimpleName()) {
                case "ModInitializer" -> {
                    return type.cast(new PolyglotModInitializer(source));
                }
                case "ClientModInitializer" -> {
                    return type.cast(new PolyglotClientModInitializer(source));
                }
                case "DedicatedServerModInitializer" -> {
                    return type.cast(new PolyglotDedicatedServerModInitializer(source));
                }
                case "Runnable" -> {
                    return type.cast(new PolyglotRunnable(source));
                }
                default -> throw new LanguageAdapterException("Can't handle initializer of type: " + type.getSimpleName());
            }
        } catch (IOException ex) {
            throw new LanguageAdapterException(ex);
        }
    }
}
