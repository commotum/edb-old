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

public final class datafy$fn__17192
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"map-property-types");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"type-descriptor");

    public static Object invokeStatic(Object container, Object prop, Object type) {
        Object object;
        Object temp__5455__auto__17197;
        Object object2 = container;
        container = null;
        Object object3 = prop;
        prop = null;
        Object object4 = temp__5455__auto__17197 = ((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)object2, (Object)object3));
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__17197;
            temp__5455__auto__17197 = null;
            Object vec__17193 = object5;
            Object k = RT.nth((Object)vec__17193, (int)RT.intCast((long)0L), null);
            Object object6 = vec__17193;
            vec__17193 = null;
            Object v = RT.nth((Object)object6, (int)RT.intCast((long)1L), null);
            Object[] objectArray = new Object[2];
            Object object7 = k;
            k = null;
            objectArray[0] = object7;
            Object object8 = v;
            v = null;
            objectArray[1] = ((IFn)const__4.getRawRoot()).invoke(object8);
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            Object object9 = type;
            type = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object9);
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
        return datafy$fn__17192.invokeStatic(object4, object5, object6);
    }
}

