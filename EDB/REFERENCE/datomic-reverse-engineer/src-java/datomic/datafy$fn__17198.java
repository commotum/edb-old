/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datafy$fn__17198
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"list-property-types");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"type-descriptor");

    public static Object invokeStatic(Object container, Object prop, Object type) {
        Object object;
        Object temp__5455__auto__17200;
        Object object2 = container;
        container = null;
        Object object3 = prop;
        prop = null;
        Object object4 = temp__5455__auto__17200 = ((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)object2, (Object)object3));
        if (object4 != null && object4 != Boolean.FALSE) {
            Object v;
            Object object5 = temp__5455__auto__17200;
            temp__5455__auto__17200 = null;
            Object object6 = v = object5;
            v = null;
            object = Tuple.create((Object)((IFn)const__1.getRawRoot()).invoke(object6));
        } else {
            Object object7 = type;
            type = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object7);
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
        return datafy$fn__17198.invokeStatic(object4, object5, object6);
    }
}

