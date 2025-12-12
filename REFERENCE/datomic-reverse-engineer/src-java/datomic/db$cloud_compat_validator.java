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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.Datom;

public final class db$cloud_compat_validator
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"on-prem-only-aids");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"on-prem-only-vts");
    public static final Var const__4 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"not-supported-in-cloud");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        Object temp__5455__auto__13346;
        Object object2;
        Object or__5238__auto__13345;
        Object a = ((Datom)d).a();
        Object object3 = or__5238__auto__13345 = ((IFn)const__0.getRawRoot()).invoke(a);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__13345;
            or__5238__auto__13345 = null;
        } else {
            Object object4 = a;
            a = null;
            boolean and__5236__auto__13344 = Util.equiv((long)40L, (Object)object4);
            if (and__5236__auto__13344) {
                Object object5 = d;
                d = null;
                object2 = ((IFn)const__3.getRawRoot()).invoke(((Datom)object5).v());
            } else {
                object2 = and__5236__auto__13344 ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        Object object6 = temp__5455__auto__13346 = object2;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = temp__5455__auto__13346;
            temp__5455__auto__13346 = null;
            Object problem = object7;
            Object object8 = db2;
            db2 = null;
            Object object9 = problem;
            problem = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, ((IFn)const__6.getRawRoot()).invoke(((Database)object8).ident(object9), (Object)" is not supported in Datomic Cloud."));
        } else {
            object = Boolean.TRUE;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$cloud_compat_validator.invokeStatic(object3, object4);
    }
}

