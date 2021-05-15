package dev.vriska.quiltlangjs;

import dev.vriska.quiltlangjs.entrypoint.JSClientModInitializer;
import dev.vriska.quiltlangjs.entrypoint.JSDedicatedServerModInitializer;
import dev.vriska.quiltlangjs.entrypoint.JSModInitializer;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;

public class JSLangAdapter implements LanguageAdapter {

    public static final String FILE_SUFFIX = System.getProperty("os.name").contains("Win") ? ".dll" : ".so";

    @Override
    public <T> T create(ModContainer mod, String entrypointName, Class<T> type) throws LanguageAdapterException {
        String libName = entrypointName + FILE_SUFFIX;
        String modid = mod.getMetadata().getId();

        QuiltLangJS.tryLoadJS();
        switch (type.getSimpleName()) {
            case "ModInitializer" -> {
                return type.cast(new JSModInitializer(libName, modid));
            }
            case "ClientModInitializer" -> {
                return type.cast(new JSClientModInitializer(libName, modid));
            }
            case "DedicatedServerModInitializer" -> {
                return type.cast(new JSDedicatedServerModInitializer(libName, modid));
            }
            default -> throw new LanguageAdapterException("Can't handle initializer of type: " + type.getSimpleName());
        }
    }
}
