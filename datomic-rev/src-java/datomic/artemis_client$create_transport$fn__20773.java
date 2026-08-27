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

public final class artemis_client$create_transport$fn__20773
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"integer?");

    public Object invoke(Object p__20772) {
        Object object;
        Object object2 = p__20772;
        p__20772 = null;
        Object vec__20774 = object2;
        Object k = RT.nth((Object)vec__20774, (int)RT.intCast((long)0L), null);
        Object object3 = vec__20774;
        vec__20774 = null;
        Object v = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = k;
        k = null;
        Object object5 = ((IFn)const__3.getRawRoot()).invoke(object4);
        Object object6 = ((IFn)const__4.getRawRoot()).invoke(v);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = v;
            v = null;
            object = RT.intCast((Object)object7);
        } else {
            object = v;
            v = null;
        }
        return Tuple.create((Object)object5, (Object)object);
    }
}

