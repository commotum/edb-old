/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$LLL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.io.Serializable;

public final class ProcessExpander$replace_tempid__14006
extends AFunction {
    Object ids;
    Object local_tempids;
    long basis;
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Var const__5 = RT.var((String)"clojure.set", (String)"map-invert");
    public static final Var const__6 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__7 = RT.keyword((String)"db.error", (String)"tempid-not-an-entity");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");

    public ProcessExpander$replace_tempid__14006(Object object, Object object2, long l) {
        this.ids = object;
        this.local_tempids = object2;
        this.basis = l;
    }

    public Object invoke(Object v) {
        Object object;
        Object or__5238__auto__14010;
        Object object2 = or__5238__auto__14010 = ((IFn)this_.ids).invoke(v);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__14010;
            or__5238__auto__14010 = null;
        } else {
            Boolean or__5238__auto__14009;
            boolean and__5236__auto__14008 = Util.equiv((long)3L, (long)((IFn.LL)const__2.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)v))));
            Boolean bl = or__5238__auto__14009 = and__5236__auto__14008 ? (Serializable)Numbers.num((long)((IFn.LLL)const__3.getRawRoot()).invokePrim(3L, this_.basis)) : (Serializable)(and__5236__auto__14008 ? Boolean.TRUE : Boolean.FALSE);
            if (bl != null && bl != Boolean.FALSE) {
                object = or__5238__auto__14009;
                or__5238__auto__14009 = null;
            } else {
                Object v2;
                Object object3 = v;
                Object object4 = v;
                v = null;
                Object object5 = v2 = RT.get((Object)((IFn)const__5.getRawRoot()).invoke(this_.local_tempids), (Object)object3, (Object)object4);
                v2 = null;
                ProcessExpander$replace_tempid__14006 this_ = null;
                object = ((IFn)const__6.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)"tempid '", object5, (Object)"' used only as value in transaction"));
            }
        }
        return object;
    }
}

