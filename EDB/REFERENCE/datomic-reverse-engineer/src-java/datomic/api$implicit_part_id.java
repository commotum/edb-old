/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class api$implicit_part_id
extends AFunction
implements IFn.LO {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"implicit-part-id");

    public static Object invokeStatic(long part2) {
        return ((IFn.LO)const__0.getRawRoot()).invokePrim(part2);
    }

    public Object invoke(Object object) {
        return api$implicit_part_id.invokeStatic(RT.longCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return api$implicit_part_id.invokeStatic(l);
    }
}

