/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.kv_dynamo_skv.KVDynamoSKV;
import java.io.StringReader;
import java.util.Properties;

public final class kv_dynamo_skv$kv_ddb_skv_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.simple-kv", (String)"get-with-retry");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"sbuf");
    public static final Var const__4 = RT.var((String)"datomic.io", (String)"bbuf->string");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"table");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"table");
    public static final Var const__7 = RT.var((String)"datomic.ddb", (String)"client");
    public static final Keyword const__8 = RT.keyword(null, (String)"region");
    public static final Keyword const__9 = RT.keyword(null, (String)"override-endpoint");
    public static final Keyword const__10 = RT.keyword(null, (String)"maxErrorRetry");
    public static final Object const__11 = 0L;

    public static Object invokeStatic(Object creds, Object prefix, Object skv) {
        Object client2;
        String table;
        Object sbuf;
        Object object = sbuf = ((IFn)const__0.getRawRoot()).invoke(skv, (Object)"config/dynamo.properties");
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"No 'config/dynamo.properties' key found in storage", (Object)"\n", ((IFn)const__2.getRawRoot()).invoke((Object)const__3))));
        }
        Properties G__23549 = new Properties();
        Object object2 = sbuf;
        sbuf = null;
        G__23549.load(new StringReader((String)((IFn)const__4.getRawRoot()).invoke(object2)));
        Properties properties = G__23549;
        G__23549 = null;
        Properties props = properties;
        String string = table = props.getProperty("aws-dynamodb-table");
        if (string == null || string == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"No 'aws-dynamodb-table' entry found in config/dynamo.properties", (Object)"\n", ((IFn)const__2.getRawRoot()).invoke((Object)const__5))));
        }
        String region = props.getProperty("aws-dynamodb-region");
        Properties properties2 = props;
        props = null;
        String override_endpoint = properties2.getProperty("aws-dynamodb-override-endpoint");
        String string2 = table;
        if (string2 == null || string2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", (Object)"No 'aws-dynamodb-region' entry found in config/dynamo.properties", (Object)"\n", ((IFn)const__2.getRawRoot()).invoke((Object)const__6))));
        }
        Object object3 = creds;
        creds = null;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__8;
        String string3 = region;
        region = null;
        objectArray[1] = string3;
        objectArray[2] = const__9;
        String string4 = override_endpoint;
        override_endpoint = null;
        objectArray[3] = string4;
        objectArray[4] = const__10;
        objectArray[5] = const__11;
        Object object4 = client2 = ((IFn)const__7.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        client2 = null;
        String string5 = table;
        table = null;
        Object object5 = skv;
        skv = null;
        Object object6 = prefix;
        prefix = null;
        return new KVDynamoSKV(object4, string5, object5, object6);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return kv_dynamo_skv$kv_ddb_skv_.invokeStatic(object4, object5, object6);
    }
}

