/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class common$log_and_print
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"print-str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");

    public static Object invokeStatic(ISeq xs) {
        block0: {
            ISeq iSeq = xs;
            xs = null;
            Object s = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)iSeq);
            ((IFn)const__2.getRawRoot()).invoke(s);
            Logger logger = LoggerFactory.getLogger((String)"datomic.common");
            if (!logger.isInfoEnabled()) break block0;
            Logger logger2 = logger;
            logger = null;
            Object object = s;
            s = null;
            logger2.info((String)((IFn)const__3.getRawRoot()).invoke(object));
        }
        return null;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return common$log_and_print.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

