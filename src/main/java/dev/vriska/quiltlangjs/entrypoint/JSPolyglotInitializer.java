package dev.vriska.quiltlangjs.entrypoint;

import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public abstract class JSPolyglotInitializer {
    public final Source source;
    public final Context polyglot;

    public JSPolyglotInitializer(File sourceFile) throws IOException {
        polyglot = Context.newBuilder("js").allowAllAccess(true).build();
        source = Source.newBuilder("js", sourceFile).build();
        polyglot.eval(source);
    }

    public void callJS(String function) {
        polyglot.getBindings("js").getMember(function).executeVoid();
    }
}
