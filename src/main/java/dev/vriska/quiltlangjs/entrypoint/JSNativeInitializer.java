package dev.vriska.quiltlangjs.entrypoint;

public abstract class JSNativeInitializer {

    public final String modid;
    public final String libName;

    public JSNativeInitializer(String libName, String modid) {
        this.libName = libName;
        this.modid = modid;
    }
}
