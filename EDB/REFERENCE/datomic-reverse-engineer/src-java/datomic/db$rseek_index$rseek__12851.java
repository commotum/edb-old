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

public final class db$rseek_index$rseek__12851
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"reversed-iter");
    public static final Var const__1 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__2 = RT.var((String)"datomic.btset", (String)"seek-last");

    public Object invoke(Object idx, Object d) {
        Object object;
        Object or__5238__auto__12853;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = d;
        d = null;
        Object object3 = or__5238__auto__12853 = ((IFn)const__1.getRawRoot()).invoke(idx, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__12853;
            or__5238__auto__12853 = null;
        } else {
            Object object4 = idx;
            idx = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object4);
        }
        db$rseek_index$rseek__12851 this_ = null;
        return iFn.invoke(object);
    }
}

