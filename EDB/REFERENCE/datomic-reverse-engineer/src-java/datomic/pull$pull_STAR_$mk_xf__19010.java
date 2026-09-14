/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.pull$pull_STAR_$mk_xf__19010$fn__19014;

public final class pull$pull_STAR_$mk_xf__19010
extends AFunction {
    Object spec;
    Object db;
    Object recursed;
    Object prefer_aevt_QMARK_;
    Object e;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"update-in");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__7 = RT.keyword(null, (String)"subspec");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"dec");
    public static final Keyword const__10 = RT.keyword(null, (String)"dbid");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"...");
    public static final Keyword const__13 = RT.keyword(null, (String)"else");
    public static final Var const__14 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"invalid-attr-subspec");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"identity");

    public pull$pull_STAR_$mk_xf__19010(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.spec = object;
        this.db = object2;
        this.recursed = object3;
        this.prefer_aevt_QMARK_ = object4;
        this.e = object5;
    }

    public Object invoke(Object path2, Object subspec, Object def_subspec) {
        Object object;
        Object object2;
        if (Util.identical((Object)subspec, null)) {
            Object object3 = def_subspec;
            def_subspec = null;
            object2 = Tuple.create((Object)object3, (Object)this.recursed);
        } else {
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(subspec);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = subspec;
                subspec = null;
                object2 = Tuple.create((Object)object5, (Object)this.recursed);
            } else {
                Object idc;
                Object object6 = ((IFn)const__2.getRawRoot()).invoke(subspec);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = subspec;
                    subspec = null;
                    boolean and__5236__auto__19017 = Numbers.isPos((Object)object7);
                    Object object8 = and__5236__auto__19017 ? ((IFn)const__4.getRawRoot()).invoke(((IFn)this.recursed).invoke(this.e)) : (and__5236__auto__19017 ? Boolean.TRUE : Boolean.FALSE);
                    if (object8 != null && object8 != Boolean.FALSE) {
                        Object object9 = path2;
                        path2 = null;
                        object2 = Tuple.create((Object)((IFn)const__5.getRawRoot()).invoke(this.spec, ((IFn)const__6.getRawRoot()).invoke(object9, (Object)const__7), const__8.getRawRoot()), (Object)((IFn)const__6.getRawRoot()).invoke(this.recursed, this.e));
                    } else {
                        PersistentArrayMap persistentArrayMap;
                        Object temp__5455__auto__19018;
                        Object object10 = temp__5455__auto__19018 = RT.get((Object)this.spec, (Object)const__10);
                        if (object10 != null && object10 != Boolean.FALSE) {
                            Object object11 = temp__5455__auto__19018;
                            temp__5455__auto__19018 = null;
                            idc = object11;
                            Object[] objectArray = new Object[2];
                            objectArray[0] = const__10;
                            Object object12 = idc;
                            idc = null;
                            objectArray[1] = object12;
                            persistentArrayMap = RT.mapUniqueKeys((Object[])objectArray);
                        } else {
                            persistentArrayMap = PersistentArrayMap.EMPTY;
                        }
                        object2 = Tuple.create((Object)persistentArrayMap, (Object)this.recursed);
                    }
                } else if (Util.equiv((Object)const__12, (Object)subspec)) {
                    Object object13 = ((IFn)const__4.getRawRoot()).invoke(((IFn)this.recursed).invoke(this.e));
                    if (object13 != null && object13 != Boolean.FALSE) {
                        object2 = Tuple.create((Object)this.spec, (Object)((IFn)const__6.getRawRoot()).invoke(this.recursed, this.e));
                    } else {
                        PersistentArrayMap persistentArrayMap;
                        Object temp__5455__auto__19019;
                        Object object14 = temp__5455__auto__19019 = RT.get((Object)this.spec, (Object)const__10);
                        if (object14 != null && object14 != Boolean.FALSE) {
                            Object object15 = temp__5455__auto__19019;
                            temp__5455__auto__19019 = null;
                            idc = object15;
                            Object[] objectArray = new Object[2];
                            objectArray[0] = const__10;
                            Object object16 = idc;
                            idc = null;
                            objectArray[1] = object16;
                            persistentArrayMap = RT.mapUniqueKeys((Object[])objectArray);
                        } else {
                            persistentArrayMap = PersistentArrayMap.EMPTY;
                        }
                        object2 = Tuple.create((Object)persistentArrayMap, (Object)this.recursed);
                    }
                } else {
                    Keyword keyword = const__13;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        Object object17 = subspec;
                        Object object18 = subspec;
                        subspec = null;
                        object2 = ((IFn)const__14.getRawRoot()).invoke((Object)const__15, ((IFn)const__16.getRawRoot()).invoke((Object)"Cannot interpret as sub-pull pattern: ", object17, (Object)" of class: ", ((IFn)const__17.getRawRoot()).invoke(object18)));
                    } else {
                        object2 = null;
                    }
                }
            }
        }
        IPersistentVector vec__19011 = object2;
        Object _subspec = RT.nth((Object)vec__19011, (int)RT.intCast((long)0L), null);
        IPersistentVector iPersistentVector = vec__19011;
        vec__19011 = null;
        Object _recursed = RT.nth((Object)iPersistentVector, (int)RT.intCast((long)1L), null);
        Object object19 = _subspec;
        if (object19 != null && object19 != Boolean.FALSE) {
            _recursed = null;
            _subspec = null;
            object = new pull$pull_STAR_$mk_xf__19010$fn__19014(this.db, _recursed, _subspec, this.prefer_aevt_QMARK_);
        } else {
            object = const__21.getRawRoot();
        }
        return object;
    }
}

