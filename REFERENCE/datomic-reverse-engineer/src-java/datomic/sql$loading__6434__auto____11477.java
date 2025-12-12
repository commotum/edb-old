/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Namespace
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Associative;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Namespace;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class sql$loading__6434__auto____11477
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__1 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.error"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"error"));
    public static final AFn const__2 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.string"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"str"));
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Keyword const__5 = RT.keyword(null, (String)"exclude");
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"update"));

    public Object invoke() {
        Object object;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)const__2);
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.sql.Connection"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.sql.PreparedStatement"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.sql.ResultSet"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.sql.Types"));
            ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"javax.sql.DataSource"));
            object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, (Object)const__5, (Object)const__6);
        }
        finally {
            Var.popThreadBindings();
        }
        return object;
    }
}

