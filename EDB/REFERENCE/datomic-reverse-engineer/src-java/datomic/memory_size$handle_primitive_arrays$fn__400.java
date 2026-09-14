/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class memory_size$handle_primitive_arrays$fn__400
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__6 = (AFn)Symbol.intern((String)"clojure.core", (String)"extend-type");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"datomic.memory-size", (String)"array-type");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"datomic.memory-size", (String)"MemorySize");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"memory-size");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"x");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"clojure.core", (String)"+");
    public static final Object const__14 = 16L;
    public static final AFn const__15 = (AFn)Symbol.intern((String)"clojure.core", (String)"*");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"clojure.core", (String)"count");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"x");

    public Object invoke(Object p__399) {
        Object object = p__399;
        p__399 = null;
        Object vec__401 = object;
        Object type = RT.nth((Object)vec__401, (int)RT.intCast((long)0L), null);
        Object object2 = vec__401;
        vec__401 = null;
        Object size = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = type;
        type = null;
        Object object4 = size;
        size = null;
        memory_size$handle_primitive_arrays$fn__400 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__7), ((IFn)const__5.getRawRoot()).invoke(object3)))), ((IFn)const__5.getRawRoot()).invoke((Object)const__8), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__9), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__12))))), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__13), ((IFn)const__5.getRawRoot()).invoke(const__14), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__15), ((IFn)const__5.getRawRoot()).invoke(object4), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__16), ((IFn)const__5.getRawRoot()).invoke((Object)const__17)))))))))))))));
    }
}

