/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class query$process_pulls$pull_QMARK___19384
extends AFunction {
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"pull");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");

    public Object invoke(Object p1__19379_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__19386 = p1__19379_SHARP_ instanceof List;
        if (and__5236__auto__19386) {
            Object object = p1__19379_SHARP_;
            p1__19379_SHARP_ = null;
            query$process_pulls$pull_QMARK___19384 this_ = null;
            bl = Util.equiv((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke(object)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__19386 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

