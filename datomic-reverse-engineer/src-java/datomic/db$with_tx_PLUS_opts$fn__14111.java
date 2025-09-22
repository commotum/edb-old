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

public final class db$with_tx_PLUS_opts$fn__14111
extends AFunction {
    Object f;
    public static final Var const__0 = RT.var((String)"datomic.measure.io-trace", (String)"tracing-keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__5 = RT.keyword(null, (String)"hints");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__7 = RT.keyword(null, (String)"segments");

    public db$with_tx_PLUS_opts$fn__14111(Object object) {
        this.f = object;
    }

    public Object invoke() {
        Object vec__14112 = ((IFn)const__0.getRawRoot()).invoke(this_.f);
        Object ret = RT.nth((Object)vec__14112, (int)RT.uncheckedIntCast((long)0L), null);
        Object object = vec__14112;
        vec__14112 = null;
        Object segments = RT.nth((Object)object, (int)RT.uncheckedIntCast((long)1L), null);
        Object object2 = ret;
        ret = null;
        Object[] objectArray = new Object[2];
        objectArray[0] = const__7;
        Object object3 = segments;
        segments = null;
        objectArray[1] = object3;
        db$with_tx_PLUS_opts$fn__14111 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(object2, (Object)const__5, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

