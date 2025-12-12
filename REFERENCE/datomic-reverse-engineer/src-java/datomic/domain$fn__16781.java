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
 *  clojure.lang.Tuple
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
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.domain$fn__16781$__GT_ValcachePoller__16783;
import java.util.Arrays;

public final class domain$fn__16781
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"->ValcachePoller");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"valcache-group-config"), (Object)Symbol.intern(null, (String)"server-specs-ref"), (Object)Symbol.intern(null, (String)"timer")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.domain.ValcachePoller");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.domain.ValcachePoller"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new domain$fn__16781$__GT_ValcachePoller__16783());
        return const__6;
    }

    public Object invoke() {
        return domain$fn__16781.invokeStatic();
    }
}

