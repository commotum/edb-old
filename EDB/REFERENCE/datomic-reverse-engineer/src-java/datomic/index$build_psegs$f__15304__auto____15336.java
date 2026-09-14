/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class index$build_psegs$f__15304__auto____15336
extends AFunction {
    Object cstore;
    Object write_handlers;
    long bound;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"build-one-seg");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"bounded-count");
    public static final Object const__5 = 0L;
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__14 = RT.var((String)"datomic.index", (String)"write-vals");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__16 = RT.keyword(null, (String)"key");
    public static final Keyword const__17 = RT.keyword(null, (String)"segid");
    public static final Keyword const__18 = RT.keyword(null, (String)"offset");
    public static final Keyword const__19 = RT.keyword(null, (String)"count");
    public static final Keyword const__20 = RT.keyword(null, (String)"last-d");

    public index$build_psegs$f__15304__auto____15336(Object object, Object object2, long l) {
        this.cstore = object;
        this.write_handlers = object2;
        this.bound = l;
    }

    public Object invoke(Object vmap, Object data2, Object es, Object c) {
        while (true) {
            Object object;
            Object object2 = data2;
            if (object2 == null || object2 == Boolean.FALSE) break;
            Object d = ((IFn)const__0.getRawRoot()).invoke(data2);
            Object segid = ((IFn)const__1.getRawRoot()).invoke();
            Object object3 = data2;
            Object object4 = data2;
            data2 = null;
            Object vec__15337 = ((IFn)const__2.getRawRoot()).invoke(object3, ((IFn)const__3.getRawRoot()).invoke((Object)Numbers.num((long)this.bound), object4), this.write_handlers);
            Object buf = RT.nth((Object)vec__15337, (int)RT.uncheckedIntCast((long)0L), null);
            Object data3 = RT.nth((Object)vec__15337, (int)RT.uncheckedIntCast((long)1L), null);
            Object written = RT.nth((Object)vec__15337, (int)RT.uncheckedIntCast((long)2L), null);
            Object last_d = RT.nth((Object)vec__15337, (int)RT.uncheckedIntCast((long)3L), null);
            Object object5 = vec__15337;
            vec__15337 = null;
            RT.nth((Object)object5, (int)RT.uncheckedIntCast((long)4L), null);
            Object object6 = vmap;
            vmap = null;
            Object object7 = buf;
            buf = null;
            Object vmap2 = ((IFn)const__10.getRawRoot()).invoke(object6, segid, object7);
            if ((long)RT.count((Object)vmap2) > 100L) {
                Object object8 = vmap2;
                vmap2 = null;
                ((IFn)const__14.getRawRoot()).invoke(this.cstore, object8);
                object = PersistentArrayMap.EMPTY;
            } else {
                object = vmap2;
                vmap2 = null;
            }
            Object object9 = data3;
            data3 = null;
            Object object10 = es;
            es = null;
            Object[] objectArray = new Object[10];
            objectArray[0] = const__16;
            Object object11 = d;
            d = null;
            objectArray[1] = object11;
            objectArray[2] = const__17;
            Object object12 = segid;
            segid = null;
            objectArray[3] = object12;
            objectArray[4] = const__18;
            objectArray[5] = const__5;
            objectArray[6] = const__19;
            Object object13 = written;
            written = null;
            objectArray[7] = object13;
            objectArray[8] = const__20;
            Object object14 = last_d;
            last_d = null;
            objectArray[9] = object14;
            Object object15 = c;
            c = null;
            c = Numbers.unchecked_inc((Object)object15);
            es = ((IFn)const__15.getRawRoot()).invoke(object10, (Object)RT.mapUniqueKeys((Object[])objectArray));
            data2 = object9;
            vmap = object;
        }
        Object object = vmap;
        vmap = null;
        Object object16 = es;
        es = null;
        Object object17 = c;
        c = null;
        return Tuple.create((Object)object, (Object)object16, (Object)object17);
    }
}

