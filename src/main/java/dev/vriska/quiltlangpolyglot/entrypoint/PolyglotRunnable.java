package dev.vriska.quiltlangpolyglot.entrypoint;

import java.io.File;
import java.io.IOException;

public class PolyglotRunnable extends PolyglotInitializer implements Runnable {
    public PolyglotRunnable(File source) throws IOException {
        super(source);
    }

    @Override
    public void run() {
        callPolyglot("run");
    }
}
