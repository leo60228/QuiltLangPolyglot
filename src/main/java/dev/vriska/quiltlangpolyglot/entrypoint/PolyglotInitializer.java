package dev.vriska.quiltlangpolyglot.entrypoint;

import java.nio.file.Path;
import java.net.URI;
import java.net.URL;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public abstract class PolyglotInitializer {
    public final String language;
    public final Source source;
    public final Context polyglot;

    public PolyglotInitializer(Path sourcePath) throws IOException {
        URI sourceUri = sourcePath.toUri();
        URL sourceUrl = sourceUri.toURL();

        polyglot = Context.newBuilder().allowAllAccess(true).build();
        language = Source.findLanguage(sourceUrl);
        source = Source.newBuilder(language, sourceUrl).build();
        polyglot.eval(source);
    }

    public void callPolyglot(String function) {
        polyglot.getBindings(language).getMember(function).executeVoid();
    }
}
