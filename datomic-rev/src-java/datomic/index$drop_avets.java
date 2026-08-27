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
import datomic.index$drop_avets$fn__15614;

public final class index$drop_avets
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object store, Object olookup, Object root_id2, Object attrids, Object garbage2) {
        Object object = store;
        store = null;
        Object object2 = olookup;
        olookup = null;
        Object object3 = root_id2;
        root_id2 = null;
        Object object4 = garbage2;
        garbage2 = null;
        Object object5 = attrids;
        attrids = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new index$drop_avets$fn__15614(object, object2), (Object)Tuple.create((Object)object3, (Object)object4), object5);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return index$drop_avets.invokeStatic(object6, object7, object8, object9, object10);
    }
}

