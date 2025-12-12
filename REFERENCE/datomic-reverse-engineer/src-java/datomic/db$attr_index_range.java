/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$attr_index_range$fn__12954;
import datomic.db.Attribute;
import datomic.db.IDb;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class db$attr_index_range
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-id");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"attribute-not-indexed");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__6 = RT.keyword(null, (String)"a");
    public static final Keyword const__7 = RT.keyword(null, (String)"v");

    public static Object invokeStatic(Object db2, Object a, Object start, Object end) {
        Object object;
        Object object2;
        Object attr;
        Object and__5236__auto__12959;
        long attrid = ((IFn.OOL)const__0.getRawRoot()).invokePrim(db2, a);
        Object object3 = and__5236__auto__12959 = (attr = ((IDbImpl)db2).elementAt(Numbers.num((long)attrid)));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = attr;
            attr = null;
            object2 = ((Attribute)object4).hasAVET();
        } else {
            object2 = and__5236__auto__12959;
            and__5236__auto__12959 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = null;
        } else {
            Object object5 = a;
            a = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, ((IFn)const__3.getRawRoot()).invoke((Object)"attribute: ", object5, (Object)" is not indexed"));
        }
        Object object6 = db2;
        Object object7 = end;
        end = null;
        IDb iDb = (IDb)db2;
        Object object8 = db2;
        db2 = null;
        Object object9 = start;
        start = null;
        return ((IFn)const__4.getRawRoot()).invoke(object6, (Object)new db$attr_index_range$fn__12954(attrid, object7), (Object)iDb.seekAVET((IDatum)((IFn)const__5.getRawRoot()).invoke(object8, (Object)const__6, (Object)Numbers.num((long)attrid), (Object)const__7, object9)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$attr_index_range.invokeStatic(object5, object6, object7, object8);
    }
}

