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
import datomic.cache$lookup_transformer$reify__9397;
import datomic.cache$lookup_transformer$try_val_fn__9395;

public final class cache$lookup_transformer
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"key-fn");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"identity");
    public static final Keyword const__5 = RT.keyword(null, (String)"val-fn");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 83, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object m, ISeq p__9393) {
        Object val_fn;
        ISeq iSeq;
        ISeq iSeq2 = p__9393;
        p__9393 = null;
        ISeq map__9394 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__9394);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__9394;
            map__9394 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__9394;
            map__9394 = null;
        }
        ISeq map__93942 = iSeq;
        Object key_fn = RT.get((Object)map__93942, (Object)const__3, (Object)const__4.getRawRoot());
        ISeq iSeq4 = map__93942;
        map__93942 = null;
        Object object2 = val_fn = RT.get((Object)iSeq4, (Object)const__5, (Object)const__4.getRawRoot());
        val_fn = null;
        cache$lookup_transformer$try_val_fn__9395 try_val_fn = new cache$lookup_transformer$try_val_fn__9395(object2);
        Object object3 = m;
        m = null;
        cache$lookup_transformer$try_val_fn__9395 cache$lookup_transformer$try_val_fn__9395 = try_val_fn;
        try_val_fn = null;
        Object object4 = key_fn;
        key_fn = null;
        return ((IObj)new cache$lookup_transformer$reify__9397(null, object3, (Object)cache$lookup_transformer$try_val_fn__9395, object4)).withMeta((IPersistentMap)const__10);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return cache$lookup_transformer.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

