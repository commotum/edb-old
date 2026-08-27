/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.model.Owner
 *  com.amazonaws.services.s3.model.RestoreStatus
 *  com.amazonaws.services.s3.model.S3ObjectSummary
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.RestoreStatus;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.Date;

public final class s3_api$fn__23196
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"lastModified");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"bucketName");
    public static final Keyword const__6 = RT.keyword(null, (String)"restoreStatus");
    public static final Keyword const__7 = RT.keyword(null, (String)"eTag");
    public static final Keyword const__8 = RT.keyword(null, (String)"storageClass");
    public static final Keyword const__9 = RT.keyword(null, (String)"key");
    public static final Keyword const__10 = RT.keyword(null, (String)"size");
    public static final Keyword const__11 = RT.keyword(null, (String)"owner");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        Owner temp__5457__auto__23213;
        IPersistentVector iPersistentVector2;
        IPersistentVector iPersistentVector3;
        String temp__5457__auto__23209;
        IPersistentVector iPersistentVector4;
        String temp__5457__auto__23207;
        IPersistentVector iPersistentVector5;
        String temp__5457__auto__23205;
        IPersistentVector iPersistentVector6;
        RestoreStatus temp__5457__auto__23203;
        IPersistentVector iPersistentVector7;
        String temp__5457__auto__23201;
        IPersistentVector iPersistentVector8;
        Date temp__5457__auto__23199;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Date date = temp__5457__auto__23199 = ((S3ObjectSummary)o).getLastModified();
        if (date != null && date != Boolean.FALSE) {
            Date v__17285__auto__23198;
            Date date2 = temp__5457__auto__23199;
            temp__5457__auto__23199 = null;
            Date date3 = v__17285__auto__23198 = date2;
            v__17285__auto__23198 = null;
            iPersistentVector8 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date3));
        } else {
            iPersistentVector8 = null;
        }
        String string = temp__5457__auto__23201 = ((S3ObjectSummary)o).getBucketName();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__23200;
            String string2 = temp__5457__auto__23201;
            temp__5457__auto__23201 = null;
            String string3 = v__17285__auto__23200 = string2;
            v__17285__auto__23200 = null;
            iPersistentVector7 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector7 = null;
        }
        RestoreStatus restoreStatus = temp__5457__auto__23203 = ((S3ObjectSummary)o).getRestoreStatus();
        if (restoreStatus != null && restoreStatus != Boolean.FALSE) {
            RestoreStatus v__17285__auto__23202;
            RestoreStatus restoreStatus2 = temp__5457__auto__23203;
            temp__5457__auto__23203 = null;
            RestoreStatus restoreStatus3 = v__17285__auto__23202 = restoreStatus2;
            v__17285__auto__23202 = null;
            iPersistentVector6 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)restoreStatus3));
        } else {
            iPersistentVector6 = null;
        }
        String string4 = temp__5457__auto__23205 = ((S3ObjectSummary)o).getETag();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__23204;
            String string5 = temp__5457__auto__23205;
            temp__5457__auto__23205 = null;
            String string6 = v__17285__auto__23204 = string5;
            v__17285__auto__23204 = null;
            iPersistentVector5 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector5 = null;
        }
        String string7 = temp__5457__auto__23207 = ((S3ObjectSummary)o).getStorageClass();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__23206;
            String string8 = temp__5457__auto__23207;
            temp__5457__auto__23207 = null;
            String string9 = v__17285__auto__23206 = string8;
            v__17285__auto__23206 = null;
            iPersistentVector4 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector4 = null;
        }
        String string10 = temp__5457__auto__23209 = ((S3ObjectSummary)o).getKey();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__23208;
            String string11 = temp__5457__auto__23209;
            temp__5457__auto__23209 = null;
            String string12 = v__17285__auto__23208 = string11;
            v__17285__auto__23208 = null;
            iPersistentVector3 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector3 = null;
        }
        long temp__5457__auto__23211 = ((S3ObjectSummary)o).getSize();
        Number number = Numbers.num((long)temp__5457__auto__23211);
        if (number != null && number != Boolean.FALSE) {
            long v__17285__auto__23210 = temp__5457__auto__23211;
            iPersistentVector2 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)Numbers.num((long)v__17285__auto__23210)));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        Owner owner = temp__5457__auto__23213 = ((S3ObjectSummary)object2).getOwner();
        if (owner != null && owner != Boolean.FALSE) {
            Owner v__17285__auto__23212;
            Owner owner2 = temp__5457__auto__23213;
            temp__5457__auto__23213 = null;
            Owner owner3 = v__17285__auto__23212 = owner2;
            v__17285__auto__23212 = null;
            iPersistentVector = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)owner3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector8, (Object)iPersistentVector7, (Object)iPersistentVector6, (Object)iPersistentVector5, (Object)iPersistentVector4, (Object)iPersistentVector3, (Object)iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__23196.invokeStatic(object2);
    }
}

