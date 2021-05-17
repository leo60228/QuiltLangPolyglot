package dev.vriska.quiltlangpolyglot.entrypoint;

import java.nio.file.Path;
import java.nio.file.Files;
import java.net.URI;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.ByteSequence;
import org.apache.tika.Tika;

public abstract class PolyglotInitializer {
    public final String language;
    public final Source source;
    public final Context polyglot;

    public PolyglotInitializer(Path sourcePath) throws IOException {
        String sourceName = sourcePath.getFileName().toString();

        InputStream sourceStream = Files.newInputStream(sourcePath);
        Tika tika = new Tika();
        String mimeType = tika.detect(sourceStream, sourceName);

        if (mimeType.equals("text/x-ruby")) {
            mimeType = "application/x-ruby";
        } else if (mimeType.equals("text/x-rsrc")) {
            mimeType = "application/x-r";
        }

        System.out.println("mime type: " + mimeType);

        sourceStream.close();

        polyglot = Context.newBuilder().allowAllAccess(true).build();
        language = Source.findLanguage(mimeType);
        System.out.println("language: " + language);

        URI sourceUri = sourcePath.toUri();
        URL sourceUrl = sourceUri.toURL();

        byte[] sourceBytes = Files.readAllBytes(sourcePath);

        if (language.equals("llvm")) {
            ByteSequence sourceSequence = ByteSequence.create(sourceBytes);

            source = Source.newBuilder(language, sourceUrl).content(sourceSequence).mimeType(mimeType).build();
        } else {
            String sourceText = new String(sourceBytes);

            source = Source.newBuilder(language, sourceUrl).content(sourceText).mimeType(mimeType).build();
        }

        polyglot.eval(source);
    }

    public void callPolyglot(String function) {
        polyglot.getBindings(language).getMember(function).executeVoid();
    }
}
