/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$alter_attribute$attr__13216;
import datomic.db$alter_attribute$fn__13220;
import datomic.impl.db.IDatum;

public final class db$alter_attribute
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"get-entity");
    public static final Keyword const__1 = RT.keyword(null, (String)"raw");
    public static final Keyword const__3 = RT.keyword((String)"db.attr", (String)"preds");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"reserved-keyword?");
    public static final Keyword const__5 = RT.keyword((String)"db", (String)"ident");
    public static final Var const__6 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__7 = RT.keyword((String)"db.error", (String)"attr-pred-on-system-attr");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"attr-hook-attr-ids");

    public static Object invokeStatic(Object before, Object after, Object d) {
        Object object;
        Object and__5236__auto__13229;
        Object object2 = d;
        d = null;
        Object eid = ((IDatum)object2).getV();
        Object ebefore = ((IFn)const__0.getRawRoot()).invoke(before, eid, (Object)const__1, (Object)Boolean.TRUE);
        Object eafter = ((IFn)const__0.getRawRoot()).invoke(after, eid, (Object)const__1, (Object)Boolean.TRUE);
        db$alter_attribute$attr__13216 attr = new db$alter_attribute$attr__13216();
        Object object3 = and__5236__auto__13229 = RT.get((Object)eafter, (Object)const__3);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = ((IFn)const__4.getRawRoot()).invoke(RT.get((Object)eafter, (Object)const__5));
        } else {
            object = and__5236__auto__13229;
            and__5236__auto__13229 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            ((IFn)const__6.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)":db.attr/preds cannot be added to system attribute ", ((IFn)const__9.getRawRoot()).invoke(after, eid)));
        }
        db$alter_attribute$attr__13216 db$alter_attribute$attr__13216 = attr;
        attr = null;
        Object object4 = eafter;
        eafter = null;
        Object object5 = ebefore;
        ebefore = null;
        Object object6 = eid;
        eid = null;
        db$alter_attribute$fn__13220 db$alter_attribute$fn__13220 = new db$alter_attribute$fn__13220(before, (Object)db$alter_attribute$attr__13216, object4, object5, after, object6);
        Object object7 = after;
        after = null;
        Object object8 = before;
        before = null;
        return ((IFn)const__10.getRawRoot()).invoke((Object)db$alter_attribute$fn__13220, (Object)Tuple.create((Object)object7, null), ((IFn)const__11.getRawRoot()).invoke(object8));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$alter_attribute.invokeStatic(object4, object5, object6);
    }
}

