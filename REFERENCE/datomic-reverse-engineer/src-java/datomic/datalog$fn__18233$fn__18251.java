/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18251
extends AFunction {
    Object startv;
    Object bound;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"asserting-datum");

    public datalog$fn__18233$fn__18251(Object object, Object object2) {
        this.startv = object;
        this.bound = object2;
    }

    public Object invoke(Object d) {
        Object object;
        IFn.LLOLO lLOLO = (IFn.LLOLO)const__0.getRawRoot();
        long l = ((IDatum)d).getE();
        long l2 = ((IDatum)d).getA();
        Object object2 = RT.aget((Object[])((Object[])this.bound), (int)((int)2L));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = d;
            d = null;
            object = ((IDatum)object3).getV();
        } else {
            Object object4 = this.startv;
            object = object4 != null && object4 != Boolean.FALSE ? this.startv : null;
        }
        return lLOLO.invokePrim(l, l2, object, 0x1FFFFFFFFFFFFFFFL);
    }
}

