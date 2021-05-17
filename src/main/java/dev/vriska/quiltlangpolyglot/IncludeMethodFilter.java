package dev.vriska.quiltlangpolyglot;

import java.lang.reflect.Method;
import java.util.List;
import javassist.util.proxy.MethodFilter;

public record IncludeMethodFilter(Class<?> target, List<String> methods) implements MethodFilter {
    @Override
    public boolean isHandled(Method method) {
        String mapped = GraalRemapper.remapMethod(method);
        return methods.contains(mapped);
    }
}
