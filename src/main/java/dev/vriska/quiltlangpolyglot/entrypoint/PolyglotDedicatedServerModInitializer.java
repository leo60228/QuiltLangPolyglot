package dev.vriska.quiltlangpolyglot.entrypoint;

import net.fabricmc.api.DedicatedServerModInitializer;
import java.nio.file.Path;
import java.io.IOException;

public class PolyglotDedicatedServerModInitializer extends PolyglotInitializer implements DedicatedServerModInitializer {
    public PolyglotDedicatedServerModInitializer(Path source) throws IOException {
        super(source);
    }

    @Override
    public void onInitializeServer() {
        callPolyglot("onInitializeServer");
    }
}
