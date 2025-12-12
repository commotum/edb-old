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
import datomic.index.DirNode;

public final class treewalk$fn__19720
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object dn) {
        Object object = dn;
        dn = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((DirNode)object).segids);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return treewalk$fn__19720.invokeStatic(object2);
    }
}

