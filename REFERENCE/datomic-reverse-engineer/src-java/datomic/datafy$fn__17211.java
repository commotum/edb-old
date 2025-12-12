/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datafy$fn__17211$fn__17212;

public final class datafy$fn__17211
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"filter-overlapped-setters");
    public static final Var const__2 = RT.var((String)"datomic.datafy", (String)"setters");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"symbol");

    public static Object invokeStatic(Object c) {
        Object object;
        Object temp__5455__auto__17215;
        Object object2 = temp__5455__auto__17215 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(c)));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__17215;
            temp__5455__auto__17215 = null;
            Object s = object3;
            Object object4 = c;
            c = null;
            Object object5 = s;
            s = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)new datafy$fn__17211$fn__17212(object4), (Object)PersistentArrayMap.EMPTY, object5);
        } else {
            Object object6 = c;
            c = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)((Class)object6).getName());
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$fn__17211.invokeStatic(object2);
    }
}

