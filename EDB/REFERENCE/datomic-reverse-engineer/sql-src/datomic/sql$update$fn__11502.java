/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.sql.PreparedStatement;

public final class sql$update$fn__11502
extends AFunction {
    Object stmt;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"second");

    public sql$update$fn__11502(Object object) {
        this.stmt = object;
    }

    public Object invoke(Object i, Object cv) {
        Object object = i;
        i = null;
        Object object2 = cv;
        cv = null;
        ((PreparedStatement)this.stmt).setObject(RT.intCast((Object)Numbers.add((Object)object, (long)1L)), ((IFn)const__2.getRawRoot()).invoke(object2));
        return null;
    }
}
