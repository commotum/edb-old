/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.excise$create_es_pred$reify__14859$fn__14860;
import datomic.excise.ExcisePred;
import datomic.impl.db.IDatum;

public final class excise$create_es_pred$reify__14859
implements ExcisePred,
IObj {
    final IPersistentMap __meta;
    Object e__GT_xpreds;
    Object db;
    Object xpreds;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__1 = RT.var((String)"datomic.excise", (String)"ep-datoms");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"datomic.excise", (String)"ref-datom?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"some");

    public excise$create_es_pred$reify__14859(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.e__GT_xpreds = object;
        this.db = object2;
        this.xpreds = object3;
    }

    public excise$create_es_pred$reify__14859(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new excise$create_es_pred$reify__14859(iPersistentMap, this.e__GT_xpreds, this.db, this.xpreds);
    }

    public Object ep_remove_QMARK_(Object d) {
        Object object;
        Object temp__5457__auto__14863;
        Object object2 = ((IFn)const__4.getRawRoot()).invoke(this_.db, d);
        Object object3 = temp__5457__auto__14863 = ((IFn)const__2.getRawRoot()).invoke(RT.get((Object)this_.e__GT_xpreds, (Object)Numbers.num((long)((IDatum)d).getE())), object2 != null && object2 != Boolean.FALSE ? RT.get((Object)this_.e__GT_xpreds, (Object)((IDatum)d).getV()) : null);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__14863;
            temp__5457__auto__14863 = null;
            Object xpreds = object4;
            Object object5 = d;
            d = null;
            Object object6 = xpreds;
            xpreds = null;
            excise$create_es_pred$reify__14859 this_ = null;
            object = ((IFn)const__5.getRawRoot()).invoke((Object)new excise$create_es_pred$reify__14859$fn__14860(object5), object6);
        } else {
            object = null;
        }
        return object;
    }

    public Object ep_datoms() {
        excise$create_es_pred$reify__14859 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), this_.xpreds);
    }
}

