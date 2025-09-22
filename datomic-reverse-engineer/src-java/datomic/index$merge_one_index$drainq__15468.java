/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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

public final class index$merge_one_index$drainq__15468
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"peek");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pop");

    public Object invoke(Object es, Object rs, Object erq) {
        while (true) {
            Object object = ((IFn)const__0.getRawRoot()).invoke(erq);
            if (object != null && object != Boolean.FALSE) break;
            Object vec__15469 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(erq));
            Object nes = RT.nth((Object)vec__15469, (int)RT.uncheckedIntCast((long)0L), null);
            Object object2 = vec__15469;
            vec__15469 = null;
            Object nrs = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
            Object object3 = es;
            es = null;
            Object object4 = nes;
            nes = null;
            Object object5 = rs;
            rs = null;
            Object object6 = nrs;
            nrs = null;
            Object object7 = erq;
            erq = null;
            erq = ((IFn)const__7.getRawRoot()).invoke(object7);
            rs = ((IFn)const__6.getRawRoot()).invoke(object5, object6);
            es = ((IFn)const__6.getRawRoot()).invoke(object3, object4);
        }
        Object object = es;
        es = null;
        Object object8 = rs;
        rs = null;
        Object object9 = erq;
        erq = null;
        return Tuple.create((Object)object, (Object)object8, (Object)object9);
    }
}

