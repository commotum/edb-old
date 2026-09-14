/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class aws$newclient
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"clojure.core", (String)"instance?");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"com.amazonaws.auth.AWSCredentialsProvider");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"new");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.auth.AWSCredentialsProvider")});
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"new");
    public static final AFn const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.auth.AWSCredentials")});
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__15 = (AFn)Symbol.intern((String)"clojure.core", (String)"instance?");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"com.amazonaws.auth.AWSCredentialsProvider");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"new");
    public static final AFn const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.auth.AWSCredentialsProvider")});
    public static final AFn const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.ClientConfiguration")});
    public static final AFn const__22 = (AFn)Symbol.intern(null, (String)"new");
    public static final AFn const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.auth.AWSCredentials")});
    public static final AFn const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.ClientConfiguration")});

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object cls, Object creds, Object conf) {
        Object object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__15), ((IFn)const__2.getRawRoot()).invoke((Object)const__16), ((IFn)const__2.getRawRoot()).invoke(creds))));
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__17), ((IFn)const__2.getRawRoot()).invoke(cls), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(creds, (Object)const__19)), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(conf, (Object)const__21)))));
        Object object3 = cls;
        cls = null;
        Object object4 = creds;
        creds = null;
        Object object5 = conf;
        conf = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__14), object, object2, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__22), ((IFn)const__2.getRawRoot()).invoke(object3), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object4, (Object)const__24)), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object5, (Object)const__26)))))));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return aws$newclient.invokeStatic(object6, object7, object8, object9, object10);
    }

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object cls, Object creds) {
        Object object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), ((IFn)const__2.getRawRoot()).invoke((Object)const__5), ((IFn)const__2.getRawRoot()).invoke(creds))));
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__6), ((IFn)const__2.getRawRoot()).invoke(cls), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(creds, (Object)const__10)))));
        Object object3 = cls;
        cls = null;
        Object object4 = creds;
        creds = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), object, object2, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__11), ((IFn)const__2.getRawRoot()).invoke(object3), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object4, (Object)const__13)))))));
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
        return aws$newclient.invokeStatic(object5, object6, object7, object8);
    }
}

