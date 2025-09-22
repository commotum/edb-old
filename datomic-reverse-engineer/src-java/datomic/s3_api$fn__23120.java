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
 *  com.amazonaws.services.s3.model.ObjectMetadata
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
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.util.Date;
import java.util.Map;

public final class s3_api$fn__23120
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"expirationTime");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"contentLength");
    public static final Keyword const__6 = RT.keyword(null, (String)"lastModified");
    public static final Keyword const__7 = RT.keyword(null, (String)"contentType");
    public static final Keyword const__8 = RT.keyword(null, (String)"contentEncoding");
    public static final Keyword const__9 = RT.keyword(null, (String)"requesterCharged");
    public static final Keyword const__10 = RT.keyword(null, (String)"instanceLength");
    public static final Keyword const__11 = RT.keyword(null, (String)"restoreExpirationTime");
    public static final Keyword const__12 = RT.keyword(null, (String)"ongoingRestore");
    public static final Keyword const__13 = RT.keyword(null, (String)"archiveStatus");
    public static final Keyword const__14 = RT.keyword(null, (String)"sSEAwsKmsKeyId");
    public static final Keyword const__15 = RT.keyword(null, (String)"sSEAwsKmsEncryptionContext");
    public static final Keyword const__16 = RT.keyword(null, (String)"partCount");
    public static final Keyword const__17 = RT.keyword(null, (String)"versionId");
    public static final Keyword const__18 = RT.keyword(null, (String)"eTag");
    public static final Keyword const__19 = RT.keyword(null, (String)"storageClass");
    public static final Keyword const__20 = RT.keyword(null, (String)"bucketKeyEnabled");
    public static final Keyword const__21 = RT.keyword(null, (String)"objectLockMode");
    public static final Keyword const__22 = RT.keyword(null, (String)"objectLockRetainUntilDate");
    public static final Keyword const__23 = RT.keyword(null, (String)"objectLockLegalHoldStatus");
    public static final Keyword const__24 = RT.keyword(null, (String)"contentMD5");
    public static final Keyword const__25 = RT.keyword(null, (String)"sSEAlgorithm");
    public static final Keyword const__26 = RT.keyword(null, (String)"sSECustomerAlgorithm");
    public static final Keyword const__27 = RT.keyword(null, (String)"sSECustomerKeyMd5");
    public static final Keyword const__28 = RT.keyword(null, (String)"expirationTimeRuleId");
    public static final Keyword const__29 = RT.keyword(null, (String)"cacheControl");
    public static final Keyword const__30 = RT.keyword(null, (String)"contentDisposition");
    public static final Keyword const__31 = RT.keyword(null, (String)"contentLanguage");
    public static final Keyword const__32 = RT.keyword(null, (String)"contentRange");
    public static final Keyword const__33 = RT.keyword(null, (String)"replicationStatus");
    public static final Keyword const__34 = RT.keyword(null, (String)"serverSideEncryption");
    public static final Keyword const__35 = RT.keyword(null, (String)"rawMetadata");
    public static final Keyword const__36 = RT.keyword(null, (String)"httpExpiresDate");
    public static final Keyword const__37 = RT.keyword(null, (String)"userMetadata");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        Map temp__5457__auto__23189;
        IPersistentVector iPersistentVector2;
        Date temp__5457__auto__23187;
        IPersistentVector iPersistentVector3;
        Map temp__5457__auto__23185;
        IPersistentVector iPersistentVector4;
        String temp__5457__auto__23183;
        IPersistentVector iPersistentVector5;
        String temp__5457__auto__23181;
        IPersistentVector iPersistentVector6;
        Long[] temp__5457__auto__23179;
        Object object;
        String temp__5457__auto__23177;
        IPersistentVector iPersistentVector7;
        String temp__5457__auto__23175;
        IPersistentVector iPersistentVector8;
        String temp__5457__auto__23173;
        IPersistentVector iPersistentVector9;
        String temp__5457__auto__23171;
        IPersistentVector iPersistentVector10;
        String temp__5457__auto__23169;
        IPersistentVector iPersistentVector11;
        String temp__5457__auto__23167;
        IPersistentVector iPersistentVector12;
        String temp__5457__auto__23165;
        IPersistentVector iPersistentVector13;
        String temp__5457__auto__23163;
        IPersistentVector iPersistentVector14;
        String temp__5457__auto__23161;
        IPersistentVector iPersistentVector15;
        Date temp__5457__auto__23159;
        IPersistentVector iPersistentVector16;
        String temp__5457__auto__23157;
        IPersistentVector iPersistentVector17;
        Boolean temp__5457__auto__23155;
        IPersistentVector iPersistentVector18;
        String temp__5457__auto__23153;
        IPersistentVector iPersistentVector19;
        String temp__5457__auto__23151;
        IPersistentVector iPersistentVector20;
        String temp__5457__auto__23149;
        IPersistentVector iPersistentVector21;
        Integer temp__5457__auto__23147;
        IPersistentVector iPersistentVector22;
        String temp__5457__auto__23145;
        IPersistentVector iPersistentVector23;
        String temp__5457__auto__23143;
        IPersistentVector iPersistentVector24;
        String temp__5457__auto__23141;
        IPersistentVector iPersistentVector25;
        Boolean temp__5457__auto__23139;
        IPersistentVector iPersistentVector26;
        Date temp__5457__auto__23137;
        IPersistentVector iPersistentVector27;
        IPersistentVector iPersistentVector28;
        IPersistentVector iPersistentVector29;
        String temp__5457__auto__23131;
        IPersistentVector iPersistentVector30;
        String temp__5457__auto__23129;
        IPersistentVector iPersistentVector31;
        Date temp__5457__auto__23127;
        IPersistentVector iPersistentVector32;
        IPersistentVector iPersistentVector33;
        Date temp__5457__auto__23123;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Date date = temp__5457__auto__23123 = ((ObjectMetadata)o).getExpirationTime();
        if (date != null && date != Boolean.FALSE) {
            Date v__17285__auto__23122;
            Date date2 = temp__5457__auto__23123;
            temp__5457__auto__23123 = null;
            Date date3 = v__17285__auto__23122 = date2;
            v__17285__auto__23122 = null;
            iPersistentVector33 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date3));
        } else {
            iPersistentVector33 = null;
        }
        long temp__5457__auto__23125 = ((ObjectMetadata)o).getContentLength();
        Number number = Numbers.num((long)temp__5457__auto__23125);
        if (number != null && number != Boolean.FALSE) {
            long v__17285__auto__23124 = temp__5457__auto__23125;
            iPersistentVector32 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)Numbers.num((long)v__17285__auto__23124)));
        } else {
            iPersistentVector32 = null;
        }
        Date date4 = temp__5457__auto__23127 = ((ObjectMetadata)o).getLastModified();
        if (date4 != null && date4 != Boolean.FALSE) {
            Date v__17285__auto__23126;
            Date date5 = temp__5457__auto__23127;
            temp__5457__auto__23127 = null;
            Date date6 = v__17285__auto__23126 = date5;
            v__17285__auto__23126 = null;
            iPersistentVector31 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date6));
        } else {
            iPersistentVector31 = null;
        }
        String string = temp__5457__auto__23129 = ((ObjectMetadata)o).getContentType();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__23128;
            String string2 = temp__5457__auto__23129;
            temp__5457__auto__23129 = null;
            String string3 = v__17285__auto__23128 = string2;
            v__17285__auto__23128 = null;
            iPersistentVector30 = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector30 = null;
        }
        String string4 = temp__5457__auto__23131 = ((ObjectMetadata)o).getContentEncoding();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__23130;
            String string5 = temp__5457__auto__23131;
            temp__5457__auto__23131 = null;
            String string6 = v__17285__auto__23130 = string5;
            v__17285__auto__23130 = null;
            iPersistentVector29 = Tuple.create((Object)const__8, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector29 = null;
        }
        boolean temp__5457__auto__23133 = ((ObjectMetadata)o).isRequesterCharged();
        if (temp__5457__auto__23133) {
            boolean v__17285__auto__23132 = temp__5457__auto__23133;
            iPersistentVector28 = Tuple.create((Object)const__9, (Object)((IFn)const__4.getRawRoot()).invoke((Object)(v__17285__auto__23132 ? Boolean.TRUE : Boolean.FALSE)));
        } else {
            iPersistentVector28 = null;
        }
        long temp__5457__auto__23135 = ((ObjectMetadata)o).getInstanceLength();
        Number number2 = Numbers.num((long)temp__5457__auto__23135);
        if (number2 != null && number2 != Boolean.FALSE) {
            long v__17285__auto__23134 = temp__5457__auto__23135;
            iPersistentVector27 = Tuple.create((Object)const__10, (Object)((IFn)const__4.getRawRoot()).invoke((Object)Numbers.num((long)v__17285__auto__23134)));
        } else {
            iPersistentVector27 = null;
        }
        Date date7 = temp__5457__auto__23137 = ((ObjectMetadata)o).getRestoreExpirationTime();
        if (date7 != null && date7 != Boolean.FALSE) {
            Date v__17285__auto__23136;
            Date date8 = temp__5457__auto__23137;
            temp__5457__auto__23137 = null;
            Date date9 = v__17285__auto__23136 = date8;
            v__17285__auto__23136 = null;
            iPersistentVector26 = Tuple.create((Object)const__11, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date9));
        } else {
            iPersistentVector26 = null;
        }
        Boolean bl = temp__5457__auto__23139 = ((ObjectMetadata)o).getOngoingRestore();
        if (bl != null && bl != Boolean.FALSE) {
            Boolean v__17285__auto__23138;
            Boolean bl2 = temp__5457__auto__23139;
            temp__5457__auto__23139 = null;
            Boolean bl3 = v__17285__auto__23138 = bl2;
            v__17285__auto__23138 = null;
            iPersistentVector25 = Tuple.create((Object)const__12, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl3));
        } else {
            iPersistentVector25 = null;
        }
        String string7 = temp__5457__auto__23141 = ((ObjectMetadata)o).getArchiveStatus();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__23140;
            String string8 = temp__5457__auto__23141;
            temp__5457__auto__23141 = null;
            String string9 = v__17285__auto__23140 = string8;
            v__17285__auto__23140 = null;
            iPersistentVector24 = Tuple.create((Object)const__13, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector24 = null;
        }
        String string10 = temp__5457__auto__23143 = ((ObjectMetadata)o).getSSEAwsKmsKeyId();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__23142;
            String string11 = temp__5457__auto__23143;
            temp__5457__auto__23143 = null;
            String string12 = v__17285__auto__23142 = string11;
            v__17285__auto__23142 = null;
            iPersistentVector23 = Tuple.create((Object)const__14, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector23 = null;
        }
        String string13 = temp__5457__auto__23145 = ((ObjectMetadata)o).getSSEAwsKmsEncryptionContext();
        if (string13 != null && string13 != Boolean.FALSE) {
            String v__17285__auto__23144;
            String string14 = temp__5457__auto__23145;
            temp__5457__auto__23145 = null;
            String string15 = v__17285__auto__23144 = string14;
            v__17285__auto__23144 = null;
            iPersistentVector22 = Tuple.create((Object)const__15, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string15));
        } else {
            iPersistentVector22 = null;
        }
        Integer n = temp__5457__auto__23147 = ((ObjectMetadata)o).getPartCount();
        if (n != null && n != Boolean.FALSE) {
            Integer v__17285__auto__23146;
            Integer n2 = temp__5457__auto__23147;
            temp__5457__auto__23147 = null;
            Integer n3 = v__17285__auto__23146 = n2;
            v__17285__auto__23146 = null;
            iPersistentVector21 = Tuple.create((Object)const__16, (Object)((IFn)const__4.getRawRoot()).invoke((Object)n3));
        } else {
            iPersistentVector21 = null;
        }
        String string16 = temp__5457__auto__23149 = ((ObjectMetadata)o).getVersionId();
        if (string16 != null && string16 != Boolean.FALSE) {
            String v__17285__auto__23148;
            String string17 = temp__5457__auto__23149;
            temp__5457__auto__23149 = null;
            String string18 = v__17285__auto__23148 = string17;
            v__17285__auto__23148 = null;
            iPersistentVector20 = Tuple.create((Object)const__17, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string18));
        } else {
            iPersistentVector20 = null;
        }
        String string19 = temp__5457__auto__23151 = ((ObjectMetadata)o).getETag();
        if (string19 != null && string19 != Boolean.FALSE) {
            String v__17285__auto__23150;
            String string20 = temp__5457__auto__23151;
            temp__5457__auto__23151 = null;
            String string21 = v__17285__auto__23150 = string20;
            v__17285__auto__23150 = null;
            iPersistentVector19 = Tuple.create((Object)const__18, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string21));
        } else {
            iPersistentVector19 = null;
        }
        String string22 = temp__5457__auto__23153 = ((ObjectMetadata)o).getStorageClass();
        if (string22 != null && string22 != Boolean.FALSE) {
            String v__17285__auto__23152;
            String string23 = temp__5457__auto__23153;
            temp__5457__auto__23153 = null;
            String string24 = v__17285__auto__23152 = string23;
            v__17285__auto__23152 = null;
            iPersistentVector18 = Tuple.create((Object)const__19, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string24));
        } else {
            iPersistentVector18 = null;
        }
        Boolean bl4 = temp__5457__auto__23155 = ((ObjectMetadata)o).getBucketKeyEnabled();
        if (bl4 != null && bl4 != Boolean.FALSE) {
            Boolean v__17285__auto__23154;
            Boolean bl5 = temp__5457__auto__23155;
            temp__5457__auto__23155 = null;
            Boolean bl6 = v__17285__auto__23154 = bl5;
            v__17285__auto__23154 = null;
            iPersistentVector17 = Tuple.create((Object)const__20, (Object)((IFn)const__4.getRawRoot()).invoke((Object)bl6));
        } else {
            iPersistentVector17 = null;
        }
        String string25 = temp__5457__auto__23157 = ((ObjectMetadata)o).getObjectLockMode();
        if (string25 != null && string25 != Boolean.FALSE) {
            String v__17285__auto__23156;
            String string26 = temp__5457__auto__23157;
            temp__5457__auto__23157 = null;
            String string27 = v__17285__auto__23156 = string26;
            v__17285__auto__23156 = null;
            iPersistentVector16 = Tuple.create((Object)const__21, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string27));
        } else {
            iPersistentVector16 = null;
        }
        Date date10 = temp__5457__auto__23159 = ((ObjectMetadata)o).getObjectLockRetainUntilDate();
        if (date10 != null && date10 != Boolean.FALSE) {
            Date v__17285__auto__23158;
            Date date11 = temp__5457__auto__23159;
            temp__5457__auto__23159 = null;
            Date date12 = v__17285__auto__23158 = date11;
            v__17285__auto__23158 = null;
            iPersistentVector15 = Tuple.create((Object)const__22, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date12));
        } else {
            iPersistentVector15 = null;
        }
        String string28 = temp__5457__auto__23161 = ((ObjectMetadata)o).getObjectLockLegalHoldStatus();
        if (string28 != null && string28 != Boolean.FALSE) {
            String v__17285__auto__23160;
            String string29 = temp__5457__auto__23161;
            temp__5457__auto__23161 = null;
            String string30 = v__17285__auto__23160 = string29;
            v__17285__auto__23160 = null;
            iPersistentVector14 = Tuple.create((Object)const__23, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string30));
        } else {
            iPersistentVector14 = null;
        }
        Object[] objectArray = new Object[14];
        String string31 = temp__5457__auto__23163 = ((ObjectMetadata)o).getContentMD5();
        if (string31 != null && string31 != Boolean.FALSE) {
            String v__17285__auto__23162;
            String string32 = temp__5457__auto__23163;
            temp__5457__auto__23163 = null;
            String string33 = v__17285__auto__23162 = string32;
            v__17285__auto__23162 = null;
            iPersistentVector13 = Tuple.create((Object)const__24, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string33));
        } else {
            iPersistentVector13 = null;
        }
        objectArray[0] = iPersistentVector13;
        String string34 = temp__5457__auto__23165 = ((ObjectMetadata)o).getSSEAlgorithm();
        if (string34 != null && string34 != Boolean.FALSE) {
            String v__17285__auto__23164;
            String string35 = temp__5457__auto__23165;
            temp__5457__auto__23165 = null;
            String string36 = v__17285__auto__23164 = string35;
            v__17285__auto__23164 = null;
            iPersistentVector12 = Tuple.create((Object)const__25, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string36));
        } else {
            iPersistentVector12 = null;
        }
        objectArray[1] = iPersistentVector12;
        String string37 = temp__5457__auto__23167 = ((ObjectMetadata)o).getSSECustomerAlgorithm();
        if (string37 != null && string37 != Boolean.FALSE) {
            String v__17285__auto__23166;
            String string38 = temp__5457__auto__23167;
            temp__5457__auto__23167 = null;
            String string39 = v__17285__auto__23166 = string38;
            v__17285__auto__23166 = null;
            iPersistentVector11 = Tuple.create((Object)const__26, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string39));
        } else {
            iPersistentVector11 = null;
        }
        objectArray[2] = iPersistentVector11;
        String string40 = temp__5457__auto__23169 = ((ObjectMetadata)o).getSSECustomerKeyMd5();
        if (string40 != null && string40 != Boolean.FALSE) {
            String v__17285__auto__23168;
            String string41 = temp__5457__auto__23169;
            temp__5457__auto__23169 = null;
            String string42 = v__17285__auto__23168 = string41;
            v__17285__auto__23168 = null;
            iPersistentVector10 = Tuple.create((Object)const__27, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string42));
        } else {
            iPersistentVector10 = null;
        }
        objectArray[3] = iPersistentVector10;
        String string43 = temp__5457__auto__23171 = ((ObjectMetadata)o).getExpirationTimeRuleId();
        if (string43 != null && string43 != Boolean.FALSE) {
            String v__17285__auto__23170;
            String string44 = temp__5457__auto__23171;
            temp__5457__auto__23171 = null;
            String string45 = v__17285__auto__23170 = string44;
            v__17285__auto__23170 = null;
            iPersistentVector9 = Tuple.create((Object)const__28, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string45));
        } else {
            iPersistentVector9 = null;
        }
        objectArray[4] = iPersistentVector9;
        String string46 = temp__5457__auto__23173 = ((ObjectMetadata)o).getCacheControl();
        if (string46 != null && string46 != Boolean.FALSE) {
            String v__17285__auto__23172;
            String string47 = temp__5457__auto__23173;
            temp__5457__auto__23173 = null;
            String string48 = v__17285__auto__23172 = string47;
            v__17285__auto__23172 = null;
            iPersistentVector8 = Tuple.create((Object)const__29, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string48));
        } else {
            iPersistentVector8 = null;
        }
        objectArray[5] = iPersistentVector8;
        String string49 = temp__5457__auto__23175 = ((ObjectMetadata)o).getContentDisposition();
        if (string49 != null && string49 != Boolean.FALSE) {
            String v__17285__auto__23174;
            String string50 = temp__5457__auto__23175;
            temp__5457__auto__23175 = null;
            String string51 = v__17285__auto__23174 = string50;
            v__17285__auto__23174 = null;
            iPersistentVector7 = Tuple.create((Object)const__30, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string51));
        } else {
            iPersistentVector7 = null;
        }
        objectArray[6] = iPersistentVector7;
        String string52 = temp__5457__auto__23177 = ((ObjectMetadata)o).getContentLanguage();
        if (string52 != null && string52 != Boolean.FALSE) {
            String v__17285__auto__23176;
            String string53 = temp__5457__auto__23177;
            temp__5457__auto__23177 = null;
            String string54 = v__17285__auto__23176 = string53;
            v__17285__auto__23176 = null;
            object = Tuple.create((Object)const__31, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string54));
        } else {
            object = objectArray[7] = null;
        }
        if (temp__5457__auto__23179 != null && (temp__5457__auto__23179 = ((ObjectMetadata)o).getContentRange()) != Boolean.FALSE) {
            Long[] v__17285__auto__23178;
            Long[] longArray = temp__5457__auto__23179;
            temp__5457__auto__23179 = null;
            Long[] longArray2 = v__17285__auto__23178 = longArray;
            v__17285__auto__23178 = null;
            iPersistentVector6 = Tuple.create((Object)const__32, (Object)((IFn)const__4.getRawRoot()).invoke((Object)longArray2));
        } else {
            iPersistentVector6 = null;
        }
        objectArray[8] = iPersistentVector6;
        String string55 = temp__5457__auto__23181 = ((ObjectMetadata)o).getReplicationStatus();
        if (string55 != null && string55 != Boolean.FALSE) {
            String v__17285__auto__23180;
            String string56 = temp__5457__auto__23181;
            temp__5457__auto__23181 = null;
            String string57 = v__17285__auto__23180 = string56;
            v__17285__auto__23180 = null;
            iPersistentVector5 = Tuple.create((Object)const__33, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string57));
        } else {
            iPersistentVector5 = null;
        }
        objectArray[9] = iPersistentVector5;
        String string58 = temp__5457__auto__23183 = ((ObjectMetadata)o).getServerSideEncryption();
        if (string58 != null && string58 != Boolean.FALSE) {
            String v__17285__auto__23182;
            String string59 = temp__5457__auto__23183;
            temp__5457__auto__23183 = null;
            String string60 = v__17285__auto__23182 = string59;
            v__17285__auto__23182 = null;
            iPersistentVector4 = Tuple.create((Object)const__34, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string60));
        } else {
            iPersistentVector4 = null;
        }
        objectArray[10] = iPersistentVector4;
        Map map2 = temp__5457__auto__23185 = ((ObjectMetadata)o).getRawMetadata();
        if (map2 != null && map2 != Boolean.FALSE) {
            Map v__17285__auto__23184;
            Map map3 = temp__5457__auto__23185;
            temp__5457__auto__23185 = null;
            Map map4 = v__17285__auto__23184 = map3;
            v__17285__auto__23184 = null;
            iPersistentVector3 = Tuple.create((Object)const__35, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map4));
        } else {
            iPersistentVector3 = null;
        }
        objectArray[11] = iPersistentVector3;
        Date date13 = temp__5457__auto__23187 = ((ObjectMetadata)o).getHttpExpiresDate();
        if (date13 != null && date13 != Boolean.FALSE) {
            Date v__17285__auto__23186;
            Date date14 = temp__5457__auto__23187;
            temp__5457__auto__23187 = null;
            Date date15 = v__17285__auto__23186 = date14;
            v__17285__auto__23186 = null;
            iPersistentVector2 = Tuple.create((Object)const__36, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date15));
        } else {
            iPersistentVector2 = null;
        }
        objectArray[12] = iPersistentVector2;
        Object object3 = o;
        o = null;
        Map map5 = temp__5457__auto__23189 = ((ObjectMetadata)object3).getUserMetadata();
        if (map5 != null && map5 != Boolean.FALSE) {
            Map v__17285__auto__23188;
            Map map6 = temp__5457__auto__23189;
            temp__5457__auto__23189 = null;
            Map map7 = v__17285__auto__23188 = map6;
            v__17285__auto__23188 = null;
            iPersistentVector = Tuple.create((Object)const__37, (Object)((IFn)const__4.getRawRoot()).invoke((Object)map7));
        } else {
            iPersistentVector = null;
        }
        objectArray[13] = iPersistentVector;
        return iFn.invoke(object2, iFn2.invoke((Object)iPersistentVector33, (Object)iPersistentVector32, (Object)iPersistentVector31, (Object)iPersistentVector30, (Object)iPersistentVector29, (Object)iPersistentVector28, (Object)iPersistentVector27, (Object)iPersistentVector26, (Object)iPersistentVector25, (Object)iPersistentVector24, (Object)iPersistentVector23, (Object)iPersistentVector22, (Object)iPersistentVector21, (Object)iPersistentVector20, (Object)iPersistentVector19, (Object)iPersistentVector18, (Object)iPersistentVector17, (Object)iPersistentVector16, (Object)iPersistentVector15, (Object)iPersistentVector14, objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__23120.invokeStatic(object2);
    }
}

