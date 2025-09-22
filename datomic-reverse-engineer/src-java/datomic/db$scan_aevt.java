/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$scan_aevt$fn__13258;

public final class db$scan_aevt
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__4 = RT.var((String)"datomic.btset", (String)"seek");

    public static Object invokeStatic(Object aevt2, Object attrid) {
        Object d = ((IFn.LLOLO)const__0.getRawRoot()).invokePrim(Long.MIN_VALUE, RT.uncheckedLongCast((Object)((Number)attrid)), null, Long.MAX_VALUE / 4L);
        Object object = attrid;
        attrid = null;
        Object object2 = aevt2;
        aevt2 = null;
        Object object3 = d;
        d = null;
        return ((IFn)const__3.getRawRoot()).invoke((Object)new db$scan_aevt$fn__13258(object), ((IFn)const__4.getRawRoot()).invoke(object2, object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$scan_aevt.invokeStatic(object3, object4);
    }
}

