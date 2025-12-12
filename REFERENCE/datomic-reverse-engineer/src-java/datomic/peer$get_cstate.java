/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class peer$get_cstate
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Object const__1 = 0L;
    public static final Keyword const__3 = RT.keyword((String)"datomic.peer", (String)"shutdown");
    public static final Var const__4 = RT.var((String)"datomic.error", (String)"state");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"connection-released");

    public static Object invokeStatic(Object state_ref, Object or_else) {
        Object object;
        Object object2 = state_ref;
        state_ref = null;
        Object object3 = or_else;
        or_else = null;
        Object state2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(object2), const__1, object3);
        if (Util.equiv((Object)state2, (Object)const__3)) {
            object = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)"The connection has been released.");
        } else {
            object = state2;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$get_cstate.invokeStatic(object3, object4);
    }
}

