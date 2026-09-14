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

public final class pull$normalize_pattern$direction__18968
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"reverse-key?");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Keyword const__2 = RT.keyword(null, (String)"reverse");
    public static final Keyword const__3 = RT.keyword(null, (String)"forward");

    public Object invoke(Object kw) {
        Object object = kw;
        kw = null;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object));
        return object2 != null && object2 != Boolean.FALSE ? const__2 : const__3;
    }
}

