/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IDeref
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.store.Directory
 *  com.datomic.lucene.store.IndexInput
 *  com.datomic.lucene.store.IndexOutput
 *  com.datomic.lucene.store.LockFactory
 *  com.datomic.lucene.store.RAMFile
 *  com.datomic.lucene.store.RAMOutputStream
 *  com.datomic.lucene.store.SingleInstanceLockFactory
 */
package datomic.impl.lucene;

import clojure.lang.IDeref;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.store.DatomicThunk;
import com.datomic.lucene.store.Directory;
import com.datomic.lucene.store.IndexInput;
import com.datomic.lucene.store.IndexOutput;
import com.datomic.lucene.store.LockFactory;
import com.datomic.lucene.store.RAMFile;
import com.datomic.lucene.store.RAMOutputStream;
import com.datomic.lucene.store.SingleInstanceLockFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class DirectoryRef
extends Directory
implements IDeref {
    public static final Var swap = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var atom = RT.var((String)"clojure.core", (String)"atom");
    public static final Var dissoc = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var assoc = RT.var((String)"clojure.core", (String)"assoc");
    public final IDeref ref;

    public DirectoryRef(IPersistentMap m) throws IOException {
        this.ref = (IDeref)atom.invoke((Object)m);
        this.setLockFactory((LockFactory)new SingleInstanceLockFactory());
    }

    Map getMap() {
        return (Map)this.ref.deref();
    }

    RAMFile getRAMFile(String s) throws FileNotFoundException {
        RAMFile ramFile = (RAMFile)this.getMap().get(s);
        if (ramFile == null) {
            throw new FileNotFoundException(s);
        }
        return ramFile;
    }

    public String[] listAll() throws IOException {
        return this.getMap().keySet().toArray(new String[0]);
    }

    public boolean fileExists(String s) throws IOException {
        return this.getMap().containsKey(s);
    }

    public long fileModified(String s) throws IOException {
        throw new IOException("trying to get modify date on " + s);
    }

    public void touchFile(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void deleteFile(String s) throws IOException {
        swap.invoke((Object)this.ref, (Object)dissoc, (Object)s);
    }

    public long fileLength(String s) throws IOException {
        return this.getRAMFile(s).getLength();
    }

    public IndexOutput createOutput(String s) throws IOException {
        RAMFile ramFile = DatomicThunk.createRAMFile();
        swap.invoke((Object)this.ref, (Object)assoc, (Object)s, (Object)ramFile);
        return new RAMOutputStream(ramFile);
    }

    public IndexInput openInput(String s) throws IOException {
        return DatomicThunk.createRAMInputStream(this.getRAMFile(s));
    }

    public void close() throws IOException {
    }

    public Object deref() {
        return this.ref.deref();
    }
}

