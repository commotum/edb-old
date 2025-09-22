/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class slf4j$metric_expr
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"event->timing");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"datomic.monitor", (String)"add-stat");

    public static Object invokeStatic(Object event, Object msec) {
        Object object;
        Object temp__5457__auto__8979;
        Object object2 = event;
        event = null;
        Object object3 = temp__5457__auto__8979 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object kw;
            Object object4 = temp__5457__auto__8979;
            temp__5457__auto__8979 = null;
            Object object5 = kw = object4;
            kw = null;
            Object object6 = msec;
            msec = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)const__4), ((IFn)const__3.getRawRoot()).invoke(object5), ((IFn)const__3.getRawRoot()).invoke(object6)));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return slf4j$metric_expr.invokeStatic(object3, object4);
    }
}

