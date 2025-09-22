/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.pull$index_pull$fn__19038;
import datomic.pull$index_pull$fn__19042;

public final class pull$index_pull
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"parse-index-pull-arg-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"index");
    public static final Keyword const__5 = RT.keyword(null, (String)"reverse");
    public static final Keyword const__6 = RT.keyword(null, (String)"selector");
    public static final Keyword const__7 = RT.keyword(null, (String)"start");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__10 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"nil-input");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"rseek-datoms");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"seek-datoms");
    public static final Var const__17 = RT.var((String)"datomic.pull", (String)"normalized-pattern-cache");
    public static final Keyword const__19 = RT.keyword(null, (String)"avet");
    public static final Keyword const__20 = RT.keyword(null, (String)"e");
    public static final Keyword const__21 = RT.keyword(null, (String)"v");
    public static final Object const__23 = 36L;
    public static final Keyword const__27 = RT.keyword((String)"db.error", (String)"invalid-pull");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__29 = RT.keyword(null, (String)"aevt");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"not=");
    public static final Object const__32 = 20L;
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"contains?");
    public static final AFn const__34 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"aevt"), RT.keyword(null, (String)"avet")});
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__36 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Keyword const__37 = RT.keyword(null, (String)"default");
    public static final Var const__38 = RT.var((String)"clojure.core", (String)"name");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cardinality"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"cardinality"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"vtypeid"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object db2, Object arg_map) {
        Object object;
        boolean bl;
        Object object2;
        Object or__5238__auto__19046;
        Object object3;
        Object object4 = arg_map;
        arg_map = null;
        Object map__19037 = ((IFn)const__0.getRawRoot()).invoke(object4);
        Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__19037);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__19037;
            map__19037 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__19037;
            map__19037 = null;
        }
        Object map__190372 = object3;
        Object index2 = RT.get((Object)map__190372, (Object)const__4);
        Object reverse = RT.get((Object)map__190372, (Object)const__5);
        Object selector = RT.get((Object)map__190372, (Object)const__6);
        Object object7 = map__190372;
        map__190372 = null;
        Object start = RT.get((Object)object7, (Object)const__7);
        Object object8 = or__5238__auto__19046 = ((IFn)const__8.getRawRoot()).invoke(start);
        if (object8 != null && object8 != Boolean.FALSE) {
            object2 = or__5238__auto__19046;
            or__5238__auto__19046 = null;
        } else {
            Object or__5238__auto__19045;
            Object object9 = or__5238__auto__19045 = ((IFn)const__9.getRawRoot()).invoke(selector);
            if (object9 != null && object9 != Boolean.FALSE) {
                object2 = or__5238__auto__19045;
                or__5238__auto__19045 = null;
            } else {
                object2 = ((IFn)const__9.getRawRoot()).invoke(index2);
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            ((IFn)const__10.getRawRoot()).invoke((Object)const__11, (Object)"selector, index, and start attribute must be specified");
        }
        if (((Database)db2).isHistory()) {
            throw (Throwable)new IllegalStateException("Can't pull from history");
        }
        Object a = ((IFn)const__12.getRawRoot()).invoke(start);
        Object attrid = ((IFn)const__13.getRawRoot()).invoke(db2, a);
        Object attribute2 = ((IFn)const__14.getRawRoot()).invoke(db2, attrid);
        Object object10 = reverse;
        reverse = null;
        Object seek_fn = object10 != null && object10 != Boolean.FALSE ? const__15.getRawRoot() : const__16.getRawRoot();
        Object object11 = selector;
        selector = null;
        Object cached_selector = RT.get((Object)const__17.getRawRoot(), (Object)object11);
        Keyword pull_kw = Util.equiv((Object)index2, (Object)const__19) ? const__20 : const__21;
        boolean and__5236__auto__19048 = Util.equiv((Object)index2, (Object)const__19);
        if (and__5236__auto__19048) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object12 = attribute2;
            Object object13 = iLookupThunk.get(object12);
            if (iLookupThunk == object13) {
                __thunk__0__ = __site__0__.fault(object12);
                object13 = __thunk__0__.get(object12);
            }
            boolean and__5236__auto__19047 = Util.equiv((Object)object13, (long)36L);
            bl = and__5236__auto__19047 ? Numbers.lt((long)RT.count((Object)start), (long)2L) : and__5236__auto__19047;
        } else {
            bl = and__5236__auto__19048;
        }
        if (bl) {
            Object object14 = a;
            a = null;
            object = ((IFn)const__10.getRawRoot()).invoke((Object)const__27, ((IFn)const__28.getRawRoot()).invoke(object14, (Object)" is not card-one, as required for :avet when start has only A specified"));
        } else {
            Object object15;
            boolean and__5236__auto__19049 = Util.equiv((Object)index2, (Object)const__29);
            if (and__5236__auto__19049) {
                IFn iFn = (IFn)const__30.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__1__;
                Object object16 = attribute2;
                Object object17 = iLookupThunk.get(object16);
                if (iLookupThunk == object17) {
                    __thunk__1__ = __site__1__.fault(object16);
                    object17 = __thunk__1__.get(object16);
                }
                object15 = iFn.invoke(object17, const__23);
            } else {
                object15 = and__5236__auto__19049 ? Boolean.TRUE : Boolean.FALSE;
            }
            if (object15 != null && object15 != Boolean.FALSE) {
                Object object18 = a;
                a = null;
                object = ((IFn)const__10.getRawRoot()).invoke((Object)const__27, ((IFn)const__28.getRawRoot()).invoke(object18, (Object)" is not card-many, as required for :aevt"));
            } else {
                Object object19;
                boolean and__5236__auto__19050 = Util.equiv((Object)index2, (Object)const__29);
                if (and__5236__auto__19050) {
                    IFn iFn = (IFn)const__30.getRawRoot();
                    ILookupThunk iLookupThunk = __thunk__2__;
                    Object object20 = attribute2;
                    attribute2 = null;
                    Object object21 = iLookupThunk.get(object20);
                    if (iLookupThunk == object21) {
                        __thunk__2__ = __site__2__.fault(object20);
                        object21 = __thunk__2__.get(object20);
                    }
                    object19 = iFn.invoke(object21, const__32);
                } else {
                    object19 = and__5236__auto__19050 ? Boolean.TRUE : Boolean.FALSE;
                }
                if (object19 != null && object19 != Boolean.FALSE) {
                    Object object22 = a;
                    a = null;
                    object = ((IFn)const__10.getRawRoot()).invoke((Object)const__27, ((IFn)const__28.getRawRoot()).invoke(object22, (Object)" is not a ref, as required for :aevt"));
                } else {
                    Object object23 = ((IFn)const__33.getRawRoot()).invoke((Object)const__34, index2);
                    if (object23 != null && object23 != Boolean.FALSE) {
                        Keyword keyword = pull_kw;
                        pull_kw = null;
                        Object object24 = cached_selector;
                        cached_selector = null;
                        pull$index_pull$fn__19038 pull$index_pull$fn__19038 = new pull$index_pull$fn__19038(db2, keyword, object24);
                        Object object25 = attrid;
                        attrid = null;
                        Object object26 = seek_fn;
                        seek_fn = null;
                        Object object27 = db2;
                        db2 = null;
                        Object object28 = index2;
                        index2 = null;
                        Object object29 = start;
                        start = null;
                        object = ((IFn)const__35.getRawRoot()).invoke((Object)pull$index_pull$fn__19038, ((IFn)const__36.getRawRoot()).invoke((Object)new pull$index_pull$fn__19042(object25), ((IFn)object26).invoke(object27, object28, object29)));
                    } else {
                        Keyword keyword = const__37;
                        if (keyword != null && keyword != Boolean.FALSE) {
                            Object object30 = index2;
                            index2 = null;
                            object = ((IFn)const__10.getRawRoot()).invoke((Object)const__27, ((IFn)const__28.getRawRoot()).invoke(((IFn)const__38.getRawRoot()).invoke(object30), (Object)" is an invalid index, must be :avet or :aevt"));
                        } else {
                            object = null;
                        }
                    }
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pull$index_pull.invokeStatic(object3, object4);
    }
}

