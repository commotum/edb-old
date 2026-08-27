/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Db;

public final class db$bootstrap_db_STAR_$ad__14149
extends AFunction {
    Object db;
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"bootstrap-maybe-resolve");

    public db$bootstrap_db_STAR_$ad__14149(Object object) {
        this.db = object;
    }

    public Object invoke(Object p__14148) {
        Object object = p__14148;
        p__14148 = null;
        Object vec__14150 = object;
        Object e = RT.nth((Object)vec__14150, (int)RT.uncheckedIntCast((long)0L), null);
        Object a = RT.nth((Object)vec__14150, (int)RT.uncheckedIntCast((long)1L), null);
        Object object2 = vec__14150;
        vec__14150 = null;
        Object v = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)2L), null);
        Object object3 = e;
        e = null;
        long l = RT.uncheckedLongCast((Object)((Number)((IFn)const__5.getRawRoot()).invoke(this.db, a)));
        Object object4 = a;
        a = null;
        Object object5 = v;
        v = null;
        return ((IFn.LLOLO)const__4.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)((IFn)const__5.getRawRoot()).invoke(this.db, object3))), l, ((IFn)const__6.getRawRoot()).invoke(this.db, object4, object5), ((Db)this.db).nextT());
    }
}

