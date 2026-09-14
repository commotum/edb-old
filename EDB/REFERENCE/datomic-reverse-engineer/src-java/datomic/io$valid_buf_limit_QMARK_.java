/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.Buffer;

public final class io$valid_buf_limit_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"<=");
    public static final Object const__1 = 0L;

    public static Object invokeStatic(Object bbuf, Object limit2) {
        Object object = limit2;
        limit2 = null;
        Object object2 = bbuf;
        bbuf = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1, object, (Object)((Buffer)object2).capacity());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$valid_buf_limit_QMARK_.invokeStatic(object3, object4);
    }
}

