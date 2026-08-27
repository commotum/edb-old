/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  com.datomic.lucene.store.Directory
 *  com.datomic.lucene.store.IndexInput
 *  com.datomic.lucene.store.IndexOutput
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.impl.lucene;

import clojure.lang.IFn;
import com.datomic.lucene.store.Directory;
import com.datomic.lucene.store.IndexInput;
import com.datomic.lucene.store.IndexOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HybridDirectory
extends Directory {
    private Directory readDirectory;
    private Directory writeDirectory;
    private IFn deletionHandler;
    private static Logger logger = LoggerFactory.getLogger(HybridDirectory.class);

    public HybridDirectory(Directory writeDirectory, Directory readDirectory, IFn deletionHandler) throws IOException {
        this.writeDirectory = writeDirectory;
        this.readDirectory = readDirectory;
        this.deletionHandler = deletionHandler;
        this.setLockFactory(writeDirectory.getLockFactory());
    }

    public String[] listAll() throws IOException {
        HashSet<String> s = new HashSet<String>(Arrays.asList(this.writeDirectory.listAll()));
        s.addAll(Arrays.asList(this.readDirectory.listAll()));
        return s.toArray(new String[s.size()]);
    }

    public boolean fileExists(String s) throws IOException {
        return this.writeDirectory.fileExists(s) || this.readDirectory.fileExists(s);
    }

    public long fileModified(String s) throws IOException {
        if (this.readDirectory.fileExists(s)) {
            return this.readDirectory.fileModified(s);
        }
        return this.writeDirectory.fileModified(s);
    }

    public void touchFile(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void deleteFile(String s) throws IOException {
        if (this.writeDirectory.fileExists(s)) {
            logger.debug(":hybrid/local-delete " + s);
            this.writeDirectory.deleteFile(s);
        } else {
            logger.debug(":hybrid/request-cluster-delete " + s);
            this.deletionHandler.invoke((Object)s);
        }
    }

    public long fileLength(String s) throws IOException {
        if (this.readDirectory.fileExists(s)) {
            return this.readDirectory.fileLength(s);
        }
        return this.writeDirectory.fileLength(s);
    }

    public IndexOutput createOutput(String s) throws IOException {
        return this.writeDirectory.createOutput(s);
    }

    public IndexInput openInput(String s) throws IOException {
        if (this.readDirectory.fileExists(s)) {
            return this.readDirectory.openInput(s);
        }
        return this.writeDirectory.openInput(s);
    }

    public void close() throws IOException {
        this.writeDirectory.close();
        this.readDirectory.close();
    }
}

