package dev.vriska.quiltlangjs;

import dev.vriska.quiltlangjs.entrypoint.JSClientModInitializer;
import dev.vriska.quiltlangjs.entrypoint.JSDedicatedServerModInitializer;
import dev.vriska.quiltlangjs.entrypoint.JSModInitializer;
import dev.vriska.quiltlangjs.entrypoint.JSRunnable;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import java.io.File;
import java.io.IOException;

public class JSLangAdapter implements LanguageAdapter {
    public static final String FILE_SUFFIX = System.getProperty("os.name").contains("Win") ? ".dll" : ".so";

    @Override
    public <T> T create(ModContainer mod, String entrypointName, Class<T> type) throws LanguageAdapterException {
        String libName = entrypointName + FILE_SUFFIX;
        String modid = mod.getMetadata().getId();

        File source = mod.getPath(entrypointName).toFile();

        try {
            switch (type.getSimpleName()) {
                case "ModInitializer" -> {
                    return type.cast(new JSModInitializer(source));
                }
                case "ClientModInitializer" -> {
                    return type.cast(new JSClientModInitializer(libName, modid));
                }
                case "DedicatedServerModInitializer" -> {
                    return type.cast(new JSDedicatedServerModInitializer(source));
                }
                case "Runnable" -> {
                    return type.cast(new JSRunnable(source));
                }
                default -> throw new LanguageAdapterException("Can't handle initializer of type: " + type.getSimpleName());
            }
        } catch (IOException ex) {
            throw new LanguageAdapterException(ex);
        }
    }
}
