/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Associative
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Associative;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class async$loading__6789__auto____19466
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.core.async"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"a"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<!!"), Symbol.intern(null, (String)">!!"), Symbol.intern(null, (String)"alts!"), Symbol.intern(null, (String)"alts!!"), Symbol.intern(null, (String)"alt!!"), Symbol.intern(null, (String)"chan"), Symbol.intern(null, (String)"offer!"), Symbol.intern(null, (String)"timeout"), Symbol.intern(null, (String)"go"), Symbol.intern(null, (String)"go-loop"), Symbol.intern(null, (String)"put!"), Symbol.intern(null, (String)"<!"), Symbol.intern(null, (String)"close!"), Symbol.intern(null, (String)">!")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 12})));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"cognitect.anomalies"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"anom"));

    public Object invoke() {
        Object object;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4);
        }
        finally {
            Var.popThreadBindings();
        }
        return object;
    }
}

