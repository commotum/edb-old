// Copyright (c) Nicola Mometto & contributors.

package clojure.tools.decompiler;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RetrieveClasses {

    private static Map<String,byte[]> classes = new ConcurrentHashMap<String,byte[]>();

    public static Map<String,byte[]> getClasses() {
        return classes;
    }

    public static class Transformer implements ClassFileTransformer {
        public byte[] transform(ClassLoader loader, String className,
                                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                byte[] classBytes) throws IllegalClassFormatException {
            classes.put(className, classBytes);
            return classBytes;
        }
    }

    public static void premain(String args, Instrumentation inst) {
        inst.addTransformer(new Transformer());
    }

}
