package dev.vriska.quiltlangjs.entrypoint;

import dev.vriska.quiltlangjs.JSBlock;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import net.minecraft.block.Block;
import net.minecraft.util.ActionResult;

public abstract class JSPolyglotInitializer {
    public final Source source;
    public final Context polyglot;

    public JSPolyglotInitializer(File sourceFile) throws IOException {
        polyglot = Context.newBuilder("js").allowAllAccess(true).build();

        Value bindings = polyglot.getBindings("js");
        bindings.putMember("newBlock", (BiFunction<Block.Settings, Map<String, String>, Block>) (settings, methods) -> {
            JSBlock block = new JSBlock(settings);

            String onUse = methods.get("onUse");
            if (onUse != null) {
                block.onUseImpl = (state, world, pos, player, hand, hit) -> {
                    Value ret = bindings.getMember(onUse).execute(block, state, world, pos, player, hand, hit);
                    return ret.as(ActionResult.class);
                };
            }

            return block;
        });

        source = Source.newBuilder("js", sourceFile).build();
        polyglot.eval(source);
    }

    public void callJS(String function) {
        polyglot.getBindings("js").getMember(function).executeVoid();
    }
}
