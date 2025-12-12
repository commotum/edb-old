/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.val_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ValCluster$fn__11230$fn__11234
extends AFunction {
    Object val_store;
    Object key;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.core2.async", (String)"<!!x");
    public static final Var const__2 = RT.var((String)"datomic.core2.val-store", (String)"delete");
    public static final Var const__3 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"throw-anom");
    public static final Keyword const__5 = RT.keyword(null, (String)"ok");
    public static final Keyword const__6 = RT.keyword(null, (String)"threw");

    public ValCluster$fn__11230$fn__11234(Object object, Object object2) {
        this.val_store = object;
        this.key = object2;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.key = null;
            Object res = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.val_store, this.key));
            Object object2 = ((IFn)const__3.getRawRoot()).invoke(res);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = res;
                res = null;
                object = ((IFn)const__4.getRawRoot()).invoke(object3);
            } else {
                object = const__5;
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__6;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

