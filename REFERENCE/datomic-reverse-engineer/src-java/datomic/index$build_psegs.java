/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$build_psegs$f__15304__auto____15336;

public final class index$build_psegs
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Object const__4 = 0L;
    public static final Var const__7 = RT.var((String)"datomic.index", (String)"write-vals");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"+");

    public static Object invokeStatic(Object cstore, Object olookup, Object data2, Object es, Object write_handlers2, Object segs_written_ref) {
        index$build_psegs$f__15304__auto____15336 f__15304__auto__15342;
        long bound = 2L * 16000L;
        Object object = write_handlers2;
        write_handlers2 = null;
        index$build_psegs$f__15304__auto____15336 index$build_psegs$f__15304__auto____15336 = f__15304__auto__15342 = new index$build_psegs$f__15304__auto____15336(cstore, object, bound);
        f__15304__auto__15342 = null;
        Object object2 = data2;
        data2 = null;
        Object object3 = es;
        es = null;
        Object vec__15333 = ((IFn)index$build_psegs$f__15304__auto____15336).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__3.getRawRoot()).invoke(object2), object3, const__4);
        Object vmap = RT.nth((Object)vec__15333, (int)RT.uncheckedIntCast((long)0L), null);
        Object es2 = RT.nth((Object)vec__15333, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = vec__15333;
        vec__15333 = null;
        Object c = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)2L), null);
        Object object5 = cstore;
        cstore = null;
        Object object6 = vmap;
        vmap = null;
        ((IFn)const__7.getRawRoot()).invoke(object5, object6);
        Object object7 = segs_written_ref;
        segs_written_ref = null;
        Object object8 = c;
        c = null;
        ((IFn)const__8.getRawRoot()).invoke(object7, const__9.getRawRoot(), object8);
        Object object9 = es2;
        es2 = null;
        return object9;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return index$build_psegs.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

