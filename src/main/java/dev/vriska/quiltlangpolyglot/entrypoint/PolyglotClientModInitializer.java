package dev.vriska.quiltlangpolyglot.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import java.io.File;
import java.io.IOException;

public class PolyglotClientModInitializer extends PolyglotInitializer implements ClientModInitializer {
    public PolyglotClientModInitializer(File source) throws IOException {
        super(source);
    }

    @Override
    public void onInitializeClient() {
        callPolyglot("onInitializeClient");
    }
}
