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
 *  com.amazonaws.services.s3.model.ObjectListing
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.ObjectListing;
import java.util.List;

public final class s3_api$fn__23096
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"prefix");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"marker");
    public static final Keyword const__6 = RT.keyword(null, (String)"requesterCharged");
    public static final Keyword const__7 = RT.keyword(null, (String)"bucketName");
    public static final Keyword const__8 = RT.keyword(null, (String)"objectSummaries");
    public static final Keyword const__9 = RT.keyword(null, (String)"commonPrefixes");
    public static final Keyword const__10 = RT.keyword(null, (String)"truncated");
    public static final Keyword const__11 = RT.keyword(null, (String)"delimiter");
    public static final Keyword const__12 = RT.keyword(null, (String)"maxKeys");
    public static final Keyword const__13 = RT.keyword(null, (String)"encodingType");
    public static final Keyword const__14 = RT.keyword(null, (String)"nextMarker");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        String temp__5457__auto__23119;
        IPersistentVector iPersistentVector2;
        String temp__5457__auto__23117;
        IPersistentVector iPersistentVector3;
        IPersistentVector iPersistentVector4;
        String temp__5457__auto__23113;
        IPersistentVector iPersistentVector5;
        IPersistentVector iPersistentVector6;
        List temp__5457__auto__23109;
        IPersistentVector iPersistentVector7;
        List temp__5457__auto__23107;
        IPersistentVector iPersistentVector8;
        String temp__5457__auto__23105;
        IPersistentVector iPersistentVector9;
        IPersistentVector iPersistentVector10;
        String temp__5457__auto__23101;
        IPersistentVector iPersistentVector11;
        String temp__5457__auto__23099;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        String string = temp__5457__auto__23099 = ((ObjectListing)o).getPrefix();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__23098;
            String string2 = temp__5457__auto__23099;
            temp__5457__auto__23099 = null;
            String string3 = v__17285__auto__23098 = string2;
            v__17285__auto__23098 = null;
            iPersistentVector11 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector11 = null;
        }
        String string4 = temp__5457__auto__23101 = ((ObjectListing)o).getMarker();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__23100;
            String string5 = temp__5457__auto__23101;
            temp__5457__auto__23101 = null;
            String string6 = v__17285__auto__23100 = string5;
            v__17285__auto__23100 = null;
            iPersistentVector10 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector10 = null;
        }
        boolean temp__5457__auto__23103 = ((ObjectListing)o).isRequesterCharged();
        if (temp__5457__auto__23103) {
            boolean v__17285__auto__23102 = temp__5457__auto__23103;
            iPersistentVector9 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)(v__17285__auto__23102 ? Boolean.TRUE : Boolean.FALSE)));
        } else {
            iPersistentVector9 = null;
        }
        String string7 = temp__5457__auto__23105 = ((ObjectListing)o).getBucketName();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__23104;
            String string8 = temp__5457__auto__23105;
            temp__5457__auto__23105 = null;
            String string9 = v__17285__auto__23104 = string8;
            v__17285__auto__23104 = null;
            iPersistentVector8 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector8 = null;
        }
        List list = temp__5457__auto__23107 = ((ObjectListing)o).getObjectSummaries();
        if (list != null && list != Boolean.FALSE) {
            List v__17285__auto__23106;
            List list2 = temp__5457__auto__23107;
            temp__5457__auto__23107 = null;
            List list3 = v__17285__auto__23106 = list2;
            v__17285__auto__23106 = null;
            iPersistentVector7 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list3));
        } else {
            iPersistentVector7 = null;
        }
        List list4 = temp__5457__auto__23109 = ((ObjectListing)o).getCommonPrefixes();
        if (list4 != null && list4 != Boolean.FALSE) {
            List v__17285__auto__23108;
            List list5 = temp__5457__auto__23109;
            temp__5457__auto__23109 = null;
            List list6 = v__17285__auto__23108 = list5;
            v__17285__auto__23108 = null;
            iPersistentVector6 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list6));
        } else {
            iPersistentVector6 = null;
        }
        boolean temp__5457__auto__23111 = ((ObjectListing)o).isTruncated();
        if (temp__5457__auto__23111) {
            boolean v__17285__auto__23110 = temp__5457__auto__23111;
            iPersistentVector5 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)(v__17285__auto__23110 ? Boolean.TRUE : Boolean.FALSE)));
        } else {
            iPersistentVector5 = null;
        }
        String string10 = temp__5457__auto__23113 = ((ObjectListing)o).getDelimiter();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__23112;
            String string11 = temp__5457__auto__23113;
            temp__5457__auto__23113 = null;
            String string12 = v__17285__auto__23112 = string11;
            v__17285__auto__23112 = null;
            iPersistentVector4 = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector4 = null;
        }
        int temp__5457__auto__23115 = ((ObjectListing)o).getMaxKeys();
        Integer n = temp__5457__auto__23115;
        if (n != null && n != Boolean.FALSE) {
            int v__17285__auto__23114 = temp__5457__auto__23115;
            iPersistentVector3 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)v__17285__auto__23114));
        } else {
            iPersistentVector3 = null;
        }
        String string13 = temp__5457__auto__23117 = ((ObjectListing)o).getEncodingType();
        if (string13 != null && string13 != Boolean.FALSE) {
            String v__17285__auto__23116;
            String string14 = temp__5457__auto__23117;
            temp__5457__auto__23117 = null;
            String string15 = v__17285__auto__23116 = string14;
            v__17285__auto__23116 = null;
            iPersistentVector2 = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string15));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        String string16 = temp__5457__auto__23119 = ((ObjectListing)object2).getNextMarker();
        if (string16 != null && string16 != Boolean.FALSE) {
            String v__17285__auto__23118;
            String string17 = temp__5457__auto__23119;
            temp__5457__auto__23119 = null;
            String string18 = v__17285__auto__23118 = string17;
            v__17285__auto__23118 = null;
            iPersistentVector = Tuple.create((Object)const__14, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string18));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector11, (Object)iPersistentVector10, (Object)iPersistentVector9, (Object)iPersistentVector8, (Object)iPersistentVector7, (Object)iPersistentVector6, (Object)iPersistentVector5, (Object)iPersistentVector4, (Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__23096.invokeStatic(object2);
    }
}

