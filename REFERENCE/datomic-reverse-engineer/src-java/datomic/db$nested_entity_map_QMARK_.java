/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.DbId;
import java.util.Map;

public final class db$nested_entity_map_QMARK_
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"attribute");

    public static Object invokeStatic(Object db2, Object attrid, Object v) {
        Object object;
        boolean and__5236__auto__13689 = v instanceof Map;
        if (and__5236__auto__13689) {
            Object and__5236__auto__13688;
            Object object2 = v;
            v = null;
            Object object3 = and__5236__auto__13688 = ((IFn)const__2.getRawRoot()).invoke((Object)(object2 instanceof DbId ? Boolean.TRUE : Boolean.FALSE));
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = db2;
                db2 = null;
                Object object5 = attrid;
                attrid = null;
                object = Util.equiv((long)20L, (Object)((Attribute)((IFn)db$nested_entity_map_QMARK_.const__6.getRawRoot()).invoke((Object)object4, (Object)object5)).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object = and__5236__auto__13688;
                and__5236__auto__13688 = null;
            }
        } else {
            object = and__5236__auto__13689 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$nested_entity_map_QMARK_.invokeStatic(object4, object5, object6);
    }
}

