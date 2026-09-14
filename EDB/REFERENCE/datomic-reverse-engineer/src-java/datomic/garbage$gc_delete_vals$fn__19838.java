/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class garbage$gc_delete_vals$fn__19838
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.garbage", (String)"pace-gc");
    public static final Keyword const__3 = RT.keyword(null, (String)"ok");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"deref");

    public Object invoke(Object count2, Object fut) {
        ((IFn)const__0.getRawRoot()).invoke();
        Object object = count2;
        count2 = null;
        Object object2 = fut;
        fut = null;
        garbage$gc_delete_vals$fn__19838 this_ = null;
        return Numbers.add((Object)object, (long)(Util.equiv((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke(object2)) ? 1L : 0L));
    }
}

