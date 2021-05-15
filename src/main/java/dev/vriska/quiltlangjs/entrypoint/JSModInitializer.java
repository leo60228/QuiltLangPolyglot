package dev.vriska.quiltlangjs.entrypoint;

import net.fabricmc.api.ModInitializer;
import java.io.File;

public class JSModInitializer implements ModInitializer {
    public final File source;

    public JSModInitializer(File source) {
        this.source = source;
    }

    @Override
    public void onInitialize() {
        System.out.println("initializing JS mod");
    }
}
