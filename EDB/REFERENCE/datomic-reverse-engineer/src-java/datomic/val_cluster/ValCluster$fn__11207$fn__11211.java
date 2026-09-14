/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.val_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ValCluster$fn__11207$fn__11211
extends AFunction {
    Object val_key;
    Object val_store;
    Object opts;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.core2.async", (String)"<!!x");
    public static final Var const__2 = RT.var((String)"datomic.core2.val-store", (String)"get");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"val");
    public static final Var const__7 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__8 = RT.var((String)"datomic.common", (String)"throw-anom");
    public static final Keyword const__10 = RT.keyword(null, (String)"else");
    public static final Keyword const__11 = RT.keyword(null, (String)"buf");
    public static final Keyword const__12 = RT.keyword(null, (String)"threw");

    public ValCluster$fn__11207$fn__11211(Object object, Object object2, Object object3) {
        this.val_key = object;
        this.val_store = object2;
        this.opts = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object map__11212;
            Object object2;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.val_key = null;
            this.opts = null;
            Object map__112122 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.val_store, this.val_key, this.opts));
            Object object3 = ((IFn)const__3.getRawRoot()).invoke(map__112122);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__112122;
                map__112122 = null;
                object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object4)));
            } else {
                object2 = map__112122;
                map__112122 = null;
            }
            Object res = map__11212 = object2;
            Object object5 = map__11212;
            map__11212 = null;
            Object val = RT.get((Object)object5, (Object)const__6);
            Object object6 = ((IFn)const__7.getRawRoot()).invoke(res);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object7 = res;
                res = null;
                object = ((IFn)const__8.getRawRoot()).invoke(object7);
            } else if (Util.identical((Object)val, null)) {
                object = null;
            } else {
                Keyword keyword = const__10;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object[] objectArray2 = new Object[2];
                    objectArray2[0] = const__11;
                    Object object8 = val;
                    val = null;
                    objectArray2[1] = object8;
                    object = RT.mapUniqueKeys((Object[])objectArray2);
                } else {
                    object = null;
                }
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__12;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

