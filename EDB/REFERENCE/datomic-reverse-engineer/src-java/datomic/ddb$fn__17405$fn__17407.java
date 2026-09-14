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

public final class ddb$fn__17405$fn__17407
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Object const__5 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.AttributeValue");

    public Object invoke(Object m, Object p__17406) {
        Object object = p__17406;
        p__17406 = null;
        Object vec__17408 = object;
        Object k = RT.nth((Object)vec__17408, (int)RT.intCast((long)0L), null);
        Object object2 = vec__17408;
        vec__17408 = null;
        Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = m;
        m = null;
        Object object4 = k;
        k = null;
        Object object5 = v;
        v = null;
        ddb$fn__17405$fn__17407 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object3, object4, ((IFn)const__4.getRawRoot()).invoke(object5, const__5));
    }
}

