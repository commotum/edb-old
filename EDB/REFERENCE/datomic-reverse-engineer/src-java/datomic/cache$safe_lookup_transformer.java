/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.cache$safe_lookup_transformer$reify__9404;
import datomic.cache$safe_lookup_transformer$try_val_fn__9402;

public final class cache$safe_lookup_transformer
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"key-fn");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"identity");
    public static final Keyword const__5 = RT.keyword(null, (String)"val-fn");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 114, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object m, ISeq p__9400) {
        cache$safe_lookup_transformer$try_val_fn__9402 try_val_fn;
        Object val_fn;
        ISeq iSeq;
        ISeq iSeq2 = p__9400;
        p__9400 = null;
        ISeq map__9401 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__9401);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__9401;
            map__9401 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__9401;
            map__9401 = null;
        }
        ISeq map__94012 = iSeq;
        Object key_fn = RT.get((Object)map__94012, (Object)const__3, (Object)const__4.getRawRoot());
        ISeq iSeq4 = map__94012;
        map__94012 = null;
        Object object2 = val_fn = RT.get((Object)iSeq4, (Object)const__5, (Object)const__4.getRawRoot());
        val_fn = null;
        cache$safe_lookup_transformer$try_val_fn__9402 cache$safe_lookup_transformer$try_val_fn__9402 = try_val_fn = new cache$safe_lookup_transformer$try_val_fn__9402(object2, key_fn);
        try_val_fn = null;
        Object object3 = key_fn;
        key_fn = null;
        Object object4 = m;
        m = null;
        return ((IObj)new cache$safe_lookup_transformer$reify__9404(null, (Object)cache$safe_lookup_transformer$try_val_fn__9402, object3, object4)).withMeta((IPersistentMap)const__10);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return cache$safe_lookup_transformer.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

