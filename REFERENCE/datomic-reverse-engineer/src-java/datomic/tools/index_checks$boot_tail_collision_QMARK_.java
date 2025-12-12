/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;

public final class index_checks$boot_tail_collision_QMARK_
extends AFunction {
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"BOOT-IDS");
    public static final Keyword const__5 = RT.keyword((String)"db.bootstrap", (String)"part");

    public static Object invokeStatic(Object p__21890) {
        Object object = p__21890;
        p__21890 = null;
        Object vec__21891 = object;
        Object d1 = RT.nth((Object)vec__21891, (int)RT.intCast((long)0L), null);
        Object object2 = vec__21891;
        vec__21891 = null;
        RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = d1;
        d1 = null;
        return Util.equiv((Object)((Datom)object3).e(), (Object)((IFn)const__4.getRawRoot()).invoke((Object)const__5)) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index_checks$boot_tail_collision_QMARK_.invokeStatic(object2);
    }
}

