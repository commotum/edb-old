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
import datomic.iter.Iter;

public final class iter$iter_seq$fn__11745
extends AFunction {
    Object iter;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"iter-seq");

    public iter$iter_seq$fn__11745(Object object) {
        this.iter = object;
    }

    public Object invoke() {
        Object object;
        Object object2 = this_.iter;
        if (object2 != null && object2 != Boolean.FALSE) {
            this_.iter = null;
            iter$iter_seq$fn__11745 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((Iter)this_.iter).get(), ((IFn)const__1.getRawRoot()).invoke(((Iter)this_.iter).next()));
        } else {
            object = null;
        }
        return object;
    }
}

