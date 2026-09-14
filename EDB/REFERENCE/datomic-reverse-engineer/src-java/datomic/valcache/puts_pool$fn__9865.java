/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IRef
 *  clojure.lang.Keyword
 *  clojure.lang.MultiFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IRef;
import clojure.lang.Keyword;
import clojure.lang.MultiFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class puts_pool$fn__9865
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.valcache.puts-pool", (String)"get-from-put");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"deref");
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final Keyword const__8 = RT.keyword(null, (String)"source");
    public static final Keyword const__9 = RT.keyword(null, (String)"default");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"global-hierarchy");

    /*
     * WARNING - void declaration
     */
    public static Object invokeStatic() {
        Var var;
        boolean bl;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__3);
        Var v__5445__auto__9868 = var2;
        boolean and__5236__auto__9867 = v__5445__auto__9868.hasRoot();
        if (and__5236__auto__9867) {
            Var var3 = v__5445__auto__9868;
            v__5445__auto__9868 = null;
            bl = ((IFn)const__6.getRawRoot()).invoke((Object)var3) instanceof MultiFn;
        } else {
            void var1_1;
            bl = var1_1;
        }
        if (bl) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__7);
            var = var4;
            var4.bindRoot((Object)new MultiFn("get-from-put", (IFn)const__8, (Object)const__9, (IRef)const__10));
        }
        return var;
    }

    public Object invoke() {
        return puts_pool$fn__9865.invokeStatic();
    }
}

