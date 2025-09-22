/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class fressian$fressian_val
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"gzip-buffer");
    public static final Var const__1 = RT.var((String)"datomic.fressian", (String)"byte-buf");
    public static final Keyword const__2 = RT.keyword(null, (String)"handlers");
    public static final Keyword const__3 = RT.keyword(null, (String)"footer");

    public static Object invokeStatic(Object val, Object handlers) {
        Object object = val;
        val = null;
        Object object2 = handlers;
        handlers = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)const__2, object2, (Object)const__3, (Object)Boolean.TRUE));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fressian$fressian_val.invokeStatic(object3, object4);
    }
}

