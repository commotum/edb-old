/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  com.amazonaws.AmazonServiceException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import com.amazonaws.AmazonServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb$log_errors
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__2 = RT.keyword(null, (String)"event");
    public static final Keyword const__3 = RT.keyword((String)"aws", (String)"error");
    public static final Keyword const__4 = RT.keyword(null, (String)"exception");
    public static final Keyword const__5 = RT.keyword(null, (String)"errorCode");
    public static final Keyword const__6 = RT.keyword(null, (String)"serviceName");
    public static final Keyword const__7 = RT.keyword(null, (String)"statusCode");
    public static final Keyword const__8 = RT.keyword(null, (String)"args");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"drop");
    public static final Object const__10 = 1L;
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public static Object invokeStatic(Object f, ISeq args) {
        Object object;
        try {
            Object object2 = f;
            f = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)args);
        }
        catch (AmazonServiceException ase2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.ddb");
            AmazonServiceException ex = ase2;
            if (logger.isWarnEnabled()) {
                Object[] objectArray = new Object[6];
                objectArray[0] = const__2;
                objectArray[1] = const__3;
                objectArray[2] = const__4;
                objectArray[3] = RT.mapUniqueKeys((Object[])new Object[]{const__5, ase2.getErrorCode(), const__6, ase2.getServiceName(), const__7, ase2.getStatusCode()});
                objectArray[4] = const__8;
                ISeq iSeq = args;
                args = null;
                objectArray[5] = ((IFn)const__9.getRawRoot()).invoke(const__10, (Object)iSeq);
                logger.warn((String)((IFn)const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)), (Throwable)ex);
                Logger logger2 = logger;
                logger = null;
                AmazonServiceException amazonServiceException = ex;
                ex = null;
                ((IFn)const__11.getRawRoot()).invoke((Object)logger2, (Object)amazonServiceException);
            }
            Object ase2 = null;
            throw (Throwable)ase2;
        }
        return object;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return ddb$log_errors.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

