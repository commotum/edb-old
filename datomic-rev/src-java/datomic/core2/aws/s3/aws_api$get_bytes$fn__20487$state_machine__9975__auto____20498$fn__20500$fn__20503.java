/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.InputStream;

public final class aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500$fn__20503
extends AFunction {
    Object is;
    Object result;
    public static final Var const__2 = RT.var((String)"datomic.java.io", (String)"fill-from-stream!");
    public static final Keyword const__3 = RT.keyword(null, (String)"value");
    public static final Var const__4 = RT.var((String)"datomic.core2.anomalies", (String)"fault");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"ContentLength"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public aws_api$get_bytes$fn__20487$state_machine__9975__auto____20498$fn__20500$fn__20503(Object object, Object object2) {
        this.is = object;
        this.result = object2;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            try {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object3 = this.result;
                Object object4 = iLookupThunk.get(object3);
                if (iLookupThunk == object4) {
                    __thunk__0__ = __site__0__.fault(object3);
                    object4 = __thunk__0__.get(object3);
                }
                byte[] ba = Numbers.byte_array((Object)object4);
                ((IFn)const__2.getRawRoot()).invoke((Object)ba, this.is);
                Object[] objectArray = new Object[2];
                objectArray[0] = const__3;
                byte[] byArray = ba;
                ba = null;
                objectArray[1] = byArray;
                object2 = RT.mapUniqueKeys((Object[])objectArray);
            }
            catch (Throwable t2) {
                Object t2 = null;
                object2 = ((IFn)const__4.getRawRoot()).invoke((Object)t2);
            }
            object = object2;
        }
        finally {
            ((InputStream)this.is).close();
        }
        return object;
    }
}

