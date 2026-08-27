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

public final class index$merge_one_index$f__15304__auto____15473$fn__15500
extends AFunction {
    Object cmp;
    Object cstore;
    Object retractions;
    Object insert_data;
    Object olookup;
    Object tailp;
    Object partfn;
    Object write_handlers;
    Object segs_written_ref;
    Object db;
    Object segid;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.cache", (String)"getx-uncached");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"merge-data");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"build-segs");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"deliver");

    public index$merge_one_index$f__15304__auto____15473$fn__15500(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
        this.cmp = object;
        this.cstore = object2;
        this.retractions = object3;
        this.insert_data = object4;
        this.olookup = object5;
        this.tailp = object6;
        this.partfn = object7;
        this.write_handlers = object8;
        this.segs_written_ref = object9;
        this.db = object10;
        this.segid = object11;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            Object seg_data;
            Object object3;
            Object and__5236__auto__15502;
            Object object4 = and__5236__auto__15502 = this.segid;
            if (object4 != null && object4 != Boolean.FALSE) {
                object3 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this.olookup, this.segid));
            } else {
                object3 = and__5236__auto__15502;
                seg_data = null;
            }
            Object object5 = seg_data = object3;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = seg_data;
                seg_data = null;
                object2 = ((IFn)const__2.getRawRoot()).invoke(this.cmp, object6, this.insert_data);
            } else {
                object2 = this.insert_data;
            }
            Object mdata = object2;
            Object object7 = mdata;
            mdata = null;
            Object object8 = this.retractions;
            object = ((IFn)const__3.getRawRoot()).invoke(this.db, this.cstore, this.olookup, object7, (Object)PersistentVector.EMPTY, (Object)(object8 != null && object8 != Boolean.FALSE ? PersistentVector.EMPTY : null), this.partfn, this.write_handlers, this.segs_written_ref);
        }
        catch (Throwable t2) {
            ((IFn)const__4.getRawRoot()).invoke(this.tailp, (Object)t2);
            Object t2 = null;
            throw t2;
        }
        return object;
    }
}

