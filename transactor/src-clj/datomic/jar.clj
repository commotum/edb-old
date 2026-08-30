(do
  (clojure.core/in-ns 'datomic.jar)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.java.io :as 'io])
      (clojure.core/import 'java.util.jar.JarEntry)
      (clojure.core/import 'java.util.jar.JarOutputStream)
      (clojure.core/import 'java.util.jar.Manifest)
      (clojure.core/import 'java.util.zip.ZipEntry)
      (clojure.core/import 'java.util.zip.ZipOutputStream)
      (clojure.core/import 'java.io.File)))
  (when-not (.equals 'datomic.jar 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.jar))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.java.io :as 'io])
        (clojure.core/import 'java.util.jar.JarEntry)
        (clojure.core/import 'java.util.jar.JarOutputStream)
        (clojure.core/import 'java.util.jar.Manifest)
        (clojure.core/import 'java.util.zip.ZipEntry)
        (clojure.core/import 'java.util.zip.ZipOutputStream)
        (clojure.core/import 'java.io.File))))
  (def add-entries
   (fn add_entries
     ([jos sources transform]
       (loop [seq_26006 (seq
                          (filter
                            (fn fn__26010 ([p1__26005#] (.isFile ^java.io.File p1__26005#)))
                            sources))
              chunk_26007 nil
              count_26008 0
              i_26009 0]
         (if (< i_26009 count_26008)
           (let [source (.nth ^clojure.lang.Indexed chunk_26007 (int i_26009))]
             (let [entry (java.util.jar.JarEntry.
                           (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
               (.setTime ^java.util.zip.ZipEntry entry (long (.lastModified ^java.io.File source)))
               (.putNextEntry ^java.util.jar.JarOutputStream jos ^java.util.zip.ZipEntry entry))
             (io/copy source jos)
             (recur seq_26006 chunk_26007 count_26008 (inc i_26009)))
           (let [temp__5804__auto__ (seq seq_26006)]
             (when temp__5804__auto__
               (let [seq_26006 temp__5804__auto__]
                 (if (chunked-seq? seq_26006)
                   (let [c__6065__auto__ (chunk-first seq_26006)]
                     (recur
                       (chunk-rest seq_26006)
                       c__6065__auto__
                       (int (count c__6065__auto__))
                       (int 0)))
                   (let [source (first seq_26006)]
                     (let [entry (java.util.jar.JarEntry.
                                   (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
                       (.setTime
                         ^java.util.zip.ZipEntry entry
                         (long (.lastModified ^java.io.File source)))
                       (.putNextEntry
                         ^java.util.jar.JarOutputStream jos
                         ^java.util.zip.ZipEntry entry))
                     (io/copy source jos)
                     (recur (next seq_26006) nil 0 0)))))))))))
  (reset-meta!
    #'add-entries
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'jos {:tag 'JarOutputStream}) 'sources 'transform]),
       :column (int 1)}
      :name
      'add-entries
      :ns
      *ns*))
  (defn create-jar
    ([archive f]
      (with-open [dest (io/output-stream (io/file archive))]
        (with-open [jos (java.util.jar.JarOutputStream.
                          ^java.io.OutputStream dest
                          (java.util.jar.Manifest.))]
          (^clojure.lang.IFn f jos)))))
  (reset-meta!
    #'create-jar
    (assoc
      {:arglists (clojure.core/list ['archive 'f]), :column (int 1)}
      :name
      'create-jar
      :ns
      *ns*))
  (def add-zip-entries
   (fn add_zip_entries
     ([jos sources transform]
       (loop [seq_26017 (seq
                          (filter
                            (fn fn__26021 ([p1__26016#] (.isFile ^java.io.File p1__26016#)))
                            sources))
              chunk_26018 nil
              count_26019 0
              i_26020 0]
         (if (< i_26020 count_26019)
           (let [source (.nth ^clojure.lang.Indexed chunk_26018 (int i_26020))]
             (let [entry (java.util.zip.ZipEntry.
                           (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
               (.setTime ^java.util.zip.ZipEntry entry (long (.lastModified ^java.io.File source)))
               (.putNextEntry ^java.util.zip.ZipOutputStream jos ^java.util.zip.ZipEntry entry))
             (io/copy source jos)
             (recur seq_26017 chunk_26018 count_26019 (inc i_26020)))
           (let [temp__5804__auto__ (seq seq_26017)]
             (when temp__5804__auto__
               (let [seq_26017 temp__5804__auto__]
                 (if (chunked-seq? seq_26017)
                   (let [c__6065__auto__ (chunk-first seq_26017)]
                     (recur
                       (chunk-rest seq_26017)
                       c__6065__auto__
                       (int (count c__6065__auto__))
                       (int 0)))
                   (let [source (first seq_26017)]
                     (let [entry (java.util.zip.ZipEntry.
                                   (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
                       (.setTime
                         ^java.util.zip.ZipEntry entry
                         (long (.lastModified ^java.io.File source)))
                       (.putNextEntry
                         ^java.util.zip.ZipOutputStream jos
                         ^java.util.zip.ZipEntry entry))
                     (io/copy source jos)
                     (recur (next seq_26017) nil 0 0)))))))))))
  (reset-meta!
    #'add-zip-entries
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'jos {:tag 'ZipOutputStream}) 'sources 'transform]),
       :column (int 1)}
      :name
      'add-zip-entries
      :ns
      *ns*))
  (defn create-zip
    ([archive f]
      (with-open [dest (io/output-stream (io/file archive))]
        (with-open [jos (java.util.zip.ZipOutputStream. ^java.io.OutputStream dest)]
          (^clojure.lang.IFn f jos)))))
  (reset-meta!
    #'create-zip
    (assoc
      {:arglists (clojure.core/list ['archive 'f]), :column (int 1)}
      :name
      'create-zip
      :ns
      *ns*)))