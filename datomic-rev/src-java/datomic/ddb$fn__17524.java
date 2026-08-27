/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.AmazonWebServiceResult
 *  com.amazonaws.ResponseMetadata
 *  com.amazonaws.http.SdkHttpMetadata
 *  com.amazonaws.services.dynamodbv2.model.ListTablesResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import java.util.List;

public final class ddb$fn__17524
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"tableNames");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"lastEvaluatedTableName");
    public static final Keyword const__6 = RT.keyword(null, (String)"sdkResponseMetadata");
    public static final Keyword const__7 = RT.keyword(null, (String)"sdkHttpMetadata");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        SdkHttpMetadata temp__5457__auto__17533;
        IPersistentVector iPersistentVector2;
        ResponseMetadata temp__5457__auto__17531;
        IPersistentVector iPersistentVector3;
        String temp__5457__auto__17529;
        IPersistentVector iPersistentVector4;
        List temp__5457__auto__17527;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        List list = temp__5457__auto__17527 = ((ListTablesResult)o).getTableNames();
        if (list != null && list != Boolean.FALSE) {
            List v__17285__auto__17526;
            List list2 = temp__5457__auto__17527;
            temp__5457__auto__17527 = null;
            List list3 = v__17285__auto__17526 = list2;
            v__17285__auto__17526 = null;
            iPersistentVector4 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list3));
        } else {
            iPersistentVector4 = null;
        }
        String string = temp__5457__auto__17529 = ((ListTablesResult)o).getLastEvaluatedTableName();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__17528;
            String string2 = temp__5457__auto__17529;
            temp__5457__auto__17529 = null;
            String string3 = v__17285__auto__17528 = string2;
            v__17285__auto__17528 = null;
            iPersistentVector3 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector3 = null;
        }
        ResponseMetadata responseMetadata = temp__5457__auto__17531 = ((AmazonWebServiceResult)o).getSdkResponseMetadata();
        if (responseMetadata != null && responseMetadata != Boolean.FALSE) {
            ResponseMetadata v__17285__auto__17530;
            ResponseMetadata responseMetadata2 = temp__5457__auto__17531;
            temp__5457__auto__17531 = null;
            ResponseMetadata responseMetadata3 = v__17285__auto__17530 = responseMetadata2;
            v__17285__auto__17530 = null;
            iPersistentVector2 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)responseMetadata3));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        SdkHttpMetadata sdkHttpMetadata = temp__5457__auto__17533 = ((AmazonWebServiceResult)object2).getSdkHttpMetadata();
        if (sdkHttpMetadata != null && sdkHttpMetadata != Boolean.FALSE) {
            SdkHttpMetadata v__17285__auto__17532;
            SdkHttpMetadata sdkHttpMetadata2 = temp__5457__auto__17533;
            temp__5457__auto__17533 = null;
            SdkHttpMetadata sdkHttpMetadata3 = v__17285__auto__17532 = sdkHttpMetadata2;
            v__17285__auto__17532 = null;
            iPersistentVector = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)sdkHttpMetadata3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector4, (Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17524.invokeStatic(object2);
    }
}

