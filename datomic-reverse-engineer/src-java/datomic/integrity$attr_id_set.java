/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.integrity$attr_id_set$fn__21998;

public final class integrity$attr_id_set
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__2 = RT.keyword(null, (String)"id");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__4 = RT.var((String)"datomic.api", (String)"q");
    public static final AFn const__5 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"find"), (Object)Symbol.intern(null, (String)"?e"), (Object)RT.keyword(null, (String)"where"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"?e"), (Object)RT.keyword((String)"db", (String)"valueType")));

    public static Object invokeStatic(Object db2, Object pred2) {
        Object object = pred2;
        pred2 = null;
        integrity$attr_id_set$fn__21998 integrity$attr_id_set$fn__21998 = new integrity$attr_id_set$fn__21998(db2);
        Object object2 = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)const__2, ((IFn)const__3.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke((Object)integrity$attr_id_set$fn__21998, ((IFn)const__4.getRawRoot()).invoke((Object)const__5, object2)))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$attr_id_set.invokeStatic(object3, object4);
    }
}

