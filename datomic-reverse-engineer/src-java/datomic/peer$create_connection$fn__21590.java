/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.peer$create_connection$fn__21590$fn__21591;

public final class peer$create_connection$fn__21590
extends AFunction {
    Object reconnect_fn;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"retry-fn");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Keyword const__2 = RT.keyword(null, (String)"reconnect");
    public static final Keyword const__3 = RT.keyword(null, (String)"pred");
    public static final Keyword const__4 = RT.keyword(null, (String)"backoff");
    public static final Object const__5 = 1000L;
    public static final Keyword const__6 = RT.keyword(null, (String)"log-retry");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"log-retry");
    public static final Keyword const__8 = RT.keyword(null, (String)"max-retries");

    public peer$create_connection$fn__21590(Object object) {
        this.reconnect_fn = object;
    }

    public Object invoke() {
        peer$create_connection$fn__21590 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.reconnect_fn, (Object)const__2), (Object)const__3, (Object)new peer$create_connection$fn__21590$fn__21591(), (Object)const__4, const__5, (Object)const__6, const__7.getRawRoot(), (Object)const__8, (Object)Numbers.num((long)Long.MAX_VALUE));
    }
}

