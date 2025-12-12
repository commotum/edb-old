/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import datomic.iter.Iter;
import datomic.query$rae$fn__19135;
import datomic.query$rae$fn__19138;

public final class query$rae
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__3 = RT.keyword(null, (String)"v");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__6 = RT.var((String)"datomic.query", (String)"emap");
    public static final Var const__7 = RT.var((String)"datomic.iter", (String)"reduce");

    public static Object invokeStatic(Object db2, Object r, Object a) {
        Object object;
        Object object2;
        Object attr;
        Object and__5236__auto__19142;
        Object object3;
        Object and__5236__auto__19141;
        Object object4 = a;
        a = null;
        Object attrid = ((IFn)const__0.getRawRoot()).invoke(db2, object4);
        Object object5 = r;
        r = null;
        Object rid = ((IFn)const__0.getRawRoot()).invoke(db2, object5);
        query$rae$fn__19135 query$rae$fn__19135 = new query$rae$fn__19135(attrid, rid);
        Object object6 = rid;
        rid = null;
        Object iter2 = ((IFn)const__1.getRawRoot()).invoke(db2, (Object)query$rae$fn__19135, (Object)((IDb)db2).seekRAET((IDatum)((IFn)const__2.getRawRoot()).invoke(db2, (Object)const__3, object6, (Object)const__4, attrid)));
        Object object7 = and__5236__auto__19141 = attrid;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = attrid;
            attrid = null;
            object3 = ((IFn)const__5.getRawRoot()).invoke(db2, object8);
        } else {
            object3 = and__5236__auto__19141;
            and__5236__auto__19141 = null;
        }
        Object object9 = and__5236__auto__19142 = (attr = object3);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = attr;
            attr = null;
            object2 = ((Attribute)object10).isComponent;
        } else {
            object2 = and__5236__auto__19142;
            and__5236__auto__19142 = null;
        }
        Object component_QMARK_ = object2;
        Object object11 = iter2;
        if (object11 != null && object11 != Boolean.FALSE) {
            Object object12 = component_QMARK_;
            component_QMARK_ = null;
            if (object12 != null && object12 != Boolean.FALSE) {
                Object object13 = db2;
                db2 = null;
                Object object14 = iter2;
                iter2 = null;
                object = ((IFn)const__6.getRawRoot()).invoke(object13, (Object)Numbers.num((long)((IDatum)((Iter)object14).get()).getE()));
            } else {
                Object object15 = db2;
                db2 = null;
                Object object16 = iter2;
                iter2 = null;
                object = ((IFn)const__7.getRawRoot()).invoke((Object)new query$rae$fn__19138(object15), (Object)PersistentHashSet.EMPTY, object16);
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return query$rae.invokeStatic(object4, object5, object6);
    }
}

