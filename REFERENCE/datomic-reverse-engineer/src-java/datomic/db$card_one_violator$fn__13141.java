/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;

public final class db$card_one_violator$fn__13141
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduced");

    public Object invoke(Object d1, Object d2) {
        Object object;
        if (Util.equiv((Object)((Datom)d1).e(), (Object)((Datom)d2).e())) {
            Object object2 = d1;
            d1 = null;
            Object object3 = d2;
            d2 = null;
            db$card_one_violator$fn__13141 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)Tuple.create((Object)object2, (Object)object3));
        } else {
            object = d2;
            Object var2_2 = null;
        }
        return object;
    }
}

