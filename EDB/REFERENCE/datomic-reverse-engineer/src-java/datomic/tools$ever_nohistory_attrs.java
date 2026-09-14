/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class tools$ever_nohistory_attrs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"datomic.api", (String)"q");
    public static final AFn const__4 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"find"), (Object)Symbol.intern(null, (String)"?e"), (Object)RT.keyword(null, (String)"where"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"?e"), (Object)RT.keyword((String)"db", (String)"noHistory"), (Object)Boolean.TRUE));
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"history");

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke((Object)const__4, ((IFn)const__5.getRawRoot()).invoke(object))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$ever_nohistory_attrs.invokeStatic(object2);
    }
}

