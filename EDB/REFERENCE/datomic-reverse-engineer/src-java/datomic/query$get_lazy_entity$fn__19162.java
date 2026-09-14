/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IDb;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class query$get_lazy_entity$fn__19162
extends AFunction {
    Object db;
    public static final Var const__2 = RT.var((String)"datomic.query", (String)"ref-val");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");

    public query$get_lazy_entity$fn__19162(Object object) {
        this.db = object;
    }

    public Object invoke(Object ret, Object d) {
        Object object;
        Object object2;
        Object and__5236__auto__19167;
        Object object3;
        Object or__5238__auto__19166;
        Object ref_QMARK_;
        Object object4;
        Object vtypeid;
        Object and__5236__auto__19165;
        Object object5;
        Object and__5236__auto__19164;
        Object attr;
        int attrid = ((IDatum)d).getA();
        Object object6 = attr = ((IDbImpl)this_.db).elementAt(attrid);
        Object attrk = object6 != null && object6 != Boolean.FALSE ? ((Attribute)attr).kw() : ((IDb)this_.db).keywordOf(attrid);
        Object object7 = d;
        d = null;
        Object v = ((IDatum)object7).getV();
        Object object8 = and__5236__auto__19164 = attr;
        if (object8 != null && object8 != Boolean.FALSE) {
            object5 = ((Attribute)attr).vtypeid;
        } else {
            object5 = and__5236__auto__19164;
            and__5236__auto__19164 = null;
        }
        Object object9 = and__5236__auto__19165 = (vtypeid = object5);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = vtypeid;
            vtypeid = null;
            object4 = Util.equiv((Object)object10, (long)20L) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object4 = and__5236__auto__19165;
            and__5236__auto__19165 = null;
        }
        Object object11 = ref_QMARK_ = object4;
        ref_QMARK_ = null;
        Object object12 = v;
        v = null;
        Object val = ((IFn)const__2.getRawRoot()).invoke(this_.db, object11, object12);
        IFn iFn = (IFn)const__3.getRawRoot();
        Object object13 = or__5238__auto__19166 = ret;
        if (object13 != null && object13 != Boolean.FALSE) {
            object3 = or__5238__auto__19166;
            or__5238__auto__19166 = null;
        } else {
            object3 = PersistentArrayMap.EMPTY;
        }
        Object object14 = and__5236__auto__19167 = attr;
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15 = attr;
            attr = null;
            object2 = Util.equiv((long)36L, (Object)((Attribute)object15).cardinality) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__19167;
            and__5236__auto__19167 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object16 = ret;
            ret = null;
            Object object17 = attrk;
            attrk = null;
            Object object18 = val;
            val = null;
            object = ((IFn)const__5.getRawRoot()).invoke(RT.get((Object)object16, (Object)object17, (Object)PersistentHashSet.EMPTY), object18);
        } else {
            object = val;
            val = null;
        }
        query$get_lazy_entity$fn__19162 this_ = null;
        return iFn.invoke(object3, attrk, object);
    }
}

