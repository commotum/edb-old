/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Datom;

public final class db$unique_violator$fn__13144
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduced");

    public Object invoke(Object d1, Object d2) {
        Object object;
        if (((IFn.OOL)const__1.getRawRoot()).invokePrim(((Datom)d1).v(), ((Datom)d2).v()) == 0L) {
            Object object2 = d1;
            d1 = null;
            Object object3 = d2;
            d2 = null;
            db$unique_violator$fn__13144 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)Tuple.create((Object)object2, (Object)object3));
        } else {
            object = d2;
            Object var2_2 = null;
        }
        return object;
    }
}

