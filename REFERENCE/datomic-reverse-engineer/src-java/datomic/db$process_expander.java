/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.ProcessExpander;
import java.util.ArrayList;

public final class db$process_expander
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attr-hook-attr-ids");

    public static Object invokeStatic(Object db2, Object part_reqs, Object dispatcher, Object tx_stat_registers) {
        Object object = db2;
        Object object2 = part_reqs;
        part_reqs = null;
        Object object3 = db2;
        db2 = null;
        Object object4 = dispatcher;
        dispatcher = null;
        Object object5 = tx_stat_registers;
        tx_stat_registers = null;
        return new ProcessExpander(object, object2, new ArrayList(), ((IFn)const__0.getRawRoot()).invoke(object3), object4, object5);
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
        return db$process_expander.invokeStatic(object5, object6, object7, object8);
    }
}

