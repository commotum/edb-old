/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.aws_monitor$create_cloudwatch_reporter$fn__23632;

public final class aws_monitor$create_cloudwatch_reporter
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"name");
    public static final Keyword const__4 = RT.keyword(null, (String)"creds");
    public static final Keyword const__5 = RT.keyword(null, (String)"aws-cloudwatch-dimension-value");
    public static final Keyword const__6 = RT.keyword(null, (String)"aws-cloudwatch-region");
    public static final Var const__7 = RT.var((String)"datomic.cloudwatch", (String)"client");
    public static final Keyword const__8 = RT.keyword(null, (String)"region");

    public static Object invokeStatic(ISeq p__23630) {
        aws_monitor$create_cloudwatch_reporter$fn__23632 aws_monitor$create_cloudwatch_reporter$fn__23632;
        Object object;
        Object and__5236__auto__23635;
        ISeq iSeq;
        ISeq iSeq2 = p__23630;
        p__23630 = null;
        ISeq map__23631 = iSeq2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__23631);
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq iSeq3 = map__23631;
            map__23631 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__23631;
            map__23631 = null;
        }
        ISeq map__236312 = iSeq;
        Object name = RT.get((Object)map__236312, (Object)const__3);
        Object creds = RT.get((Object)map__236312, (Object)const__4);
        Object aws_cloudwatch_dimension_value = RT.get((Object)map__236312, (Object)const__5);
        ISeq iSeq4 = map__236312;
        map__236312 = null;
        Object aws_cloudwatch_region = RT.get((Object)iSeq4, (Object)const__6);
        Object object3 = and__5236__auto__23635 = aws_cloudwatch_dimension_value;
        if (object3 != null && object3 != Boolean.FALSE) {
            object = aws_cloudwatch_region;
        } else {
            object = and__5236__auto__23635;
            and__5236__auto__23635 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object4 = creds;
            creds = null;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__8;
            Object object5 = aws_cloudwatch_region;
            aws_cloudwatch_region = null;
            objectArray[1] = object5;
            Object client2 = ((IFn)const__7.getRawRoot()).invoke(object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
            aws_cloudwatch_dimension_value = null;
            name = null;
            client2 = null;
            aws_monitor$create_cloudwatch_reporter$fn__23632 = new aws_monitor$create_cloudwatch_reporter$fn__23632(aws_cloudwatch_dimension_value, name, client2);
        } else {
            aws_monitor$create_cloudwatch_reporter$fn__23632 = null;
        }
        return aws_monitor$create_cloudwatch_reporter$fn__23632;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return aws_monitor$create_cloudwatch_reporter.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

