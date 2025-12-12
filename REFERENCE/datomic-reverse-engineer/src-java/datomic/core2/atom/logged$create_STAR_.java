/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.atom.logged$create_STAR_$fn__20184;
import datomic.core2.atom.logged$create_STAR_$fn__20265;
import datomic.core2.atom.logged.LoggedAtom;

public final class logged$create_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"log");
    public static final Keyword const__7 = RT.keyword(null, (String)"serialize");
    public static final Keyword const__8 = RT.keyword(null, (String)"deserialize");
    public static final Keyword const__9 = RT.keyword(null, (String)"refresh-msec");
    public static final Keyword const__10 = RT.keyword(null, (String)"validator");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__12 = RT.var((String)"clojure.core.async", (String)"promise-chan");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"add-watch");
    public static final Keyword const__14 = RT.keyword((String)"datomic.core2.atom.logged", (String)"state");
    public static final Var const__15 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__16 = 1L;
    public static final Var const__17 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object p__20182, Object state2) {
        Object object;
        Object map__20183 = p__20182;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__20183);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__1.getRawRoot()).invoke(map__20183);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__20183;
                map__20183 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object4)));
            } else {
                Object object5 = ((IFn)const__3.getRawRoot()).invoke(map__20183);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__20183;
                    map__20183 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object6);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20183;
            map__20183 = null;
        }
        Object map__201832 = object;
        Object log2 = RT.get((Object)map__201832, (Object)const__6);
        Object serialize = RT.get((Object)map__201832, (Object)const__7);
        Object deserialize2 = RT.get((Object)map__201832, (Object)const__8);
        Object refresh_msec = RT.get((Object)map__201832, (Object)const__9);
        Object validator = RT.get((Object)map__201832, (Object)const__10);
        Object validator_ref = ((IFn)const__11.getRawRoot()).invoke(validator);
        Object state_ref = ((IFn)const__11.getRawRoot()).invoke(state2);
        Object watches_ref = ((IFn)const__11.getRawRoot()).invoke(null);
        Object close_ch = ((IFn)const__12.getRawRoot()).invoke();
        LoggedAtom logged_atom = new LoggedAtom(log2, close_ch, state_ref, serialize, deserialize2, validator_ref, watches_ref);
        ((IFn)const__13.getRawRoot()).invoke(state_ref, (Object)const__14, (Object)new logged$create_STAR_$fn__20184(watches_ref, logged_atom));
        Object c__10230__auto__20318 = ((IFn)const__15.getRawRoot()).invoke(const__16);
        Object captured_bindings__10231__auto__20319 = Var.getThreadBindingFrame();
        Object object7 = state2;
        state2 = null;
        Object object8 = watches_ref;
        watches_ref = null;
        Object object9 = captured_bindings__10231__auto__20319;
        captured_bindings__10231__auto__20319 = null;
        Object object10 = close_ch;
        close_ch = null;
        Object object11 = p__20182;
        p__20182 = null;
        Object object12 = serialize;
        serialize = null;
        Object object13 = validator;
        validator = null;
        Object object14 = deserialize2;
        deserialize2 = null;
        Object object15 = log2;
        log2 = null;
        Object object16 = refresh_msec;
        refresh_msec = null;
        Object object17 = state_ref;
        state_ref = null;
        Object object18 = map__201832;
        map__201832 = null;
        Object object19 = validator_ref;
        validator_ref = null;
        ((IFn)const__17.getRawRoot()).invoke((Object)new logged$create_STAR_$fn__20265(object7, object8, logged_atom, object9, object10, object11, object12, c__10230__auto__20318, object13, object14, object15, object16, object17, object18, object19));
        LoggedAtom loggedAtom = logged_atom;
        logged_atom = null;
        return loggedAtom;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return logged$create_STAR_.invokeStatic(object3, object4);
    }
}

