/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.log;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;

public final class spi$normalize_scan_opts
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"direction");
    public static final Keyword const__7 = RT.keyword(null, (String)"t");
    public static final Keyword const__8 = RT.keyword(null, (String)"ch");
    public static final Keyword const__9 = RT.keyword(null, (String)"limit");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"limit");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__16 = RT.keyword(null, (String)"backward");
    public static final Object const__17 = 0L;
    public static final Keyword const__18 = RT.keyword(null, (String)"forward");
    public static final Var const__19 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__20 = 1000L;

    public static Object invokeStatic(Object p__20980) {
        Object object;
        Object object2;
        Object object3;
        Object limit2;
        Object map__20981;
        Object object4;
        Object object5 = p__20980;
        p__20980 = null;
        Object map__209812 = object5;
        Object object6 = ((IFn)const__0.getRawRoot()).invoke(map__209812);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = ((IFn)const__1.getRawRoot()).invoke(map__209812);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object8 = map__209812;
                map__209812 = null;
                object4 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object8)));
            } else {
                Object object9 = ((IFn)const__3.getRawRoot()).invoke(map__209812);
                if (object9 != null && object9 != Boolean.FALSE) {
                    Object object10 = map__209812;
                    map__209812 = null;
                    object4 = ((IFn)const__4.getRawRoot()).invoke(object10);
                } else {
                    object4 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object4 = map__209812;
            map__209812 = null;
        }
        Object opts = map__20981 = object4;
        Object direction = RT.get((Object)map__20981, (Object)const__6);
        Object t = RT.get((Object)map__20981, (Object)const__7);
        Object ch = RT.get((Object)map__20981, (Object)const__8);
        Object object11 = map__20981;
        map__20981 = null;
        Object object12 = limit2 = RT.get((Object)object11, (Object)const__9);
        limit2 = null;
        if (object12 == null || object12 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__12))));
        }
        Object object13 = opts;
        opts = null;
        Object G__20982 = object13;
        Object object14 = t;
        t = null;
        if (Util.identical((Object)object14, null)) {
            Object object15 = G__20982;
            G__20982 = null;
            object3 = ((IFn)const__14.getRawRoot()).invoke(object15, (Object)const__7, Util.equiv((Object)direction, (Object)const__16) ? Numbers.num((long)Long.MAX_VALUE) : const__17);
        } else {
            object3 = G__20982;
            G__20982 = null;
        }
        Object G__209822 = object3;
        Object object16 = direction;
        direction = null;
        if (Util.identical((Object)object16, null)) {
            Object object17 = G__209822;
            G__209822 = null;
            object2 = ((IFn)const__14.getRawRoot()).invoke(object17, (Object)const__6, (Object)const__18);
        } else {
            object2 = G__209822;
            G__209822 = null;
        }
        Object G__209823 = object2;
        Object object18 = ch;
        ch = null;
        if (Util.identical((Object)object18, null)) {
            Object object19 = G__209823;
            G__209823 = null;
            object = ((IFn)const__14.getRawRoot()).invoke(object19, (Object)const__8, ((IFn)const__19.getRawRoot()).invoke(const__20));
        } else {
            object = G__209823;
            G__209823 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return spi$normalize_scan_opts.invokeStatic(object2);
    }
}

