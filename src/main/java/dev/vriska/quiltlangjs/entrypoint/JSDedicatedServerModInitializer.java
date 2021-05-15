package dev.vriska.quiltlangjs.entrypoint;

import net.fabricmc.api.DedicatedServerModInitializer;

public class JSDedicatedServerModInitializer extends JSNativeInitializer implements DedicatedServerModInitializer{

    public JSDedicatedServerModInitializer(String libName, String modid) {
        super(libName, modid);
    }

    @Override
    public void onInitializeServer() {
        runNativeDedicatedServerInitializer(libName, modid);
    }

    public native void runNativeDedicatedServerInitializer(String libPath, String modid);
}
