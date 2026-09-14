/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import javax.crypto.spec.SecretKeySpec;

public final class crypto$deser_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"key");
    public static final Keyword const__4 = RT.keyword(null, (String)"alg");
    public static final Var const__5 = RT.var((String)"datomic.codec", (String)"decode-64");
    public static final Var const__6 = RT.var((String)"datomic.codec", (String)"string->bytes");

    public static Object invokeStatic(Object p__23360) {
        Object object;
        Object object2 = p__23360;
        p__23360 = null;
        Object map__23361 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__23361);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__23361;
            map__23361 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__23361;
            map__23361 = null;
        }
        Object map__233612 = object;
        Object key = RT.get((Object)map__233612, (Object)const__3);
        Object object5 = map__233612;
        map__233612 = null;
        Object alg = RT.get((Object)object5, (Object)const__4);
        Object object6 = key;
        key = null;
        Object data2 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object6));
        byte[] byArray = (byte[])data2;
        Object object7 = data2;
        data2 = null;
        Object object8 = alg;
        alg = null;
        return new SecretKeySpec(byArray, RT.intCast((long)0L), RT.count((Object)object7), (String)object8);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return crypto$deser_key.invokeStatic(object2);
    }
}

