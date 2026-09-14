/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.log$fn__16251$__GT_LogTxIter__16292;
import java.util.Arrays;

public final class log$fn__16251
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"->LogTxIter");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"lookup"), Symbol.intern(null, (String)"root-val"), Symbol.intern(null, (String)"tail"), Symbol.intern(null, (String)"ridx"), Symbol.intern(null, (String)"dir"), Symbol.intern(null, (String)"didx"), Symbol.intern(null, (String)"seg"), Symbol.intern(null, (String)"sidx")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.log.LogTxIter");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.log.LogTxIter"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new log$fn__16251$__GT_LogTxIter__16292());
        return const__6;
    }

    public Object invoke() {
        return log$fn__16251.invokeStatic();
    }
}

