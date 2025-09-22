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
import datomic.integrity$mk_index_pred$fn__22136;
import datomic.integrity$mk_index_pred$fn__22138;

public final class integrity$mk_index_pred
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"aevt");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"identity");
    public static final Keyword const__2 = RT.keyword(null, (String)"avet");
    public static final Var const__3 = RT.var((String)"datomic.integrity", (String)"mk-attr-pred");
    public static final Var const__4 = RT.var((String)"datomic.integrity", (String)"attr-id-set");
    public static final Keyword const__5 = RT.keyword(null, (String)"has-avet");
    public static final Keyword const__6 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__7 = RT.keyword(null, (String)"raet");
    public static final Keyword const__8 = RT.keyword(null, (String)"vaet");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object index2) {
        Object object = index2;
        index2 = null;
        Object G__22135 = object;
        switch (Util.hash((Object)G__22135) >> 4 & 7) {
            case 0: {
                if (G__22135 != const__0) break;
                Object object2 = const__1.getRawRoot();
                return object2;
            }
            case 3: {
                if (G__22135 != const__2) break;
                Object object3 = db2;
                db2 = null;
                Object object2 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object3, (Object)const__5));
                return object2;
            }
            case 4: {
                if (G__22135 != const__6) break;
                Object object2 = const__1.getRawRoot();
                return object2;
            }
            case 5: {
                if (G__22135 != const__7) break;
                Object object4 = db2;
                db2 = null;
                Object object2 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object4, (Object)new integrity$mk_index_pred$fn__22136()));
                return object2;
            }
            case 7: {
                if (G__22135 != const__8) break;
                Object object5 = db2;
                db2 = null;
                Object object2 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object5, (Object)new integrity$mk_index_pred$fn__22138()));
                return object2;
            }
        }
        Object object6 = G__22135;
        G__22135 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__9.getRawRoot()).invoke((Object)"No matching clause: ", object6));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$mk_index_pred.invokeStatic(object3, object4);
    }
}

