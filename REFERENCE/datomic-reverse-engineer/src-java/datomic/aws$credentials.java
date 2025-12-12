/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 *  com.amazonaws.auth.AWSCredentialsProviderChain
 *  com.amazonaws.auth.BasicAWSCredentials
 *  com.amazonaws.auth.DefaultAWSCredentialsProviderChain
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import java.util.Arrays;

public final class aws$credentials
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"aws-access-key-id");
    public static final Keyword const__5 = RT.keyword(null, (String)"aws-secret-key");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__8 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"aws-access-key-id"), Symbol.intern(null, (String)"aws-secret-key")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 14}));
    public static final Keyword const__9 = RT.keyword(null, (String)"default");

    public static Object invokeStatic(Object creds) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(creds);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            Object and__5236__auto__17388;
            Object object4;
            Object object5 = creds;
            creds = null;
            Object map__17386 = object5;
            Object object6 = ((IFn)const__1.getRawRoot()).invoke(map__17386);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object7 = map__17386;
                map__17386 = null;
                object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object7)));
            } else {
                object4 = map__17386;
                map__17386 = null;
            }
            Object map__173862 = object4;
            Object aws_access_key_id = RT.get((Object)map__173862, (Object)const__4);
            Object object8 = map__173862;
            map__173862 = null;
            Object aws_secret_key = RT.get((Object)object8, (Object)const__5);
            Object object9 = and__5236__auto__17388 = aws_access_key_id;
            if (object9 != null && object9 != Boolean.FALSE) {
                object3 = aws_secret_key;
            } else {
                object3 = and__5236__auto__17388;
                and__5236__auto__17388 = null;
            }
            if (object3 == null || object3 == Boolean.FALSE) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__6.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__7.getRawRoot()).invoke(const__8))));
            }
            aws_access_key_id = null;
            aws_secret_key = null;
            object = new BasicAWSCredentials((String)aws_access_key_id, (String)aws_secret_key);
        } else {
            Object object10 = creds;
            if (object10 != null && object10 != Boolean.FALSE) {
                object = creds;
                creds = null;
            } else {
                Keyword keyword = const__9;
                object = keyword != null && keyword != Boolean.FALSE ? ((AWSCredentialsProviderChain)new DefaultAWSCredentialsProviderChain()).getCredentials() : null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws$credentials.invokeStatic(object2);
    }
}

