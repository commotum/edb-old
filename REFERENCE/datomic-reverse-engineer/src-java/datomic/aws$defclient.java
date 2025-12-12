/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.aws$defclient$fn__17395;
import java.io.StringWriter;

public final class aws$defclient
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__9 = (AFn)Symbol.intern((String)"clojure.core", (String)"defn");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"client");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Keyword const__12 = RT.keyword(null, (String)"tag");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"datomic.aws", (String)"newclient");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"com.amazonaws.auth.DefaultAWSCredentialsProviderChain.");
    public static final AFn const__16 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"creds"));
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"creds");
    public static final AFn const__19 = (AFn)Symbol.intern((String)"datomic.aws", (String)"newclient");
    public static final AFn const__20 = (AFn)Symbol.intern((String)"datomic.aws", (String)"credentials");
    public static final AFn const__21 = (AFn)Symbol.intern(null, (String)"creds");
    public static final AFn const__22 = (AFn)Symbol.intern((String)"datomic.aws", (String)"newclient");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"com.amazonaws.auth.DefaultAWSCredentialsProviderChain.");
    public static final AFn const__26 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"config"));
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__28 = (AFn)Symbol.intern(null, (String)"config");
    public static final AFn const__29 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__32 = (AFn)Symbol.intern(null, (String)"region");
    public static final AFn const__33 = (AFn)Symbol.intern((String)"clojure.core", (String)"get");
    public static final AFn const__34 = (AFn)Symbol.intern(null, (String)"config");
    public static final Keyword const__35 = RT.keyword(null, (String)"region");
    public static final AFn const__36 = (AFn)Symbol.intern(null, (String)"override-endpoint");
    public static final AFn const__37 = (AFn)Symbol.intern((String)"clojure.core", (String)"get");
    public static final AFn const__38 = (AFn)Symbol.intern(null, (String)"config");
    public static final Keyword const__39 = RT.keyword(null, (String)"override-endpoint");
    public static final AFn const__40 = (AFn)Symbol.intern(null, (String)"conf");
    public static final AFn const__41 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"data-to-object");
    public static final AFn const__42 = (AFn)Symbol.intern((String)"clojure.core", (String)"dissoc");
    public static final AFn const__43 = (AFn)Symbol.intern(null, (String)"config");
    public static final AFn const__44 = (AFn)Symbol.intern(null, (String)"com.amazonaws.ClientConfiguration");
    public static final AFn const__45 = (AFn)Symbol.intern(null, (String)"conn");
    public static final AFn const__46 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__47 = (AFn)Symbol.intern(null, (String)"creds");
    public static final AFn const__48 = (AFn)Symbol.intern((String)"datomic.aws", (String)"newclient");
    public static final AFn const__49 = (AFn)Symbol.intern((String)"datomic.aws", (String)"credentials");
    public static final AFn const__50 = (AFn)Symbol.intern(null, (String)"creds");
    public static final AFn const__51 = (AFn)Symbol.intern(null, (String)"conf");
    public static final AFn const__52 = (AFn)Symbol.intern((String)"datomic.aws", (String)"newclient");
    public static final AFn const__53 = (AFn)Symbol.intern(null, (String)"com.amazonaws.auth.DefaultAWSCredentialsProviderChain.");
    public static final AFn const__54 = (AFn)Symbol.intern(null, (String)"conf");
    public static final AFn const__55 = (AFn)Symbol.intern((String)"clojure.core", (String)"cond");
    public static final AFn const__56 = (AFn)Symbol.intern(null, (String)"override-endpoint");
    public static final AFn const__57 = (AFn)Symbol.intern(null, (String)".setEndpoint");
    public static final AFn const__58 = (AFn)Symbol.intern(null, (String)"conn");
    public static final AFn const__59 = (AFn)Symbol.intern((String)"clojure.core", (String)"str");
    public static final AFn const__60 = (AFn)Symbol.intern(null, (String)"override-endpoint");
    public static final AFn const__61 = (AFn)Symbol.intern(null, (String)"region");
    public static final AFn const__62 = (AFn)Symbol.intern(null, (String)".setEndpoint");
    public static final AFn const__63 = (AFn)Symbol.intern(null, (String)"conn");
    public static final AFn const__64 = (AFn)Symbol.intern((String)"datomic.aws", (String)"endpoint-for");
    public static final AFn const__65 = (AFn)Symbol.intern(null, (String)"region");
    public static final AFn const__66 = (AFn)Symbol.intern(null, (String)"conn");
    public static final AFn const__67 = (AFn)Symbol.intern(null, (String)"client");
    public static final AFn const__68 = (AFn)Symbol.intern(null, (String)"creds");

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object cls, Object service) {
        Object ns;
        IFn iFn = (IFn)const__0.getRawRoot();
        StringWriter s__6071__auto__17400 = new StringWriter();
        ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)s__6071__auto__17400));
        StringWriter stringWriter = s__6071__auto__17400;
        s__6071__auto__17400 = null;
        Object docstr = iFn.invoke((Object)"Create a client. Config options are:\n\n:region    String or Keyword\n:override-endpoint String (Optional, to override the default AWS endpoint).\n\nPlus any of the following:\n", ((IFn)new aws$defclient$fn__17395(stringWriter)).invoke());
        Object object = ns = ((IFn)const__4.getRawRoot()).invoke(cls);
        ns = null;
        Object tag = ((IFn)const__5.getRawRoot()).invoke((Object)((Class)object).getName());
        Object object2 = docstr;
        docstr = null;
        Object object3 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)PersistentVector.EMPTY, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__12, tag}))), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__13), ((IFn)const__8.getRawRoot()).invoke(cls), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__14))))))))));
        Object object4 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)const__16, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__12, tag}))), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__17), ((IFn)const__8.getRawRoot()).invoke((Object)const__18), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__19), ((IFn)const__8.getRawRoot()).invoke(cls), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__20), ((IFn)const__8.getRawRoot()).invoke((Object)const__21))))))), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__22), ((IFn)const__8.getRawRoot()).invoke(cls), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__23)))))))))))));
        Object[] objectArray = new Object[2];
        objectArray[0] = const__12;
        Object object5 = tag;
        tag = null;
        objectArray[1] = object5;
        Object object6 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__48), ((IFn)const__8.getRawRoot()).invoke(cls), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__49), ((IFn)const__8.getRawRoot()).invoke((Object)const__50)))), ((IFn)const__8.getRawRoot()).invoke((Object)const__51))));
        Object object7 = cls;
        cls = null;
        Object object8 = service;
        service = null;
        return ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__9), ((IFn)const__8.getRawRoot()).invoke((Object)const__10), ((IFn)const__8.getRawRoot()).invoke(object2), object3, object4, ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)const__26, (Object)RT.mapUniqueKeys((Object[])objectArray))), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__27), ((IFn)const__8.getRawRoot()).invoke((Object)const__28), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__29), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__30.getRawRoot()).invoke(const__31.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__32), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__33), ((IFn)const__8.getRawRoot()).invoke((Object)const__34), ((IFn)const__8.getRawRoot()).invoke((Object)const__35)))), ((IFn)const__8.getRawRoot()).invoke((Object)const__36), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__37), ((IFn)const__8.getRawRoot()).invoke((Object)const__38), ((IFn)const__8.getRawRoot()).invoke((Object)const__39)))), ((IFn)const__8.getRawRoot()).invoke((Object)const__40), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__41), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__42), ((IFn)const__8.getRawRoot()).invoke((Object)const__43), ((IFn)const__8.getRawRoot()).invoke((Object)const__35), ((IFn)const__8.getRawRoot()).invoke((Object)const__39)))), ((IFn)const__8.getRawRoot()).invoke((Object)const__44)))), ((IFn)const__8.getRawRoot()).invoke((Object)const__45), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__46), ((IFn)const__8.getRawRoot()).invoke((Object)const__47), object6, ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__52), ((IFn)const__8.getRawRoot()).invoke(object7), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__53)))), ((IFn)const__8.getRawRoot()).invoke((Object)const__54))))))))))), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__55), ((IFn)const__8.getRawRoot()).invoke((Object)const__56), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__57), ((IFn)const__8.getRawRoot()).invoke((Object)const__58), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__59), ((IFn)const__8.getRawRoot()).invoke((Object)"http://"), ((IFn)const__8.getRawRoot()).invoke((Object)const__60))))))), ((IFn)const__8.getRawRoot()).invoke((Object)const__61), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__62), ((IFn)const__8.getRawRoot()).invoke((Object)const__63), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__64), ((IFn)const__8.getRawRoot()).invoke(object8), ((IFn)const__8.getRawRoot()).invoke((Object)const__65)))))))))), ((IFn)const__8.getRawRoot()).invoke((Object)const__66)))), ((IFn)const__8.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)const__67), ((IFn)const__8.getRawRoot()).invoke((Object)const__68))))))))))));
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
        return aws$defclient.invokeStatic(object5, object6, object7, object8);
    }
}

