package dev.vriska.quiltlangpolyglot;

import javassist.util.proxy.MethodHandler;
import java.lang.reflect.Method;

public record SynchronizedMethodHandler(MethodHandler inner) implements MethodHandler {
    @Override
    public synchronized Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return inner.invoke(self, thisMethod, proceed, args);
    }
}
