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

public final class db$index_sort__GT_cmp
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"aevt");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"aevt-cmp");
    public static final Keyword const__2 = RT.keyword(null, (String)"avet");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"avet-cmp");
    public static final Keyword const__4 = RT.keyword(null, (String)"eavt");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"eavt-cmp");
    public static final Keyword const__6 = RT.keyword(null, (String)"vaet");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"raet-cmp");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object index_sort) {
        Object object = index_sort;
        index_sort = null;
        Object G__12843 = object;
        switch (Util.hash((Object)G__12843) >> 5 & 3) {
            case 0: {
                if (G__12843 != const__0) break;
                Object object2 = const__1.getRawRoot();
                return object2;
            }
            case 1: {
                if (G__12843 != const__2) break;
                Object object2 = const__3.getRawRoot();
                return object2;
            }
            case 2: {
                if (G__12843 != const__4) break;
                Object object2 = const__5.getRawRoot();
                return object2;
            }
            case 3: {
                if (G__12843 != const__6) break;
                Object object2 = const__7.getRawRoot();
                return object2;
            }
        }
        Object object3 = G__12843;
        G__12843 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__8.getRawRoot()).invoke((Object)"No matching clause: ", object3));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$index_sort__GT_cmp.invokeStatic(object2);
    }
}

