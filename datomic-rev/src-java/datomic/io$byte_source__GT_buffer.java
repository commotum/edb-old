/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.io.ByteSource;
import java.nio.ByteBuffer;

public final class io$byte_source__GT_buffer
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object b) {
        Object object;
        Object object2 = b;
        b = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof ByteSource) {
                object = ((ByteSource)object3).slurp_bytes();
                return ByteBuffer.wrap((byte[])object);
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__0.getRawRoot().invoke(object3);
        return ByteBuffer.wrap((byte[])object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$byte_source__GT_buffer.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.io", (String)"slurp-bytes");
    }
}

