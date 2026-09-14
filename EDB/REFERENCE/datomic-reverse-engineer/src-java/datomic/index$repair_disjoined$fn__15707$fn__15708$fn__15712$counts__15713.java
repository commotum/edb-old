/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713$fn__15715;

public final class index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public Object invoke(Object p1__15702_SHARP_) {
        Object object = p1__15702_SHARP_;
        p1__15702_SHARP_ = null;
        index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713$fn__15715(), (Object)PersistentArrayMap.EMPTY, object);
    }
}

