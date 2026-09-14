/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.log.mem;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class Log$fn__20878
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"get-in");
    public static final AFn const__3 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"header"), (Object)RT.keyword(null, (String)"next-t"));

    public Object invoke(Object p1__20868_SHARP_) {
        Object object = p1__20868_SHARP_;
        p1__20868_SHARP_ = null;
        Log$fn__20878 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__3);
    }
}

