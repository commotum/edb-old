/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
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

public final class db$partbits
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Var const__4 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"not-a-db-id");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object db2, Object kw_or_parteid) {
        Object object;
        Object temp__5455__auto__12604;
        Object object2 = db2;
        db2 = null;
        Object object3 = temp__5455__auto__12604 = ((IFn)const__0.getRawRoot()).invoke(object2, kw_or_parteid);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5455__auto__12604;
            temp__5455__auto__12604 = null;
            Object id = object4;
            long part2 = ((IFn.LL)const__1.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)id)));
            Object object5 = id;
            id = null;
            long eidx = ((IFn.LL)const__2.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object5)));
            object = part2 == 0L ? (Number)Numbers.num((long)eidx) : (Number)(eidx == 0L ? Numbers.num((long)part2) : null);
        } else {
            Object object6 = kw_or_parteid;
            kw_or_parteid = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, ((IFn)const__6.getRawRoot()).invoke((Object)"Invalid db/id: ", object6));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$partbits.invokeStatic(object3, object4);
    }
}

