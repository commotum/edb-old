/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.log.ddb;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class Log$query__20644
extends AFunction {
    Object table;
    Object p;
    Object client;
    public static final Var const__0 = RT.var((String)"cognitect.aws.client.api", (String)"invoke-async");
    public static final Var const__1 = RT.var((String)"datomic.core2.aws.ddb", (String)"query-range-request");
    public static final Keyword const__2 = RT.keyword(null, (String)"table");
    public static final Keyword const__3 = RT.keyword(null, (String)"p");
    public static final Keyword const__4 = RT.keyword(null, (String)"r");
    public static final Keyword const__5 = RT.keyword(null, (String)"forward");
    public static final Keyword const__6 = RT.keyword(null, (String)"attrs");
    public static final Keyword const__7 = RT.keyword(null, (String)"limit");

    public Log$query__20644(Object object, Object object2, Object object3) {
        this.table = object;
        this.p = object2;
        this.client = object3;
    }

    public Object invoke(Object forward_QMARK_, Object r, Object limit2) {
        Object[] objectArray = new Object[12];
        objectArray[0] = const__2;
        objectArray[1] = this_.table;
        objectArray[2] = const__3;
        objectArray[3] = const__3;
        objectArray[4] = const__4;
        objectArray[5] = const__4;
        objectArray[6] = const__5;
        Object object = forward_QMARK_;
        forward_QMARK_ = null;
        objectArray[7] = object;
        objectArray[8] = const__6;
        Object[] objectArray2 = new Object[4];
        objectArray2[0] = const__3;
        objectArray2[1] = this_.p;
        objectArray2[2] = const__4;
        Object object2 = r;
        r = null;
        objectArray2[3] = object2;
        objectArray[9] = RT.mapUniqueKeys((Object[])objectArray2);
        objectArray[10] = const__7;
        Object object3 = limit2;
        limit2 = null;
        objectArray[11] = object3;
        Log$query__20644 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.client, ((IFn)const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
    }
}

