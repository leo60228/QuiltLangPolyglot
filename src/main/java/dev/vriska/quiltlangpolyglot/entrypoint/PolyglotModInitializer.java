package dev.vriska.quiltlangpolyglot.entrypoint;

import net.fabricmc.api.ModInitializer;
import java.nio.file.Path;
import java.io.IOException;

public class PolyglotModInitializer extends PolyglotInitializer implements ModInitializer {
    public PolyglotModInitializer(Path source) throws IOException {
        super(source);
    }

    @Override
    public void onInitialize() {
        callPolyglot("onInitialize");
    }
}
