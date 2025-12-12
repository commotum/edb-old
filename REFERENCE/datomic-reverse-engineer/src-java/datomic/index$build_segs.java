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
import datomic.index$build_segs$fn__15375;

public final class index$build_segs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"filter-nohist-pairs");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"separating-retractions");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"fully-partition-by");
    public static final Var const__4 = RT.var((String)"datomic.index", (String)"fred");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object db2, Object cstore, Object olookup, Object data2, Object es, Object retractions, Object partfn, Object write_handlers2, Object segs_written_ref) {
        Object es2;
        Object object;
        Object retref = ((IFn)const__0.getRawRoot()).invoke(retractions);
        Object object2 = data2;
        data2 = null;
        Object data3 = ((IFn)const__1.getRawRoot()).invoke(db2, object2);
        Object object3 = retractions;
        retractions = null;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = db2;
            db2 = null;
            Object object5 = data3;
            data3 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object4, retref, object5);
        } else {
            object = data3;
            data3 = null;
        }
        Object data4 = object;
        Object object6 = partfn;
        partfn = null;
        Object object7 = data4;
        data4 = null;
        Object pdata = ((IFn)const__3.getRawRoot()).invoke(object6, object7);
        Object object8 = cstore;
        cstore = null;
        Object object9 = segs_written_ref;
        segs_written_ref = null;
        Object object10 = write_handlers2;
        write_handlers2 = null;
        Object object11 = olookup;
        olookup = null;
        Object object12 = es;
        es = null;
        Object object13 = pdata;
        pdata = null;
        Object object14 = es2 = ((IFn)const__4.getRawRoot()).invoke((Object)new index$build_segs$fn__15375(object8, object9, object10, object11), object12, object13);
        es2 = null;
        Object object15 = retref;
        retref = null;
        return Tuple.create((Object)object14, (Object)((IFn)const__5.getRawRoot()).invoke(object15));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        Object object10 = object;
        object = null;
        Object object11 = object2;
        object2 = null;
        Object object12 = object3;
        object3 = null;
        Object object13 = object4;
        object4 = null;
        Object object14 = object5;
        object5 = null;
        Object object15 = object6;
        object6 = null;
        Object object16 = object7;
        object7 = null;
        Object object17 = object8;
        object8 = null;
        Object object18 = object9;
        object9 = null;
        return index$build_segs.invokeStatic(object10, object11, object12, object13, object14, object15, object16, object17, object18);
    }
}

