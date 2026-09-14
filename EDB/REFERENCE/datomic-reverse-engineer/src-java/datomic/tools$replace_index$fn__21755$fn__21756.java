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

public final class tools$replace_index$fn__21755$fn__21756
extends AFunction {
    Object index_id;
    Object cluster;
    public static final Keyword const__4 = RT.keyword(null, (String)"ok");
    public static final Var const__5 = RT.var((String)"datomic.tools", (String)"reset-index-ref");

    public tools$replace_index$fn__21755$fn__21756(Object object, Object object2) {
        this.index_id = object;
        this.cluster = object2;
    }

    public Object invoke() {
        long n = 0L;
        while (true) {
            if (10L < n) {
                throw (Throwable)new RuntimeException("Retry limit exceeded replacing index.");
            }
            if (Util.equiv((Object)const__4, (Object)((IFn)const__5.getRawRoot()).invoke(this.cluster, this.index_id))) break;
            n = Numbers.inc((long)n);
        }
        return Boolean.TRUE;
    }
}

