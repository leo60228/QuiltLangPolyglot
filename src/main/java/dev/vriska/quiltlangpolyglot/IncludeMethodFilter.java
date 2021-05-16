package dev.vriska.quiltlangpolyglot;

import java.lang.reflect.Method;
import java.util.List;
import javassist.util.proxy.MethodFilter;

public class IncludeMethodFilter implements MethodFilter {
    List<String> methods;

    public IncludeMethodFilter(List<String> methods) {
        this.methods = methods;
    }

    @Override
    public boolean isHandled(Method method) {
        return methods.contains(method.getName());
    }
}
