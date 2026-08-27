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
package datomic.valcache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.ByteBuffer;

public final class puts_pool_impl$fn__9878
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"v");
    public static final Var const__4 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__5 = RT.keyword(null, (String)"ValcacheGetInFlight");
    public static final Object const__6 = 1L;

    public static Object invokeStatic(Object p__9877) {
        Object map__9879;
        Object object;
        Object object2 = p__9877;
        p__9877 = null;
        Object map__98792 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__98792);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__98792;
            map__98792 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__98792;
            map__98792 = null;
        }
        Object object5 = map__9879 = object;
        map__9879 = null;
        Object v = RT.get((Object)object5, (Object)const__3);
        ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6);
        Object object6 = v;
        v = null;
        return ((ByteBuffer)object6).duplicate();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return puts_pool_impl$fn__9878.invokeStatic(object2);
    }
}

