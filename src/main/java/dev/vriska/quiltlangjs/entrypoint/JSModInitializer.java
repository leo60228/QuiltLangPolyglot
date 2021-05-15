package dev.vriska.quiltlangjs.entrypoint;

import net.fabricmc.api.ModInitializer;
import java.io.File;
import java.io.IOException;

public class JSModInitializer extends JSPolyglotInitializer implements ModInitializer {
    public JSModInitializer(File source) throws IOException {
        super(source);
    }

    @Override
    public void onInitialize() {
        callJS("onInitialize");
    }
}
