/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.thread$pmap_n$step__21026$fn__21030;

public final class thread$pmap_n$step__21026
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");

    public Object invoke(Object p__21025, Object fs2) {
        Object object = p__21025;
        p__21025 = null;
        Object vec__21027 = object;
        Object seq__21028 = ((IFn)const__0.getRawRoot()).invoke(vec__21027);
        Object first__21029 = ((IFn)const__1.getRawRoot()).invoke(seq__21028);
        Object object2 = seq__21028;
        seq__21028 = null;
        Object seq__210282 = ((IFn)const__2.getRawRoot()).invoke(object2);
        Object object3 = first__21029;
        first__21029 = null;
        Object x = object3;
        Object object4 = seq__210282;
        seq__210282 = null;
        Object xs = object4;
        Object object5 = vec__21027;
        vec__21027 = null;
        Object vs = object5;
        Object object6 = xs;
        xs = null;
        Object object7 = fs2;
        fs2 = null;
        Object object8 = vs;
        vs = null;
        Object object9 = x;
        x = null;
        return new LazySeq((IFn)new thread$pmap_n$step__21026$fn__21030(object6, object7, (Object)this, object8, object9));
    }
}

