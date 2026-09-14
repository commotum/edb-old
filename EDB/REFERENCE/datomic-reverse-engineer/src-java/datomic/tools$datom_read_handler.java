/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.IFn$OL
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class tools$datom_read_handler
extends AFunction {
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__7 = RT.var((String)"datomic.api", (String)"tx->t");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"retracting-datum");

    public static Object invokeStatic(Object p__21725) {
        Object object;
        Object op;
        Object object2 = p__21725;
        p__21725 = null;
        Object vec__21726 = object2;
        Object e = RT.nth((Object)vec__21726, (int)RT.intCast((long)0L), null);
        Object a = RT.nth((Object)vec__21726, (int)RT.intCast((long)1L), null);
        Object v = RT.nth((Object)vec__21726, (int)RT.intCast((long)2L), null);
        Object tx = RT.nth((Object)vec__21726, (int)RT.intCast((long)3L), null);
        Object object3 = vec__21726;
        vec__21726 = null;
        Object object4 = op = RT.nth((Object)object3, (int)RT.intCast((long)4L), null);
        op = null;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = e;
            e = null;
            Object object6 = a;
            a = null;
            Object object7 = v;
            v = null;
            Object object8 = tx;
            tx = null;
            object = ((IFn.LLOLO)const__6.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object5)), RT.longCast((Object)((Number)object6)), object7, ((IFn.OL)const__7.getRawRoot()).invokePrim(object8));
        } else {
            Object object9 = e;
            e = null;
            Object object10 = a;
            a = null;
            Object object11 = v;
            v = null;
            Object object12 = tx;
            tx = null;
            object = ((IFn.LLOLO)const__8.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object9)), RT.longCast((Object)((Number)object10)), object11, ((IFn.OL)const__7.getRawRoot()).invokePrim(object12));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$datom_read_handler.invokeStatic(object2);
    }
}

