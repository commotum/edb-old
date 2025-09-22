/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datafy$define_method_to_fn$fn__17304
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");

    public Object invoke(Object idx, Object cls) {
        Object object = idx;
        idx = null;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)((Class)cls).getName());
        Object object3 = cls;
        cls = null;
        return Tuple.create((Object)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"x", (Object)Numbers.inc((Object)object))), (Object)object2, (Object)object3);
    }
}

