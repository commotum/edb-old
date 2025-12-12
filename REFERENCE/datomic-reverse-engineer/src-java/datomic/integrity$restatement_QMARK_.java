/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import datomic.Datom;

public final class integrity$restatement_QMARK_
extends AFunction {
    public static Object invokeStatic(Object p__21989) {
        Boolean bl;
        Object object = p__21989;
        p__21989 = null;
        Object vec__21990 = object;
        Object d1 = RT.nth((Object)vec__21990, (int)RT.intCast((long)0L), null);
        Object object2 = vec__21990;
        vec__21990 = null;
        Object d2 = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        boolean and__5236__auto__21996 = Util.equiv((Object)((Datom)d1).e(), (Object)((Datom)d2).e());
        if (and__5236__auto__21996) {
            boolean and__5236__auto__21995 = Util.equiv((Object)((Datom)d1).a(), (Object)((Datom)d2).a());
            if (and__5236__auto__21995) {
                boolean and__5236__auto__21994 = Util.equiv((Object)((Datom)d1).v(), (Object)((Datom)d2).v());
                if (and__5236__auto__21994) {
                    Object object3 = d1;
                    d1 = null;
                    Object object4 = d2;
                    d2 = null;
                    bl = Util.equiv((boolean)((Datom)object3).added(), (boolean)((Datom)object4).added()) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    bl = and__5236__auto__21994 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__21995 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__21996 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$restatement_QMARK_.invokeStatic(object2);
    }
}

