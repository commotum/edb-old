/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.common$distinct_by$fn__9243$fn__9244;

public final class common$distinct_by$fn__9243
extends AFunction {
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"volatile!");

    public common$distinct_by$fn__9243(Object object) {
        this.f = object;
    }

    public Object invoke(Object rf) {
        Object seen;
        Object object = seen = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY);
        seen = null;
        Object object2 = rf;
        rf = null;
        return new common$distinct_by$fn__9243$fn__9244(object, this.f, object2);
    }
}

