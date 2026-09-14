/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$write_object
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"write-vals");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"fress");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"common-write-handlers");
    public static final Var const__4 = RT.var((String)"datomic.cache", (String)"put");

    public static Object invokeStatic(Object store, Object olookup, Object v) {
        Object uuid = ((IFn)const__0.getRawRoot()).invoke();
        Object object = store;
        store = null;
        ((IFn)const__1.getRawRoot()).invoke(object, (Object)RT.mapUniqueKeys((Object[])new Object[]{uuid, ((IFn)const__2.getRawRoot()).invoke(v, const__3.getRawRoot())}));
        Object object2 = olookup;
        olookup = null;
        Object object3 = v;
        v = null;
        ((IFn)const__4.getRawRoot()).invoke(object2, uuid, object3);
        Object var3_3 = null;
        return uuid;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$write_object.invokeStatic(object4, object5, object6);
    }
}

