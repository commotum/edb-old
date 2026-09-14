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
import datomic.fressian$read_seq$fn__12187;

public final class fressian$read_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"read-seq");
    public static final Var const__1 = RT.var((String)"datomic.queue", (String)"queue-seq");
    public static final Object const__2 = 100L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"fill");
    public static final Keyword const__7 = RT.keyword(null, (String)"done");
    public static final Keyword const__8 = RT.keyword(null, (String)"drain");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"future-call");

    public static Object invokeStatic(Object readable, Object handler_lookup) {
        Object object;
        Object map__12186 = ((IFn)const__1.getRawRoot()).invoke(const__2);
        Object object2 = ((IFn)const__3.getRawRoot()).invoke(map__12186);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__12186;
            map__12186 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object3)));
        } else {
            object = map__12186;
            map__12186 = null;
        }
        Object map__121862 = object;
        Object fill = RT.get((Object)map__121862, (Object)const__6);
        Object done = RT.get((Object)map__121862, (Object)const__7);
        Object object4 = map__121862;
        map__121862 = null;
        Object drain = RT.get((Object)object4, (Object)const__8);
        Object object5 = readable;
        readable = null;
        Object object6 = fill;
        fill = null;
        Object object7 = handler_lookup;
        handler_lookup = null;
        Object object8 = done;
        done = null;
        ((IFn)const__9.getRawRoot()).invoke((Object)new fressian$read_seq$fn__12187(object5, object6, object7, object8));
        Object object9 = drain;
        drain = null;
        return ((IFn)object9).invoke();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fressian$read_seq.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object readable) {
        Object object = readable;
        readable = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$read_seq.invokeStatic(object2);
    }
}

