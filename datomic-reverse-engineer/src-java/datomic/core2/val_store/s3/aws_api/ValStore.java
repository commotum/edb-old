/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.val_store.s3.aws_api;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21429;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21531;
import datomic.core2.val_store.s3.aws_api.ValStore$fn__21601;
import datomic.core2.val_store.spi.Delete;
import datomic.core2.val_store.spi.Get;
import datomic.core2.val_store.spi.Put;
import java.nio.Buffer;

public final class ValStore
implements Get,
Delete,
Put,
IType {
    public final Object client;
    public final Object bucket;
    public final Object prefix;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"val");
    public static final Var const__7 = RT.var((String)"datomic.core2.val-store.s3", (String)"go-with-metrics");
    public static final Keyword const__8 = RT.keyword(null, (String)"create");
    public static final Keyword const__9 = RT.keyword(null, (String)"content-length");
    public static final Var const__10 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Keyword const__11 = RT.keyword(null, (String)"s3");
    public static final Keyword const__12 = RT.keyword(null, (String)"get");
    public static final Keyword const__13 = RT.keyword(null, (String)"delete");

    public ValStore(Object object, Object object2, Object object3) {
        this.client = object;
        this.bucket = object2;
        this.prefix = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"prefix"));
    }

    @Override
    public Object _delete(Object k, Object opts) {
        Object object = opts;
        opts = null;
        ValStore$fn__21601 valStore$fn__21601 = new ValStore$fn__21601(this_.bucket, this_, this_.prefix, object, this_.client, k);
        Object object2 = k;
        k = null;
        ValStore this_ = null;
        return ((IFn)const__7.getRawRoot()).invoke((Object)valStore$fn__21601, object2, (Object)const__13, (Object)PersistentArrayMap.EMPTY);
    }

    @Override
    public Object _get(Object k, Object opts) {
        ((IFn)const__10.getRawRoot()).invoke((Object)const__11);
        Object object = opts;
        opts = null;
        ValStore$fn__21531 valStore$fn__21531 = new ValStore$fn__21531(this_, this_.bucket, this_.prefix, object, this_.client, k);
        Object object2 = k;
        k = null;
        ValStore this_ = null;
        return ((IFn)const__7.getRawRoot()).invoke((Object)valStore$fn__21531, object2, (Object)const__12, (Object)PersistentArrayMap.EMPTY);
    }

    @Override
    public Object _put(Object k, Object p__21426, Object opts) {
        IPersistentMap iPersistentMap;
        Object map__21428;
        Object object;
        Object map__214282 = p__21426;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__214282);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__1.getRawRoot()).invoke(map__214282);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__214282;
                map__214282 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object4)));
            } else {
                Object object5 = ((IFn)const__3.getRawRoot()).invoke(map__214282);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__214282;
                    map__214282 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object6);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__214282;
            map__214282 = null;
        }
        Object v = map__21428 = object;
        Object val = RT.get((Object)map__21428, (Object)const__6);
        IFn iFn = (IFn)const__7.getRawRoot();
        Object object7 = p__21426;
        p__21426 = null;
        Object object8 = map__21428;
        map__21428 = null;
        Object object9 = v;
        v = null;
        Object object10 = opts;
        opts = null;
        ValStore$fn__21429 valStore$fn__21429 = new ValStore$fn__21429(k, this_.bucket, object7, object8, this_, this_.prefix, this_.client, object9, object10, val);
        Object object11 = k;
        k = null;
        Object object12 = val;
        if (object12 != null && object12 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__9;
            Object object13 = val;
            val = null;
            objectArray[1] = ((Buffer)object13).remaining();
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        ValStore this_ = null;
        return iFn.invoke((Object)valStore$fn__21429, object11, (Object)const__8, iPersistentMap);
    }
}

