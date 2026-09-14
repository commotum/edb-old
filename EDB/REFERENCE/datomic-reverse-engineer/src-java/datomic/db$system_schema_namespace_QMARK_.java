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

public final class db$system_schema_namespace_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"starts-with?");

    public static Object invokeStatic(Object s) {
        Object object;
        boolean or__5238__auto__13182 = Util.equiv((Object)"db", (Object)s);
        if (or__5238__auto__13182) {
            object = or__5238__auto__13182 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            boolean or__5238__auto__13181 = Util.equiv((Object)"fressian", (Object)s);
            if (or__5238__auto__13181) {
                object = or__5238__auto__13181 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object2 = s;
                s = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object2, (Object)"db.");
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$system_schema_namespace_QMARK_.invokeStatic(object2);
    }
}

