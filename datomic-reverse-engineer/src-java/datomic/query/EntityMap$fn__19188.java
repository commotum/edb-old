/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class EntityMap$fn__19188
extends AFunction {
    Object edits;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");

    public EntityMap$fn__19188(Object object) {
        this.edits = object;
    }

    public Object invoke(Object p__19187) {
        Object object = p__19187;
        p__19187 = null;
        Object vec__19189 = object;
        Object k = RT.nth((Object)vec__19189, (int)RT.intCast((long)0L), null);
        Object object2 = vec__19189;
        vec__19189 = null;
        RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = k;
        k = null;
        EntityMap$fn__19188 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(this_.edits, object3);
    }
}

