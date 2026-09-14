/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.ProcessExpander$replace_tempids__14011$fn__14016;
import datomic.db.ProcessExpander$replace_tempids__14011$fn__14018;
import datomic.db.ProcessExpander$replace_tempids__14011$tempid_at_index_QMARK___14012;
import datomic.impl.db.IDatum;

public final class ProcessExpander$replace_tempids__14011
extends AFunction {
    Object db;
    Object ids;
    Object replace_tempid;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-attr");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"tempid?");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"system-eid");
    public static final Keyword const__5 = RT.keyword((String)"db.type", (String)"tuple");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"map-indexed");
    public static final Keyword const__10 = RT.keyword(null, (String)"default");

    public ProcessExpander$replace_tempids__14011(Object object, Object object2, Object object3) {
        this.db = object;
        this.ids = object2;
        this.replace_tempid = object3;
    }

    public Object invoke(Object d) {
        Object object;
        Object object2;
        Object or__5238__auto__14024;
        int a = ((IDatum)d).getA();
        Object attr = ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)a);
        long e = ((IDatum)d).getE();
        Object v = ((IDatum)d).getV();
        boolean and__5236__auto__14021 = Util.equiv((Object)((Attribute)attr).vtypeid, (long)20L);
        Boolean tempid_v_QMARK_ = and__5236__auto__14021 ? ((IFn.LO)const__3.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)v))) : (and__5236__auto__14021 ? Boolean.TRUE : Boolean.FALSE);
        ProcessExpander$replace_tempids__14011$tempid_at_index_QMARK___14012 tempid_at_index_QMARK_ = new ProcessExpander$replace_tempids__14011$tempid_at_index_QMARK___14012(attr);
        Object object3 = attr;
        attr = null;
        boolean and__5236__auto__14022 = Util.equiv((Object)((Attribute)object3).vtypeid, (Object)((IFn)const__4.getRawRoot()).invoke(this_.db, (Object)const__5));
        Boolean tempid_in_v_QMARK_ = and__5236__auto__14022 ? ((IFn)new ProcessExpander$replace_tempids__14011$fn__14016(v, (Object)tempid_at_index_QMARK_)).invoke() : (and__5236__auto__14022 ? Boolean.TRUE : Boolean.FALSE);
        Object object4 = or__5238__auto__14024 = ((IFn.LO)const__3.getRawRoot()).invokePrim(e);
        if (object4 != null && object4 != Boolean.FALSE) {
            object2 = or__5238__auto__14024;
            or__5238__auto__14024 = null;
        } else {
            Boolean or__5238__auto__14023;
            Boolean bl = or__5238__auto__14023 = tempid_v_QMARK_;
            if (bl != null && bl != Boolean.FALSE) {
                object2 = or__5238__auto__14023;
                or__5238__auto__14023 = null;
            } else {
                object2 = tempid_in_v_QMARK_;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object5;
            IFn iFn = (IFn)(((IDatum)d).isAssertion() ? const__6.getRawRoot() : const__7.getRawRoot());
            Object object6 = ((IFn.LO)const__3.getRawRoot()).invokePrim(e);
            Object object7 = object6 != null && object6 != Boolean.FALSE ? ((IFn)this_.ids).invoke((Object)Numbers.num((long)e)) : Numbers.num((long)e);
            Integer n = a;
            Boolean bl = tempid_v_QMARK_;
            tempid_v_QMARK_ = null;
            if (bl != null && bl != Boolean.FALSE) {
                Object object8 = v;
                v = null;
                object5 = ((IFn)this_.replace_tempid).invoke(object8);
            } else {
                Boolean bl2 = tempid_in_v_QMARK_;
                tempid_in_v_QMARK_ = null;
                if (bl2 != null && bl2 != Boolean.FALSE) {
                    ProcessExpander$replace_tempids__14011$tempid_at_index_QMARK___14012 processExpander$replace_tempids__14011$tempid_at_index_QMARK___14012 = tempid_at_index_QMARK_;
                    tempid_at_index_QMARK_ = null;
                    Object object9 = v;
                    v = null;
                    object5 = ((IFn)const__8.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__9.getRawRoot()).invoke((Object)new ProcessExpander$replace_tempids__14011$fn__14018((Object)processExpander$replace_tempids__14011$tempid_at_index_QMARK___14012, this_.replace_tempid)), object9);
                } else {
                    Keyword keyword = const__10;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        object5 = v;
                        v = null;
                    } else {
                        object5 = null;
                    }
                }
            }
            Object object10 = d;
            d = null;
            ProcessExpander$replace_tempids__14011 this_ = null;
            object = iFn.invoke(object7, (Object)n, object5, (Object)Numbers.num((long)((IDatum)object10).getT()));
        } else {
            object = d;
            Object var1_1 = null;
        }
        return object;
    }
}

