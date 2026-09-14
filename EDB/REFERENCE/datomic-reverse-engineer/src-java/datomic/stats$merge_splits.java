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
import datomic.stats$merge_splits$fn__17923;

public final class stats$merge_splits
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object splits) {
        Object object = splits;
        splits = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new stats$merge_splits$fn__17923(), object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return stats$merge_splits.invokeStatic(object2);
    }
}

