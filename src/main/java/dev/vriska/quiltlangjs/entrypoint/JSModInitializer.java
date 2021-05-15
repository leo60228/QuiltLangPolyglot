package dev.vriska.quiltlangjs.entrypoint;

import net.fabricmc.api.ModInitializer;

public class JSModInitializer extends JSNativeInitializer implements ModInitializer {

    public JSModInitializer(String libName, String modid) {
        super(libName, modid);
    }

    @Override
    public void onInitialize() {
        runNativeInitializer(libName, modid);
    }

    public native void runNativeInitializer(String libPath, String modid);
}
