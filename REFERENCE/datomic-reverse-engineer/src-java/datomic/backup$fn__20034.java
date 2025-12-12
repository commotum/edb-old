/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class backup$fn__20034
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__1 = (AFn)Symbol.intern((String)"datomic.s3backup", (String)"storage-from-uri");

    public static Object invokeStatic(Object uri2, Object sse_QMARK_) {
        Object object = uri2;
        uri2 = null;
        Object object2 = sse_QMARK_;
        sse_QMARK_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$fn__20034.invokeStatic(object3, object4);
    }
}

