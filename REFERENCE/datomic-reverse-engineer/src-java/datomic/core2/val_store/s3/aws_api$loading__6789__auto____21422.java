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
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.val_store.s3;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Associative;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;

public final class aws_api$loading__6789__auto____21422
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"refer");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"clojure.core");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__3 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"clojure.core.async"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"go")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 31})));
    public static final AFn const__4 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"cognitect.aws.client.api"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"aws"));
    public static final AFn const__5 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"cognitect.caster"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"cast"));
    public static final AFn const__6 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.core2.anomalies"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"canom"));
    public static final AFn const__7 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.core2.async"), (Object)RT.keyword(null, (String)"refer"), (Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<!x")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 32})));
    public static final AFn const__8 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.core2.aws.s3.aws-api"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"s3"));
    public static final AFn const__9 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.core2.val-store.spi"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"spi"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.core2.val-store.s3"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"vs-s3"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"datomic.measure.io-stats"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"io-stats"));

    public Object invoke() {
        Class clazz;
        Var.pushThreadBindings((Associative)((Associative)RT.mapUniqueKeys((Object[])new Object[]{Compiler.LOADER, ((Object)((Object)this)).getClass().getClassLoader()})));
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6, (Object)const__7, (Object)const__8, (Object)const__9, (Object)const__10, (Object)const__11);
            clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"java.nio.ByteBuffer"));
        }
        finally {
            Var.popThreadBindings();
        }
        return clazz;
    }
}

