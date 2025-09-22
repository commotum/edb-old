/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class index$init_index_STAR_$fn__15276
extends AFunction {
    public static final Object const__1 = 0L;
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__7 = RT.var((String)"datomic.index", (String)"fress");
    public static final Var const__8 = RT.var((String)"datomic.index", (String)"transpose");
    public static final Var const__9 = RT.var((String)"datomic.index", (String)"common-write-handlers");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object p__15275, Object av) {
        Object object = p__15275;
        p__15275 = null;
        Object vec__15277 = object;
        Object keys = RT.nth((Object)vec__15277, (int)RT.uncheckedIntCast((long)0L), null);
        Object ids = RT.nth((Object)vec__15277, (int)RT.uncheckedIntCast((long)1L), null);
        Object offs = RT.nth((Object)vec__15277, (int)RT.uncheckedIntCast((long)2L), null);
        Object cnts = RT.nth((Object)vec__15277, (int)RT.uncheckedIntCast((long)3L), null);
        Object object2 = vec__15277;
        vec__15277 = null;
        Object bufs = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)4L), null);
        Object id = ((IFn)const__6.getRawRoot()).invoke();
        Object buf = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(av), const__9.getRawRoot());
        Object object3 = keys;
        keys = null;
        Object object4 = ((IFn)const__10.getRawRoot()).invoke(object3, ((IFn)av).invoke(const__1));
        Object object5 = ids;
        ids = null;
        Object object6 = ((IFn)const__10.getRawRoot()).invoke(object5, id);
        Object object7 = offs;
        offs = null;
        Object object8 = cnts;
        cnts = null;
        Object object9 = av;
        av = null;
        Object object10 = bufs;
        bufs = null;
        Object object11 = id;
        id = null;
        Object object12 = buf;
        buf = null;
        return Tuple.create((Object)object4, (Object)object6, (Object)((IFn)const__10.getRawRoot()).invoke(object7, const__1), (Object)((IFn)const__10.getRawRoot()).invoke(object8, (Object)RT.count((Object)object9)), (Object)((IFn)const__12.getRawRoot()).invoke(object10, object11, object12));
    }
}

