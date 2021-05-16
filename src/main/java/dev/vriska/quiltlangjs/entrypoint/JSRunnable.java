package dev.vriska.quiltlangjs.entrypoint;

import java.io.File;
import java.io.IOException;

public class JSRunnable extends JSPolyglotInitializer implements Runnable {
    public JSRunnable(File source) throws IOException {
        super(source);
    }

    @Override
    public void run() {
        callJS("run");
    }
}
