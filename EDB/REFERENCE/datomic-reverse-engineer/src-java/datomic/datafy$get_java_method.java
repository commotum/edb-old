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
import datomic.datafy$get_java_method$fn__17314;

public final class datafy$get_java_method
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object cls, Object mname, Object arity, Object types) {
        Object object;
        Object object2 = mname;
        mname = null;
        Object object3 = types;
        types = null;
        Object object4 = arity;
        arity = null;
        Object object5 = cls;
        cls = null;
        Object ms = ((IFn)const__0.getRawRoot()).invoke((Object)new datafy$get_java_method$fn__17314(object2, object3, object4), (Object)((Class)object5).getMethods());
        if (1L == (long)RT.count((Object)ms)) {
            Object object6 = ms;
            ms = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object6);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return datafy$get_java_method.invokeStatic(object5, object6, object7, object8);
    }
}

