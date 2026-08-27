/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class ddb_values$split_map_keys_with$fn__20373
extends AFunction {
    Object pred;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");

    public ddb_values$split_map_keys_with$fn__20373(Object object) {
        this.pred = object;
    }

    public Object invoke(Object p__20371, Object p__20372) {
        IPersistentVector iPersistentVector;
        Object object = p__20371;
        p__20371 = null;
        Object vec__20374 = object;
        Object match = RT.nth((Object)vec__20374, (int)RT.intCast((long)0L), null);
        Object object2 = vec__20374;
        vec__20374 = null;
        Object nomatch = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = p__20372;
        p__20372 = null;
        Object vec__20377 = object3;
        Object k = RT.nth((Object)vec__20377, (int)RT.intCast((long)0L), null);
        Object object4 = vec__20377;
        vec__20377 = null;
        Object v = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
        Object object5 = ((IFn)this.pred).invoke(k);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = match;
            match = null;
            Object object7 = k;
            k = null;
            Object object8 = v;
            v = null;
            Object object9 = nomatch;
            nomatch = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__3.getRawRoot()).invoke(object6, object7, object8), (Object)object9);
        } else {
            Object object10 = match;
            match = null;
            Object object11 = nomatch;
            nomatch = null;
            Object object12 = k;
            k = null;
            Object object13 = v;
            v = null;
            iPersistentVector = Tuple.create((Object)object10, (Object)((IFn)const__3.getRawRoot()).invoke(object11, object12, object13));
        }
        return iPersistentVector;
    }
}

