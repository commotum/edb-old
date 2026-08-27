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

public final class index$build_segs$fn__15375
extends AFunction {
    Object cstore;
    Object segs_written_ref;
    Object write_handlers;
    Object olookup;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"build-psegs");

    public index$build_segs$fn__15375(Object object, Object object2, Object object3, Object object4) {
        this.cstore = object;
        this.segs_written_ref = object2;
        this.write_handlers = object3;
        this.olookup = object4;
    }

    public Object invoke(Object es, Object pd) {
        Object object = pd;
        pd = null;
        Object object2 = es;
        es = null;
        index$build_segs$fn__15375 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.cstore, this_.olookup, object, object2, this_.write_handlers, this_.segs_written_ref);
    }
}

