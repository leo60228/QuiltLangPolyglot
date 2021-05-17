package dev.vriska.quiltlangpolyglot.entrypoint;

import java.nio.file.Path;
import java.io.IOException;

public class PolyglotRunnable extends PolyglotInitializer implements Runnable {
    public PolyglotRunnable(Path source) throws IOException {
        super(source);
    }

    @Override
    public void run() {
        callPolyglot("run");
    }
}
