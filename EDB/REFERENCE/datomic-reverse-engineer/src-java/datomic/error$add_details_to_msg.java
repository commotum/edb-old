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
import datomic.error$add_details_to_msg$fn__674;

public final class error$add_details_to_msg
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*print-length*");
    public static final Object const__4 = 10L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"*print-level*");
    public static final Object const__6 = 5L;

    public static Object invokeStatic(Object msg, Object details) {
        Object object;
        Object object2 = details;
        if (object2 != null && object2 != Boolean.FALSE) {
            IFn iFn = (IFn)const__0.getRawRoot();
            Object object3 = msg;
            msg = null;
            ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3, const__4, (Object)const__5, const__6));
            Object object4 = details;
            details = null;
            object = iFn.invoke(object3, (Object)"\n", ((IFn)new error$add_details_to_msg$fn__674(object4)).invoke());
        } else {
            object = msg;
            Object object5 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return error$add_details_to_msg.invokeStatic(object3, object4);
    }
}

