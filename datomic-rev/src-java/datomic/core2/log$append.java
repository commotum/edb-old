/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.log.spi.Append;

public final class log$append
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final AFn const__11;
    public static final AFn const__12;
    public static final Var const__13;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object log2, Object p__20560, Object body) {
        Object object;
        Object map__20561;
        Object object2;
        block6: {
            Object map__205612;
            block7: {
                Object object3 = p__20560;
                p__20560 = null;
                map__205612 = object3;
                Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__205612);
                if (object4 == null || object4 == Boolean.FALSE) break block7;
                Object object5 = ((IFn)const__2.getRawRoot()).invoke(map__205612);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__205612;
                    map__205612 = null;
                    object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__3.getRawRoot()).invoke(object6)));
                    break block6;
                } else {
                    Object object7 = ((IFn)const__4.getRawRoot()).invoke(map__205612);
                    if (object7 != null && object7 != Boolean.FALSE) {
                        Object object8 = map__205612;
                        map__205612 = null;
                        object2 = ((IFn)const__5.getRawRoot()).invoke(object8);
                        break block6;
                    } else {
                        object2 = PersistentArrayMap.EMPTY;
                    }
                }
                break block6;
            }
            object2 = map__205612;
            map__205612 = null;
        }
        Object header = map__20561 = object2;
        Object t = RT.get((Object)map__20561, (Object)const__7);
        Object object9 = map__20561;
        map__20561 = null;
        Object next_t2 = RT.get((Object)object9, (Object)const__8);
        Object object10 = t;
        t = null;
        if (object10 == null) throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__11))));
        if (object10 == Boolean.FALSE) throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__11))));
        Object object11 = next_t2;
        next_t2 = null;
        if (object11 == null) throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__12))));
        if (object11 == Boolean.FALSE) throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke((Object)const__12))));
        Object object12 = log2;
        log2 = null;
        Object object13 = object12;
        if (Util.classOf((Object)object12) != __cached_class__0) {
            if (object13 instanceof Append) {
                Object object14 = header;
                header = null;
                Object object15 = body;
                body = null;
                object = ((Append)object13)._append(object14, object15);
                return object;
            }
            object13 = object13;
            __cached_class__0 = Util.classOf((Object)object13);
        }
        Object object16 = header;
        header = null;
        Object object17 = body;
        body = null;
        object = const__13.getRawRoot().invoke(object13, object16, object17);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$append.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object log2, Object header) {
        Object object = log2;
        log2 = null;
        Object object2 = header;
        header = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$append.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.core2.log", (String)"append");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"next");
        const__3 = RT.var((String)"clojure.core", (String)"to-array");
        const__4 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.var((String)"clojure.core", (String)"first");
        const__7 = RT.keyword(null, (String)"t");
        const__8 = RT.keyword(null, (String)"next-t");
        const__9 = RT.var((String)"clojure.core", (String)"str");
        const__10 = RT.var((String)"clojure.core", (String)"pr-str");
        const__11 = (AFn)Symbol.intern(null, (String)"t");
        const__12 = (AFn)Symbol.intern(null, (String)"next-t");
        const__13 = RT.var((String)"datomic.core2.log.spi", (String)"-append");
    }
}

