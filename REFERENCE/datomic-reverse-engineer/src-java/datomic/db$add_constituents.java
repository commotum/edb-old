/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$add_constituents$fn__13127;
import datomic.db$add_constituents$fn__13129;

public final class db$add_constituents
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__3 = RT.keyword(null, (String)"constituents");

    public static Object invokeStatic(Object db2, Object comp_id, Object constituents) {
        Object object = comp_id;
        comp_id = null;
        Object object2 = constituents;
        constituents = null;
        Object m = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new db$add_constituents$fn__13127(object, db2)), object2);
        Object object3 = db2;
        db2 = null;
        Object object4 = m;
        m = null;
        return ((IFn)const__2.getRawRoot()).invoke(object3, (Object)const__3, (Object)new db$add_constituents$fn__13129(object4));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$add_constituents.invokeStatic(object4, object5, object6);
    }
}

