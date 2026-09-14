/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$system_tx_hook
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"schema");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"update-schema-level");

    public static Object invokeStatic(Object _, Object after, Object d, Object check_QMARK_) {
        Object object;
        Object object2 = d;
        d = null;
        Object G__13235 = ((IDatum)object2).getV();
        switch (Util.hash((Object)G__13235)) {
            case 1599313017: {
                if (G__13235 == const__0) {
                    Object object3 = after;
                    after = null;
                    object = ((IFn)const__1.getRawRoot()).invoke(object3);
                    break;
                }
            }
            default: {
                object = after;
                Object var1_1 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$system_tx_hook.invokeStatic(object5, object6, object7, object8);
    }
}

