/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$find_last_tx$fn__13597;
import datomic.impl.db.IDatum;

public final class db$find_last_tx
extends AFunction
implements IFn.LOOL {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__5 = 0L;
    public static final Object const__6 = -1L;

    public static long invokeStatic(long nextT, Object mid_index, Object object) {
        Object d;
        Object object2 = mid_index;
        mid_index = null;
        Object object3 = object;
        object = null;
        Object object4 = d = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke((Object)new db$find_last_tx$fn__13597(object2, object3), ((IFn)const__3.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.unchecked_dec((long)nextT)), const__5, const__6)));
        d = null;
        return ((IDatum)object4).getT();
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object2;
        object2 = null;
        Object object5 = object3;
        object3 = null;
        return new Long(db$find_last_tx.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)), object4, object5));
    }

    public final long invokePrim(long l, Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$find_last_tx.invokeStatic(l, object3, object4);
    }
}

