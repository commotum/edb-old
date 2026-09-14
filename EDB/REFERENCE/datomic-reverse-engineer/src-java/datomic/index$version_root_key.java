/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOO
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.Arrays;

public final class index$version_root_key
extends AFunction
implements IFn.LOO {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__1 = RT.keyword(null, (String)"eavt-main");
    public static final Keyword const__2 = RT.keyword(null, (String)"aevt-main");
    public static final Keyword const__3 = RT.keyword(null, (String)"raet-main");
    public static final Keyword const__4 = RT.keyword(null, (String)"avet-main");
    public static final AFn const__5 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"eavt-main"), RT.keyword(null, (String)"aevt-main"), RT.keyword(null, (String)"raet-main"), RT.keyword(null, (String)"avet-main")});
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__8 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"contains?"), PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"eavt-main"), RT.keyword(null, (String)"aevt-main"), RT.keyword(null, (String)"raet-main"), RT.keyword(null, (String)"avet-main")}), Symbol.intern(null, (String)"k")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Keyword const__10 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__11 = RT.keyword(null, (String)"avet");
    public static final Keyword const__12 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__13 = RT.keyword(null, (String)"raet");

    public static Object invokeStatic(long version2, Object object) {
        Object object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke((Object)const__5, object);
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__6.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__7.getRawRoot()).invoke(const__8))));
        }
        long G__15287 = version2;
        block0 : switch ((int)G__15287) {
            case 1: {
                if (1L == G__15287) {
                    Object object4 = object;
                    object = null;
                    Object G__15288 = object4;
                    switch (Util.hash((Object)G__15288) >> 17 & 3) {
                        case 0: {
                            if (G__15288 != const__2) break;
                            object2 = const__10;
                            break block0;
                        }
                        case 1: {
                            if (G__15288 != const__4) break;
                            object2 = const__11;
                            break block0;
                        }
                        case 2: {
                            if (G__15288 != const__1) break;
                            object2 = const__12;
                            break block0;
                        }
                        case 3: {
                            if (G__15288 != const__3) break;
                            object2 = const__13;
                            break block0;
                        }
                    }
                    Object object5 = G__15288;
                    G__15288 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__6.getRawRoot()).invoke((Object)"No matching clause: ", object5));
                }
            }
            default: {
                object2 = object;
                object = null;
            }
        }
        return object2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object2;
        object2 = null;
        return index$version_root_key.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)), object3);
    }

    public final Object invokePrim(long l, Object object) {
        Object object2 = object;
        object = null;
        return index$version_root_key.invokeStatic(l, object2);
    }
}

