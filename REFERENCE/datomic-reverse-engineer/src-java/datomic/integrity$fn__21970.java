/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$fn__21970
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"create");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__2 = RT.var((String)"datomic.uri", (String)"parse");
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"skip-efs"), Boolean.TRUE});

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object), (Object)const__4));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$fn__21970.invokeStatic(object2);
    }
}

