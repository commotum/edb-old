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
import datomic.backup$fn__20087$__GT_ValueBackup__20128;
import java.util.Arrays;

public final class backup$fn__20087
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"->ValueBackup");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"from-cluster"), Symbol.intern(null, (String)"value-storage"), Symbol.intern(null, (String)"progress"), Symbol.intern(null, (String)"incremental?"), Symbol.intern(null, (String)"ids->nodes"), Symbol.intern(null, (String)"throttle"), Symbol.intern(null, (String)"sem")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.backup.ValueBackup");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.backup.ValueBackup"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new backup$fn__20087$__GT_ValueBackup__20128());
        return const__6;
    }

    public Object invoke() {
        return backup$fn__20087.invokeStatic();
    }
}

