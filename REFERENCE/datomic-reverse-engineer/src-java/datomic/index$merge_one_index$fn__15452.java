/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$merge_one_index$fn__15452
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"IndexIOQueueCount");

    public Object invoke(Object p1__15440_SHARP_) {
        Object object = p1__15440_SHARP_;
        p1__15440_SHARP_ = null;
        index$merge_one_index$fn__15452 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, object);
    }
}

