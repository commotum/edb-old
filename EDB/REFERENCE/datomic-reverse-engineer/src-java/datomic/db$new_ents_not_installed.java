/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$new_ents_not_installed$needs_install_QMARK___13329;
import datomic.impl.db.IDatum;

public final class db$new_ents_not_installed
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj!");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"install-attrs");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"disj!");
    public static final Keyword const__9 = RT.keyword(null, (String)"default");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"empty?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"elements"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object datoms2) {
        Object object;
        block7: {
            Object eids;
            block6: {
                Object G__13335;
                Object vec__13336;
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object2 = db2;
                db2 = null;
                Object object3 = iLookupThunk.get(object2);
                if (iLookupThunk == object3) {
                    __thunk__0__ = __site__0__.fault(object2);
                    object3 = __thunk__0__.get(object2);
                }
                int p = RT.count((Object)object3);
                db$new_ents_not_installed$needs_install_QMARK___13329 needs_install_QMARK_ = new db$new_ents_not_installed$needs_install_QMARK___13329(p);
                Object eids2 = ((IFn)const__2.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY);
                Object object4 = datoms2;
                datoms2 = null;
                Object object5 = vec__13336 = (G__13335 = object4);
                vec__13336 = null;
                Object seq__13337 = ((IFn)const__3.getRawRoot()).invoke(object5);
                Object first__13338 = ((IFn)const__4.getRawRoot()).invoke(seq__13337);
                Object object6 = seq__13337;
                seq__13337 = null;
                Object seq__133372 = ((IFn)const__5.getRawRoot()).invoke(object6);
                first__13338 = null;
                seq__133372 = null;
                Object object7 = eids2;
                eids2 = null;
                Object eids3 = object7;
                Object object8 = G__13335;
                G__13335 = null;
                Object G__133352 = object8;
                while (true) {
                    Object vec__13339;
                    Object object9 = eids3;
                    eids3 = null;
                    eids = object9;
                    Object object10 = G__133352;
                    G__133352 = null;
                    Object object11 = vec__13339 = object10;
                    vec__13339 = null;
                    Object seq__13340 = ((IFn)const__3.getRawRoot()).invoke(object11);
                    Object first__13341 = ((IFn)const__4.getRawRoot()).invoke(seq__13340);
                    Object object12 = seq__13340;
                    seq__13340 = null;
                    Object seq__133402 = ((IFn)const__5.getRawRoot()).invoke(object12);
                    Object object13 = first__13341;
                    first__13341 = null;
                    Object d = object13;
                    Object object14 = seq__133402;
                    seq__133402 = null;
                    Object more = object14;
                    Object object15 = d;
                    if (object15 == null || object15 == Boolean.FALSE) break block6;
                    long e = ((IDatum)d).getE();
                    Object object16 = ((IFn)needs_install_QMARK_).invoke((Object)Numbers.num((long)e));
                    if (object16 != null && object16 != Boolean.FALSE) {
                        Object object17 = eids;
                        eids = null;
                        Object object18 = more;
                        more = null;
                        G__133352 = object18;
                        eids3 = ((IFn)const__6.getRawRoot()).invoke(object17, (Object)Numbers.num((long)e));
                        continue;
                    }
                    Object object19 = ((IFn)const__7.getRawRoot()).invoke((Object)((IDatum)d).getA());
                    if (object19 != null && object19 != Boolean.FALSE) {
                        Object object20 = eids;
                        eids = null;
                        Object object21 = d;
                        d = null;
                        Object object22 = more;
                        more = null;
                        G__133352 = object22;
                        eids3 = ((IFn)const__8.getRawRoot()).invoke(object20, ((IDatum)object21).getV());
                        continue;
                    }
                    Keyword keyword = const__9;
                    if (keyword == null || keyword == Boolean.FALSE) break;
                    Object object23 = eids;
                    eids = null;
                    Object object24 = more;
                    more = null;
                    G__133352 = object24;
                    eids3 = object23;
                }
                object = null;
                break block7;
            }
            Object object25 = eids;
            eids = null;
            Object eids4 = ((IFn)const__10.getRawRoot()).invoke(object25);
            Object object26 = ((IFn)const__11.getRawRoot()).invoke(eids4);
            if (object26 != null && object26 != Boolean.FALSE) {
                object = null;
            } else {
                object = eids4;
                eids4 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$new_ents_not_installed.invokeStatic(object3, object4);
    }
}

