/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class query$qseq$f__19556
extends AFunction {
    Object query_map;
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"query*");

    public query$qseq$f__19556(Object object) {
        this.query_map = object;
    }

    public Object invoke() {
        query$qseq$f__19556 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.query_map);
    }
}

