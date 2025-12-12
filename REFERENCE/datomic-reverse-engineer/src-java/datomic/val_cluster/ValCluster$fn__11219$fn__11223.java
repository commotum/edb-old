/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.val_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ValCluster$fn__11219$fn__11223
extends AFunction {
    Object val_key;
    Object val_store;
    Object buf;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.core2.async", (String)"<!!x");
    public static final Var const__2 = RT.var((String)"datomic.core2.val-store", (String)"put");
    public static final Keyword const__3 = RT.keyword(null, (String)"val");
    public static final Var const__4 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"throw-anom");
    public static final Keyword const__7 = RT.keyword(null, (String)"else");
    public static final Keyword const__8 = RT.keyword(null, (String)"created");
    public static final Keyword const__9 = RT.keyword(null, (String)"threw");

    public ValCluster$fn__11219$fn__11223(Object object, Object object2, Object object3) {
        this.val_key = object;
        this.val_store = object2;
        this.buf = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.val_key = null;
            Object res = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.val_store, this.val_key, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__3, this.buf = null})));
            Object object2 = ((IFn)const__4.getRawRoot()).invoke(res);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = res;
                res = null;
                object = ((IFn)const__5.getRawRoot()).invoke(object3);
            } else {
                Object object4 = res;
                res = null;
                if (Util.identical((Object)object4, null)) {
                    object = null;
                } else {
                    Keyword keyword = const__7;
                    object = keyword != null && keyword != Boolean.FALSE ? const__8 : null;
                }
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__9;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

