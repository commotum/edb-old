/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb_values$chunk
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"subs");
    public static final Object const__4 = 0L;

    public static Object invokeStatic(Object s, Object chunk_size) {
        Object chunks = PersistentVector.EMPTY;
        Object object = s;
        s = null;
        Object s2 = object;
        while (true) {
            if (Numbers.lte((long)RT.count((Object)s2), (Object)chunk_size)) break;
            PersistentVector persistentVector = chunks;
            chunks = null;
            Object object2 = ((IFn)const__2.getRawRoot()).invoke((Object)persistentVector, ((IFn)const__3.getRawRoot()).invoke(s2, const__4, chunk_size));
            Object object3 = s2;
            s2 = null;
            s2 = ((IFn)const__3.getRawRoot()).invoke(object3, chunk_size);
            chunks = object2;
        }
        PersistentVector persistentVector = chunks;
        chunks = null;
        Object object4 = s2;
        s2 = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)persistentVector, object4);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb_values$chunk.invokeStatic(object3, object4);
    }
}

