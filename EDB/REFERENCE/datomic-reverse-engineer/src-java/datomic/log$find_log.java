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

public final class log$find_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"read-tail-descriptor");
    public static final Var const__4 = RT.var((String)"datomic.log", (String)"load-tail");
    public static final Var const__5 = RT.var((String)"datomic.log", (String)"create-log-impl");

    public static Object invokeStatic(Object cs, Object olookup) {
        Object object;
        Object temp__5457__auto__16359;
        Object object2 = cs;
        cs = null;
        Object object3 = temp__5457__auto__16359 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object buf;
            Object object4 = temp__5457__auto__16359;
            temp__5457__auto__16359 = null;
            Object vec__16355 = object4;
            Object desc = RT.nth((Object)vec__16355, (int)RT.intCast((long)0L), null);
            Object object5 = vec__16355;
            vec__16355 = null;
            Object object6 = buf = RT.nth((Object)object5, (int)RT.intCast((long)1L), null);
            buf = null;
            Object tail = ((IFn)const__4.getRawRoot()).invoke(object6);
            Object object7 = olookup;
            olookup = null;
            Object object8 = desc;
            desc = null;
            Object object9 = tail;
            tail = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object7, object8, object9);
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
        return log$find_log.invokeStatic(object3, object4);
    }
}

