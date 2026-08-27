/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class db$trim_log$fn__13390
extends AFunction {
    Object t;
    public static final Keyword const__0 = RT.keyword(null, (String)"t");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"key-comparator");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"subvec");

    public db$trim_log$fn__13390(Object object) {
        this.t = object;
    }

    public Object invoke(Object p1__13389_SHARP_) {
        int idx = Collections.binarySearch((List)p1__13389_SHARP_, RT.mapUniqueKeys((Object[])new Object[]{const__0, this_.t}), (Comparator)((IFn)const__1.getRawRoot()).invoke((Object)const__0));
        Object object = p1__13389_SHARP_;
        p1__13389_SHARP_ = null;
        db$trim_log$fn__13390 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__3.getRawRoot()).invoke(object, (Object)((long)idx < 0L ? (Number)Numbers.num((long)Numbers.unchecked_minus((long)((long)idx + 1L))) : (Number)Numbers.num((long)Numbers.unchecked_inc((long)idx)))));
    }
}

