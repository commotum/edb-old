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

public final class db$init_db$assert_kw__14140
extends AFunction {
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"require-kw");

    public Object invoke(Object p__14139) {
        Object id;
        Object object = p__14139;
        p__14139 = null;
        Object vec__14141 = object;
        Object k = RT.nth((Object)vec__14141, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__14141;
        vec__14141 = null;
        Object object3 = id = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        id = null;
        Object object4 = k;
        k = null;
        return ((IFn.LLOLO)const__3.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object3)), 10L, ((IFn)const__5.getRawRoot()).invoke(object4), 0L);
    }
}

