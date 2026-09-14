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
 *  clojure.lang.Util
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
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.valcache$sasl_loop$fn__9783;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public final class valcache$sasl_loop
extends AFunction {
    public static final Object const__1 = 24L;
    public static final Var const__2 = RT.var((String)"datomic.valcache", (String)"read-header");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"opcode");

    public static Object invokeStatic(Object creds, Object sc, Object sem) {
        Boolean bl;
        ByteBuffer hb = ByteBuffer.wrap(Numbers.byte_array((Object)const__1));
        while (true) {
            Object authed;
            Object map__9782;
            Object object;
            Object map__97822 = ((IFn)const__2.getRawRoot()).invoke(sc, (Object)hb);
            Object object2 = ((IFn)const__3.getRawRoot()).invoke(map__97822);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = map__97822;
                map__97822 = null;
                object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object3)));
            } else {
                object = map__97822;
                map__97822 = null;
            }
            Object header = map__9782 = object;
            Object object4 = map__9782;
            map__9782 = null;
            Object opcode = RT.get((Object)object4, (Object)const__6);
            ((Semaphore)sem).acquire();
            Object object5 = header;
            header = null;
            Object object6 = authed = ((IFn)new valcache$sasl_loop$fn__9783(object5, creds, sem, sc)).invoke();
            authed = null;
            if (object6 != null && object6 != Boolean.FALSE) {
                bl = Boolean.TRUE;
                break;
            }
            Object object7 = opcode;
            opcode = null;
            if (Util.equiv((Object)object7, (long)7L)) {
                bl = Boolean.FALSE;
                break;
            }
            ByteBuffer byteBuffer = hb;
            hb = null;
            hb = byteBuffer;
        }
        return bl;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return valcache$sasl_loop.invokeStatic(object4, object5, object6);
    }
}

