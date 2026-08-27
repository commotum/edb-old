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
import datomic.iter.IterCat;

public final class iter$concat
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object iters) {
        IterCat iterCat;
        Object vec__11761;
        Object object = iters;
        iters = null;
        Object object2 = vec__11761 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object);
        vec__11761 = null;
        Object seq__11762 = ((IFn)const__2.getRawRoot()).invoke(object2);
        Object first__11763 = ((IFn)const__3.getRawRoot()).invoke(seq__11762);
        Object object3 = seq__11762;
        seq__11762 = null;
        Object seq__117622 = ((IFn)const__4.getRawRoot()).invoke(object3);
        Object object4 = first__11763;
        first__11763 = null;
        Object iter2 = object4;
        Object object5 = seq__117622;
        seq__117622 = null;
        Object iters2 = object5;
        Object object6 = iter2;
        if (object6 != null && object6 != Boolean.FALSE) {
            iter2 = null;
            iters2 = null;
            iterCat = new IterCat(iter2, iters2);
        } else {
            iterCat = null;
        }
        return iterCat;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return iter$concat.invokeStatic(object2);
    }
}

