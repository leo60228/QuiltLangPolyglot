package dev.vriska.quiltlangjs.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import java.io.File;
import java.io.IOException;

public class JSClientModInitializer extends JSPolyglotInitializer implements ClientModInitializer {
    public JSClientModInitializer(File source) throws IOException {
        super(source);
    }

    @Override
    public void onInitializeClient() {
        callJS("onInitializeClient");
    }
}
