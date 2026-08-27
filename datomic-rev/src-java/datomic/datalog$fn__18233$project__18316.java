/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$project__18316
extends AFunction {
    Object px_to;
    Object py_to;
    int proj_count;
    Object px_from;
    Object py_from;
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__16 = RT.var((String)"datomic.datalog", (String)"tuple");

    public datalog$fn__18233$project__18316(Object object, Object object2, int n, Object object3, Object object4) {
        this.px_to = object;
        this.py_to = object2;
        this.proj_count = n;
        this.px_from = object3;
        this.py_from = object4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object d, Object y) {
        long i;
        Object[] ret = RT.object_array((Object)this.proj_count);
        long n__5742__auto__18319 = ((Object[])this.px_from).length;
        for (i = 0L; i < n__5742__auto__18319; ++i) {
            Object object;
            int n;
            block10: {
                n = RT.uncheckedIntCast((Object)RT.aget((Object[])((Object[])this.px_to), (int)((int)i)));
                long G__18317 = RT.longCast((Object)RT.aget((Object[])((Object[])this.px_from), (int)((int)i)));
                switch ((int)G__18317) {
                    case 0: {
                        if (0L != G__18317) break;
                        object = Numbers.num((long)((IDatum)d).getE());
                        break block10;
                    }
                    case 1: {
                        if (1L != G__18317) break;
                        object = Numbers.num((long)RT.longCast((int)((IDatum)d).getA()));
                        break block10;
                    }
                    case 2: {
                        if (2L != G__18317) break;
                        object = ((IDatum)d).getV();
                        break block10;
                    }
                    case 3: {
                        if (3L != G__18317) break;
                        object = Numbers.num((long)((IDatum)d).getTx());
                        break block10;
                    }
                    case 4: {
                        if (4L != G__18317) break;
                        object = ((IDatum)d).isAssertion() ? Boolean.TRUE : Boolean.FALSE;
                        break block10;
                    }
                }
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__13.getRawRoot()).invoke((Object)"No matching clause: ", (Object)Numbers.num((long)G__18317)));
            }
            RT.aset((Object[])ret, (int)n, (Object)object);
        }
        long n__5742__auto__18320 = ((Object[])this.py_from).length;
        i = 0L;
        while (true) {
            if (i >= n__5742__auto__18320) {
                return ((IFn)const__16.getRawRoot()).invoke((Object)ret);
            }
            RT.aset((Object[])ret, (int)RT.uncheckedIntCast((Object)RT.aget((Object[])((Object[])this.py_to), (int)((int)i))), (Object)RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this.py_from), (int)((int)i))))));
            ++i;
        }
    }
}

