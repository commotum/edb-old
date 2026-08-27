/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aws_monitor$fn__23636
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.aws-monitor", (String)"create-cloudwatch-reporter");
    public static final Keyword const__1 = RT.keyword(null, (String)"name");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__3 = RT.keyword(null, (String)"creds");
    public static final Keyword const__4 = RT.keyword(null, (String)"aws-access-key-id");
    public static final Keyword const__5 = RT.keyword(null, (String)"aws-secret-key");
    public static final Keyword const__6 = RT.keyword(null, (String)"aws-cloudwatch-dimension-value");
    public static final Keyword const__7 = RT.keyword(null, (String)"aws-cloudwatch-region");

    public static Object invokeStatic() {
        IPersistentMap iPersistentMap;
        Object temp__5457__auto__23638;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.cloudwatchName");
        Object object2 = temp__5457__auto__23638 = ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.cloudwatchAccessKeyId");
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__23638;
            temp__5457__auto__23638 = null;
            Object key_id = object3;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__4;
            Object object4 = key_id;
            key_id = null;
            objectArray[1] = object4;
            objectArray[2] = const__5;
            objectArray[3] = ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.cloudwatchSecretKey");
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iFn.invoke((Object)const__1, object, (Object)const__3, iPersistentMap, (Object)const__6, ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.cloudwatchDimension"), (Object)const__7, ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.cloudwatchRegion"));
    }

    public Object invoke() {
        return aws_monitor$fn__23636.invokeStatic();
    }
}

