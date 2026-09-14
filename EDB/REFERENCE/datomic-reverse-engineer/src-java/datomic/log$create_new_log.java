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

public final class log$create_new_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"write-new-log");
    public static final Var const__4 = RT.var((String)"datomic.log", (String)"create-log-impl");

    public static Object invokeStatic(Object cs, Object olookup) {
        Object object;
        Object temp__5457__auto__16353;
        Object object2 = cs;
        cs = null;
        Object object3 = temp__5457__auto__16353 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__16353;
            temp__5457__auto__16353 = null;
            Object vec__16349 = object4;
            Object desc = RT.nth((Object)vec__16349, (int)RT.intCast((long)0L), null);
            Object object5 = vec__16349;
            vec__16349 = null;
            Object tail = RT.nth((Object)object5, (int)RT.intCast((long)1L), null);
            Object object6 = olookup;
            olookup = null;
            Object object7 = desc;
            desc = null;
            Object object8 = tail;
            tail = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object6, object7, object8);
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
        return log$create_new_log.invokeStatic(object3, object4);
    }
}

