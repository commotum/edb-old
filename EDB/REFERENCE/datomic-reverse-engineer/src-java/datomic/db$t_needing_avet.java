/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.Datom;
import datomic.Entity;

public final class db$t_needing_avet
extends AFunction {
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"has-values?");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"eid->eidx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"unique"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"index"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2, Object d) {
        Object object;
        boolean and__5236__auto__13172;
        boolean bl;
        Object a = ((Datom)d).a();
        boolean or__5238__auto__13166 = Util.equiv((Object)a, (long)44L);
        if (or__5238__auto__13166) {
            bl = or__5238__auto__13166;
        } else {
            a = null;
            bl = and__5236__auto__13172 = Util.equiv((Object)a, (long)42L);
        }
        if (and__5236__auto__13172) {
            Object and__5236__auto__13171;
            Object object2 = and__5236__auto__13171 = ((Datom)d).v();
            if (object2 != null && object2 != Boolean.FALSE) {
                boolean and__5236__auto__13170 = ((Datom)d).added();
                if (and__5236__auto__13170) {
                    Object and__5236__auto__13169;
                    Object object3 = db2;
                    db2 = null;
                    Database prior = ((Database)object3).asOf(Numbers.unchecked_dec((Object)((Datom)d).tx()));
                    Entity ent = prior.entity(((Datom)d).e());
                    IFn iFn = (IFn)const__4.getRawRoot();
                    ILookupThunk iLookupThunk = __thunk__0__;
                    Entity entity2 = ent;
                    Object object4 = iLookupThunk.get((Object)entity2);
                    if (iLookupThunk == object4) {
                        __thunk__0__ = __site__0__.fault((Object)entity2);
                        object4 = __thunk__0__.get((Object)entity2);
                    }
                    Object object5 = and__5236__auto__13169 = iFn.invoke(object4);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        Object and__5236__auto__13168;
                        IFn iFn2 = (IFn)const__4.getRawRoot();
                        ILookupThunk iLookupThunk2 = __thunk__1__;
                        Entity entity3 = ent;
                        ent = null;
                        Object object6 = iLookupThunk2.get((Object)entity3);
                        if (iLookupThunk2 == object6) {
                            __thunk__1__ = __site__1__.fault((Object)entity3);
                            object6 = __thunk__1__.get((Object)entity3);
                        }
                        Object object7 = and__5236__auto__13168 = iFn2.invoke(object6);
                        if (object7 != null && object7 != Boolean.FALSE) {
                            Object and__5236__auto__13167;
                            Database database = prior;
                            prior = null;
                            Object object8 = and__5236__auto__13167 = ((IFn)const__7.getRawRoot()).invoke((Object)database, ((Datom)d).e());
                            if (object8 != null && object8 != Boolean.FALSE) {
                                Object object9 = d;
                                d = null;
                                object = Numbers.num((long)((IFn.LL)const__8.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)((Datom)object9).tx()))));
                            } else {
                                object = and__5236__auto__13167;
                                and__5236__auto__13167 = null;
                            }
                        } else {
                            object = and__5236__auto__13168;
                            and__5236__auto__13168 = null;
                        }
                    } else {
                        object = and__5236__auto__13169;
                        and__5236__auto__13169 = null;
                    }
                } else {
                    object = and__5236__auto__13170 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object = and__5236__auto__13171;
                and__5236__auto__13171 = null;
            }
        } else {
            object = and__5236__auto__13172 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$t_needing_avet.invokeStatic(object3, object4);
    }
}

