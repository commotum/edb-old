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

public final class log$convert_log_version
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"log-conversion");

    public static Object invokeStatic(Object cs, Object to_version) {
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)"This version of Datomic cannot convert log versions");
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$convert_log_version.invokeStatic(object3, object4);
    }
}

