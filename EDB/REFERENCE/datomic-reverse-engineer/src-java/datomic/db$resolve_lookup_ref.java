/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;
import datomic.db.Attribute;
import datomic.db.IDb;
import java.util.List;

public final class db$resolve_lookup_ref
extends AFunction {
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"invalid-lookup-ref");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"lookup-ref-not-supported");
    public static final Var const__13 = RT.var((String)"datomic.iter", (String)"iget");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"attr-index-range");
    public static final Keyword const__15 = RT.keyword((String)"db.error", (String)"lookup-ref-attr-not-unique");

    public static Object invokeStatic(Object db2, Object x) {
        Object object;
        if (2L == (long)RT.count((Object)x)) {
        } else {
            ((IFn)const__3.getRawRoot()).invoke((Object)const__4, ((IFn)const__5.getRawRoot()).invoke((Object)"Invalid list form: ", x));
        }
        Object a = ((List)x).get(RT.uncheckedIntCast((long)0L));
        Object object2 = x;
        x = null;
        Object v = ((List)object2).get(RT.uncheckedIntCast((long)1L));
        Object aid = ((IFn)const__8.getRawRoot()).invoke(db2, a);
        Object attr = ((IFn)const__9.getRawRoot()).invoke(db2, aid);
        if (Util.equiv((long)27L, (Object)((Attribute)attr).vtypeid)) {
            ((IFn)const__3.getRawRoot()).invoke((Object)const__11, ((IFn)const__5.getRawRoot()).invoke((Object)"Lookup ref not supported for ", ((Attribute)attr).kw()));
        }
        Object object3 = ((Attribute)attr).unique;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = aid;
            aid = null;
            if (Util.equiv((Object)object4, (long)10L)) {
                Object object5 = db2;
                db2 = null;
                Object e = v;
                v = null;
                object = ((IDb)object5).idOf(e);
            } else {
                Object temp__5457__auto__12602;
                Object object6 = db2;
                db2 = null;
                Object e = a;
                a = null;
                Object object7 = temp__5457__auto__12602 = ((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object6, e, v, null));
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = temp__5457__auto__12602;
                    temp__5457__auto__12602 = null;
                    Object datum2 = object8;
                    Object e2 = v;
                    v = null;
                    if (Util.equiv(e2, (Object)((Datom)datum2).v())) {
                        Object object9 = datum2;
                        datum2 = null;
                        object = ((Datom)object9).e();
                    } else {
                        object = null;
                    }
                } else {
                    object = null;
                }
            }
        } else {
            Object object10 = attr;
            attr = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)const__15, ((IFn)const__5.getRawRoot()).invoke((Object)"Attribute values not unique: ", ((Attribute)object10).kw()));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$resolve_lookup_ref.invokeStatic(object3, object4);
    }
}

