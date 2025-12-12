/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class simple_kv$unpack
extends AFunction {
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__7 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Keyword const__8 = RT.keyword(null, (String)"v");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__10 = RT.keyword(null, (String)"id");

    public static Object invokeStatic(Object k, Object v) {
        Object object;
        boolean and__5236__auto__16729 = Numbers.gt((long)((Buffer)v).remaining(), (long)8L);
        if (and__5236__auto__16729 ? Util.equiv((long)568780356367818079L, (long)((ByteBuffer)v).getLong(RT.intCast((long)0L))) : and__5236__auto__16729) {
            Numbers.num((long)((ByteBuffer)v).getLong());
            int mlen = ((ByteBuffer)v).getInt();
            byte[] bytes = Numbers.byte_array((Object)mlen);
            ((ByteBuffer)v).get(bytes);
            byte[] byArray = bytes;
            bytes = null;
            Object object2 = v;
            v = null;
            object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)new String(byArray, "UTF-8")), (Object)const__8, (Object)((ByteBuffer)object2).slice());
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__8;
            Object object3 = v;
            v = null;
            objectArray[1] = object3;
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        IPersistentMap vmap = object;
        Object[] objectArray = new Object[2];
        objectArray[0] = const__10;
        Object object4 = k;
        k = null;
        objectArray[1] = object4;
        IPersistentMap iPersistentMap = vmap;
        vmap = null;
        return ((IFn)const__9.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), (Object)iPersistentMap);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return simple_kv$unpack.invokeStatic(object3, object4);
    }
}

