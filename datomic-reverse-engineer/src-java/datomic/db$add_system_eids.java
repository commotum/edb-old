/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$add_system_eids$fn__12495;

public final class db$add_system_eids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"system-eids");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"nil?");
    public static final AFn const__10 = (AFn)Tuple.create((Object)RT.keyword((String)"db.type", (String)"tuple"), (Object)RT.keyword((String)"db.attr", (String)"preds"), (Object)RT.keyword((String)"db", (String)"ensure"));

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        Object object2 = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)new db$add_system_eids$fn__12495(object2)), ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot())), (Object)const__10));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$add_system_eids.invokeStatic(object2);
    }
}

