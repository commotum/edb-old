/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.java.io.IOFactory
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.fressian.impl.ByteBufferInputStream
 */
package datomic;

import clojure.java.io.IOFactory;
import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.nio.ByteBuffer;
import org.fressian.impl.ByteBufferInputStream;

public final class io$fn__9322
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object x, Object opts) {
        Object object;
        Object object2 = x;
        x = null;
        ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream((ByteBuffer)object2);
        if (Util.classOf((Object)byteBufferInputStream) != __cached_class__0) {
            if (byteBufferInputStream instanceof IOFactory) {
                Object object3 = opts;
                opts = null;
                object = ((IOFactory)byteBufferInputStream).make_input_stream(object3);
                return object;
            }
            byteBufferInputStream = byteBufferInputStream;
            __cached_class__0 = Util.classOf((Object)byteBufferInputStream);
        }
        Object object4 = opts;
        opts = null;
        object = const__0.getRawRoot().invoke((Object)byteBufferInputStream, object4);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$fn__9322.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.java.io", (String)"make-input-stream");
    }
}

