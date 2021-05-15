package dev.vriska.quiltlangjs.entrypoint;

import net.fabricmc.api.DedicatedServerModInitializer;
import java.io.File;
import java.io.IOException;

public class JSDedicatedServerModInitializer extends JSPolyglotInitializer implements DedicatedServerModInitializer {
    public JSDedicatedServerModInitializer(File source) throws IOException {
        super(source);
    }

    @Override
    public void onInitializeServer() {
        callJS("onInitializeServer");
    }
}
