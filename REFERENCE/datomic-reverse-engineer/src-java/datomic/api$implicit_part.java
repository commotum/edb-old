/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class api$implicit_part
extends AFunction
implements IFn.LL {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"implicit-part");

    public static long invokeStatic(long id) {
        return ((IFn.LL)const__0.getRawRoot()).invokePrim(id);
    }

    public Object invoke(Object object) {
        return new Long(api$implicit_part.invokeStatic(RT.longCast((Object)((Number)object))));
    }

    public final long invokePrim(long l) {
        return api$implicit_part.invokeStatic(l);
    }
}

