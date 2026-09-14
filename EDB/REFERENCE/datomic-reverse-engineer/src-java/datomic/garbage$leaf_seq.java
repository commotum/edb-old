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
import datomic.garbage$leaf_seq$fn__19770;

public final class garbage$leaf_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__1 = RT.var((String)"datomic.garbage", (String)"dir-seq");

    public static Object invokeStatic(Object lookup, Object root) {
        garbage$leaf_seq$fn__19770 garbage$leaf_seq$fn__19770 = new garbage$leaf_seq$fn__19770(lookup);
        Object object = lookup;
        lookup = null;
        Object object2 = root;
        root = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)garbage$leaf_seq$fn__19770, ((IFn)const__1.getRawRoot()).invoke(object, object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$leaf_seq.invokeStatic(object3, object4);
    }
}

