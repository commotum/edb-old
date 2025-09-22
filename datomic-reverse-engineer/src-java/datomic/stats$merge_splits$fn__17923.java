/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$merge_splits$fn$reify__17924;

public final class stats$merge_splits$fn__17923
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"merge-data");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 218, RT.keyword(null, (String)"column"), 6});

    public Object invoke(Object p1__17921_SHARP_, Object p2__17922_SHARP_) {
        Object object = p1__17921_SHARP_;
        p1__17921_SHARP_ = null;
        Object object2 = p2__17922_SHARP_;
        p2__17922_SHARP_ = null;
        stats$merge_splits$fn__17923 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)((IObj)new stats$merge_splits$fn$reify__17924(null)).withMeta((IPersistentMap)const__5), object, object2);
    }
}

