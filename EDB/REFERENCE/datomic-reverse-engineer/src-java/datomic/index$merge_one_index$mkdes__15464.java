/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$merge_one_index$mkdes__15464$fn__15465;
import datomic.index.DirNode;

public final class index$merge_one_index$mkdes__15464
extends AFunction {
    Object pario;
    Object cstore;
    Object xsegs;
    Object olookup;
    Object excise_QMARK_;
    Object write_handlers;
    Object segs_written_ref;
    Object filter_segids;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"range");

    public index$merge_one_index$mkdes__15464(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.pario = object;
        this.cstore = object2;
        this.xsegs = object3;
        this.olookup = object4;
        this.excise_QMARK_ = object5;
        this.write_handlers = object6;
        this.segs_written_ref = object7;
        this.filter_segids = object8;
    }

    public Object invoke(Object dir) {
        index$merge_one_index$mkdes__15464$fn__15465 index$merge_one_index$mkdes__15464$fn__15465 = new index$merge_one_index$mkdes__15464$fn__15465(this_.pario, this_.cstore, this_.xsegs, this_.olookup, this_.excise_QMARK_, this_.write_handlers, this_.segs_written_ref, dir, this_.filter_segids);
        Object object = dir;
        dir = null;
        index$merge_one_index$mkdes__15464 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)index$merge_one_index$mkdes__15464$fn__15465, (Object)PersistentVector.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)RT.count((Object)((DirNode)object).segids)));
    }
}

