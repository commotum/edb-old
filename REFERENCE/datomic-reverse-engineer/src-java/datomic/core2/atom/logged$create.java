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
 *  clojure.lang.Var
 */
package datomic.core2.atom;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.core2.atom.logged$create$fn__20351;

public final class logged$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"log");
    public static final Keyword const__7 = RT.keyword(null, (String)"header");
    public static final Keyword const__8 = RT.keyword(null, (String)"value");
    public static final Keyword const__9 = RT.keyword(null, (String)"serialize");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"header");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"value");
    public static final Var const__14 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__15 = 1L;
    public static final Var const__16 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object p__20320) {
        Object map__20321;
        Object object;
        Object map__203212 = p__20320;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__203212);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__1.getRawRoot()).invoke(map__203212);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__203212;
                map__203212 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object4)));
            } else {
                Object object5 = ((IFn)const__3.getRawRoot()).invoke(map__203212);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__203212;
                    map__203212 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object6);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__203212;
            map__203212 = null;
        }
        Object args = map__20321 = object;
        Object log2 = RT.get((Object)map__20321, (Object)const__6);
        Object header = RT.get((Object)map__20321, (Object)const__7);
        Object value = RT.get((Object)map__20321, (Object)const__8);
        Object serialize = RT.get((Object)map__20321, (Object)const__9);
        Object object7 = header;
        if (object7 == null || object7 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__12))));
        }
        Object object8 = value;
        if (object8 == null || object8 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__10.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__11.getRawRoot()).invoke((Object)const__13))));
        }
        Object c__10230__auto__20384 = ((IFn)const__14.getRawRoot()).invoke(const__15);
        Object captured_bindings__10231__auto__20385 = Var.getThreadBindingFrame();
        Object object9 = map__20321;
        map__20321 = null;
        Object object10 = captured_bindings__10231__auto__20385;
        captured_bindings__10231__auto__20385 = null;
        Object object11 = log2;
        log2 = null;
        Object object12 = args;
        args = null;
        Object object13 = header;
        header = null;
        Object object14 = value;
        value = null;
        Object object15 = serialize;
        serialize = null;
        Object object16 = p__20320;
        p__20320 = null;
        ((IFn)const__16.getRawRoot()).invoke((Object)new logged$create$fn__20351(object9, object10, object11, c__10230__auto__20384, object12, object13, object14, object15, object16));
        Object object17 = c__10230__auto__20384;
        c__10230__auto__20384 = null;
        return object17;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return logged$create.invokeStatic(object2);
    }
}

