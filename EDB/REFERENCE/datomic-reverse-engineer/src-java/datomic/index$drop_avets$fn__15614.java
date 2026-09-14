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

public final class index$drop_avets$fn__15614
extends AFunction {
    Object store;
    Object olookup;
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"drop-avet");

    public index$drop_avets$fn__15614(Object object, Object object2) {
        this.store = object;
        this.olookup = object2;
    }

    public Object invoke(Object p__15613, Object aid) {
        Object object = p__15613;
        p__15613 = null;
        Object vec__15615 = object;
        Object root_id2 = RT.nth((Object)vec__15615, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__15615;
        vec__15615 = null;
        Object garbage2 = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = root_id2;
        root_id2 = null;
        Object object4 = aid;
        aid = null;
        Object object5 = garbage2;
        garbage2 = null;
        index$drop_avets$fn__15614 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(this_.store, this_.olookup, object3, object4, object5);
    }
}

