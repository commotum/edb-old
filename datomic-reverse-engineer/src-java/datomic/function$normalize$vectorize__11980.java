/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.List;

public final class function$normalize$vectorize__11980
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into");

    public Object invoke(Object o) {
        Object object;
        if (o instanceof List) {
            Object object2 = o;
            o = null;
            function$normalize$vectorize__11980 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)PersistentVector.EMPTY, object2);
        } else {
            object = o;
            Object var1_1 = null;
        }
        return object;
    }
}

