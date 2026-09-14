/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.concurrent.TimeoutException;

public final class qtune$min_ret$fn__23445
extends AFunction {
    Object args;
    Object timeout;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"flush");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__7 = RT.var((String)"datomic.api", (String)"q");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__9 = RT.keyword(null, (String)"timeout");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"prn");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__14 = RT.var((String)"datomic.common", (String)"root-cause");

    public qtune$min_ret$fn__23445(Object object, Object object2) {
        this.args = object;
        this.timeout = object2;
    }

    public Object invoke(Object p__23444) {
        IPersistentVector iPersistentVector;
        Object object = p__23444;
        p__23444 = null;
        Object vec__23446 = object;
        Object clause = RT.nth((Object)vec__23446, (int)RT.intCast((long)0L), null);
        Object object2 = vec__23446;
        vec__23446 = null;
        Object q2 = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        try {
            ((IFn)const__3.getRawRoot()).invoke(clause, (Object)"... ");
            ((IFn)const__4.getRawRoot()).invoke();
            Object object3 = q2;
            q2 = null;
            IPersistentVector ret = Tuple.create((Object)clause, (Object)RT.count((Object)((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke(object3, (Object)const__9, (Object)Tuple.create((Object)this.timeout)), this.args)));
            ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)ret));
            IPersistentVector iPersistentVector2 = ret;
            ret = null;
            iPersistentVector = iPersistentVector2;
        }
        catch (Exception e2) {
            if (!(((IFn)const__14.getRawRoot()).invoke((Object)e2) instanceof TimeoutException)) {
                Object e2 = null;
                throw (Throwable)e2;
            }
            ((IFn)const__10.getRawRoot()).invoke((Object)const__9);
            Object object4 = clause;
            clause = null;
            iPersistentVector = Tuple.create((Object)object4, null);
        }
        return iPersistentVector;
    }
}

