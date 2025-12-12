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
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$alias_buf_bytes
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;

    /*
     * WARNING - void declaration
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object buff) {
        Object object;
        void var1_1;
        boolean and__5236__auto__9317 = ((ByteBuffer)buff).hasArray();
        if (and__5236__auto__9317 ? Util.equiv((long)((Buffer)buff).remaining(), (long)((ByteBuffer)buff).array().length) : var1_1) {
            Object object2 = buff;
            buff = null;
            object = ((ByteBuffer)object2).array();
            return object;
        }
        Object object3 = buff;
        buff = null;
        Object object4 = object3;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object4 instanceof ByteSource) {
                object = ((ByteSource)object4).slurp_bytes();
                return object;
            }
            object4 = object4;
            __cached_class__0 = Util.classOf((Object)object4);
        }
        object = const__2.getRawRoot().invoke(object4);
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$alias_buf_bytes.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.io", (String)"slurp-bytes");
    }
}

