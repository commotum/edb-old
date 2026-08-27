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

public final class log$excise_root$fn__16447
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"ExciseIOQueueCount");

    public Object invoke(Object p1__16437_SHARP_) {
        Object object = p1__16437_SHARP_;
        p1__16437_SHARP_ = null;
        log$excise_root$fn__16447 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, object);
    }
}

