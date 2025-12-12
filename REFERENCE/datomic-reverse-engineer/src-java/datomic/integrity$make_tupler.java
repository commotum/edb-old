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
import datomic.integrity$make_tupler$fn__22005;
import datomic.integrity$make_tupler$fn__22007;
import datomic.integrity$make_tupler$fn__22009;
import datomic.integrity$make_tupler$fn__22011;

public final class integrity$make_tupler
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__1 = RT.keyword(null, (String)"avet");
    public static final Keyword const__2 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__3 = RT.keyword(null, (String)"vaet");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object index2) {
        Object object = index2;
        index2 = null;
        Object G__22004 = object;
        switch (Util.hash((Object)G__22004) >> 5 & 3) {
            case 0: {
                if (G__22004 != const__0) break;
                AFunction aFunction = new integrity$make_tupler$fn__22005();
                return aFunction;
            }
            case 1: {
                if (G__22004 != const__1) break;
                AFunction aFunction = new integrity$make_tupler$fn__22007();
                return aFunction;
            }
            case 2: {
                if (G__22004 != const__2) break;
                AFunction aFunction = new integrity$make_tupler$fn__22009();
                return aFunction;
            }
            case 3: {
                if (G__22004 != const__3) break;
                AFunction aFunction = new integrity$make_tupler$fn__22011();
                return aFunction;
            }
        }
        Object object2 = G__22004;
        G__22004 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__4.getRawRoot()).invoke((Object)"No matching clause: ", object2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$make_tupler.invokeStatic(object2);
    }
}

