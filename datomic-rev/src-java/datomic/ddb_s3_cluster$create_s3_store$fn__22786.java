/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb_s3_cluster$create_s3_store$fn__22786
extends AFunction {
    Object max_retries;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"i");
    public static final Keyword const__4 = RT.keyword(null, (String)"result");
    public static final Var const__6 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__8 = RT.var((String)"datomic.core2.anomalies", (String)"not-found?");

    public ddb_s3_cluster$create_s3_store$fn__22786(Object object) {
        this.max_retries = object;
    }

    public Object invoke(Object p__22785) {
        Object object;
        Object object2;
        Object object3 = p__22785;
        p__22785 = null;
        Object map__22787 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__22787);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__22787;
            map__22787 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__22787;
            map__22787 = null;
        }
        Object map__227872 = object2;
        Object i = RT.get((Object)map__227872, (Object)const__3);
        Object object6 = map__227872;
        map__227872 = null;
        Object result2 = RT.get((Object)object6, (Object)const__4);
        Object object7 = i;
        i = null;
        boolean and__5236__auto__22790 = Numbers.lte((Object)object7, (Object)this_.max_retries);
        if (and__5236__auto__22790) {
            Object and__5236__auto__22789;
            Object object8 = and__5236__auto__22789 = ((IFn)const__6.getRawRoot()).invoke(result2);
            if (object8 != null && object8 != Boolean.FALSE) {
                Object object9 = result2;
                result2 = null;
                ddb_s3_cluster$create_s3_store$fn__22786 this_ = null;
                object = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(object9));
            } else {
                object = and__5236__auto__22789;
                and__5236__auto__22789 = null;
            }
        } else {
            object = and__5236__auto__22790 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }
}

