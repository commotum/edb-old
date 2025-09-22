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
import datomic.valcache$fn__9758$__GT_Server__9760;
import java.util.Arrays;

public final class valcache$fn__9758
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"->Server");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"sem"), Symbol.intern(null, (String)"concurrency"), Symbol.intern(null, (String)"socket-registry"), Symbol.intern(null, (String)"handled"), Symbol.intern(null, (String)"host"), Symbol.intern(null, (String)"port"), Symbol.intern(null, (String)"path"), Symbol.intern(null, (String)"shutdown-fn")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.valcache.Server");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.valcache.Server"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new valcache$fn__9758$__GT_Server__9760());
        return const__6;
    }

    public Object invoke() {
        return valcache$fn__9758.invokeStatic();
    }
}

