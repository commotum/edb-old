/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$with_tx$inject_all__14099;
import datomic.db.IProcessExpander;
import datomic.db.ProcessInpoint;
import java.util.ArrayList;
import java.util.HashMap;

public final class db$with_tx
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"part-requests");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"empty-tx-stat-registers");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"process-expander");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"calc-tempids");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"add-fulltext");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"add-ensured-data");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"pf-close!");
    public static final Keyword const__10 = RT.keyword(null, (String)"db-before");
    public static final Keyword const__11 = RT.keyword(null, (String)"db-after");
    public static final Keyword const__12 = RT.keyword(null, (String)"tx-data");
    public static final Keyword const__13 = RT.keyword(null, (String)"tempids");
    public static final Keyword const__14 = RT.keyword(null, (String)"tx-stats");
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"summarize-tx-stats");

    public static Object invokeStatic(Object db2, Object dispatcher, Object txdata) {
        ArrayList added = new ArrayList();
        HashMap local_tempids = new HashMap();
        Object part_reqs = ((IFn)const__0.getRawRoot()).invoke();
        db$with_tx$inject_all__14099 inject_all = new db$with_tx$inject_all__14099(local_tempids);
        Object tx_stat_registers = ((IFn)const__1.getRawRoot()).invoke();
        Object pe = ((IFn)const__2.getRawRoot()).invoke(db2, part_reqs, dispatcher, tx_stat_registers);
        Object object = part_reqs;
        part_reqs = null;
        ProcessInpoint pi = new ProcessInpoint(db2, object, pe);
        db$with_tx$inject_all__14099 db$with_tx$inject_all__14099 = inject_all;
        inject_all = null;
        ProcessInpoint processInpoint = pi;
        pi = null;
        Object object2 = txdata;
        txdata = null;
        ((IFn)db$with_tx$inject_all__14099).invoke((Object)processInpoint, object2);
        Object object3 = pe;
        pe = null;
        Object vec__14103 = ((IProcessExpander)object3).getData(local_tempids);
        Object data2 = RT.nth((Object)vec__14103, (int)RT.uncheckedIntCast((long)0L), null);
        Object object4 = vec__14103;
        vec__14103 = null;
        Object idmap = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)1L), null);
        HashMap hashMap = local_tempids;
        local_tempids = null;
        Object object5 = idmap;
        idmap = null;
        Object tempids = ((IFn)const__6.getRawRoot()).invoke(hashMap, object5);
        Object object6 = data2;
        data2 = null;
        Object newdb = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(db2, object6, added, tempids, tx_stat_registers), added);
        Object object7 = dispatcher;
        dispatcher = null;
        ((IFn)const__9.getRawRoot()).invoke(object7);
        Object[] objectArray = new Object[10];
        objectArray[0] = const__10;
        Object object8 = db2;
        db2 = null;
        objectArray[1] = object8;
        objectArray[2] = const__11;
        Object object9 = newdb;
        newdb = null;
        objectArray[3] = object9;
        objectArray[4] = const__12;
        ArrayList arrayList = added;
        added = null;
        objectArray[5] = arrayList;
        objectArray[6] = const__13;
        Object object10 = tempids;
        tempids = null;
        objectArray[7] = object10;
        objectArray[8] = const__14;
        Object object11 = tx_stat_registers;
        tx_stat_registers = null;
        objectArray[9] = ((IFn)const__15.getRawRoot()).invoke(object11);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$with_tx.invokeStatic(object4, object5, object6);
    }
}

