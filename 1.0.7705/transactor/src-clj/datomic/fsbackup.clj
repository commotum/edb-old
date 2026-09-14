(do
  (clojure.core/in-ns 'datomic.fsbackup)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.fsbackup)
    {:doc
     "File-system implementation of backup storage. Keys are mapped to a bounded directory hierarchy and values are stored as files beneath the backup root."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'jio]
        ['clojure.string :as 'str]
        ['datomic.backup :as 'backup]
        ['datomic.io :as 'io]
        ['datomic.error :as 'error]
        ['datomic.measure.io-stats :as 'io-stats])
      (clojure.core/import 'java.io.File)
      (clojure.core/import 'java.io.FileInputStream)
      (clojure.core/import 'java.io.FileOutputStream)
      (clojure.core/import 'java.net.URI)))
  (when-not (.equals 'datomic.fsbackup 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.fsbackup))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.java.io :as 'jio]
          ['clojure.string :as 'str]
          ['datomic.backup :as 'backup]
          ['datomic.io :as 'io]
          ['datomic.error :as 'error]
          ['datomic.measure.io-stats :as 'io-stats])
        (clojure.core/import 'java.io.File)
        (clojure.core/import 'java.io.FileInputStream)
        (clojure.core/import 'java.io.FileOutputStream)
        (clojure.core/import 'java.net.URI))))
  (.setMeta
    (clojure.lang.RT/var "datomic.fsbackup" "file-system-storage")
    {:declared true, :column (int 1)})
  (defn split-path
    ([path]
      (let [temp__5823__auto__ (re-find #"(.*)/([^/]*)" path)]
        (if temp__5823__auto__
          (let [vec__28216 temp__5823__auto__
                _ (nth vec__28216 (int 0) nil)
                parent (nth vec__28216 (int 1) nil)
                name (nth vec__28216 (int 2) nil)]
            [parent name])
          [nil path]))))
  (reset-meta!
    #'split-path
    (assoc {:arglists (clojure.core/list ['path]), :column (int 1)} :name 'split-path :ns *ns*))
  (defn invert-case
    ([s]
      (apply
        str
        (map
          (fn fn__28221
            ([ch]
              (cond
                (java.lang.Character/isUpperCase (char (.charValue ^java.lang.Character ch))) (java.lang.Character/valueOf
                                                                                                (char
                                                                                                  (java.lang.Character/toLowerCase
                                                                                                    (char
                                                                                                      (.charValue
                                                                                                        ^java.lang.Character ch)))))
                (java.lang.Character/isLowerCase (char (.charValue ^java.lang.Character ch))) (java.lang.Character/valueOf
                                                                                                (char
                                                                                                  (java.lang.Character/toUpperCase
                                                                                                    (char
                                                                                                      (.charValue
                                                                                                        ^java.lang.Character ch)))))
                :default (do ch))))
          s))))
  (reset-meta!
    #'invert-case
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'invert-case
      :ns
      *ns*))
  (defn invert-prefix-case
    ([s]
      (str/replace
        s
        #"(/../)(?=[^/]*$)"
        (fn fn__28225
          ([p__28224]
            (let [vec__28226 p__28224
                  _ (nth vec__28226 (int 0) nil)
                  s (nth vec__28226 (int 1) nil)]
              (invert-case s)))))))
  (reset-meta!
    #'invert-prefix-case
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'invert-prefix-case
      :ns
      *ns*))
  ;; ATOMIC-NOTE [observed/adaptation] This is a byte/key sink, not another database engine. Prefix-case
  ;; fallback accommodates a recovered layout, not a first-release reader
  ;; to reproduce. Native backup/repository uses one hash-addressed layout,
  ;; bounded regular-file reads, authenticated reuse and fsynced publication.
  (deftype
    FileSystemStorage
    [root]
    datomic.backup.Storage
    (retrieve
      [this k]
      (let [f (jio/file root k)
            f (if (.exists ^java.io.File f) f (jio/file root (invert-prefix-case k)))]
        (when (.exists ^java.io.File f)
          (let [start (java.lang.System/nanoTime)
                len (.length ^java.io.File f)
                ret (with-open [s (java.io.FileInputStream. ^java.io.File f)]
                      {:v (io/read-n-bytes (long len) (.getChannel ^java.io.FileInputStream s))})]
            (io-stats/inc! :file)
            (io-stats/inc! :file-ns (- (java.lang.System/nanoTime) start))
            ret))))
    (exists?
      [this k]
      (or (.exists (jio/file root k)) (.exists (jio/file root (invert-prefix-case k)))))
    (list-keys
      [this raw_prefix]
      (let [vec__28237 (split-path raw_prefix)
            parent (nth vec__28237 (int 0) nil)
            prefix (nth vec__28237 (int 1) nil)
            base (if parent (jio/file root parent) root)
            len (inc (count (.getPath ^java.io.File root)))]
        (when (.isDirectory ^java.io.File base)
          {:ks
           (filter
             (fn fn__28240
               ([p1__28233#]
                 (.startsWith ^java.lang.String p1__28233# ^java.lang.String raw_prefix)))
             (map
               (fn fn__28242 ([p1__28232#] (subs (.getPath ^java.io.File p1__28232#) (long len))))
               (remove
                 (fn fn__28244 ([p1__28231#] (.isDirectory ^java.io.File p1__28231#)))
                 (file-seq base))))})))
    (store
      [this k buf]
      (let [f (jio/file root k)]
        (.mkdirs (.getParentFile ^java.io.File f))
        (with-open [s (java.io.FileOutputStream. ^java.io.File f)]
          (io/write-buffer buf (.getChannel ^java.io.FileOutputStream s)))
        {:k k})))
  (clojure.core/import 'datomic.fsbackup.FileSystemStorage)
  (defn ->FileSystemStorage ([root] (datomic.fsbackup.FileSystemStorage. root)))
  (reset-meta!
    #'->FileSystemStorage
    (assoc
      {:arglists (clojure.core/list ['root]), :column (int 1)}
      :name
      '->FileSystemStorage
      :ns
      *ns*))
  ;; Open or create a local backup directory and reject non-directory paths.
  (defn file-system-storage
    ([root]
      (let [f (jio/file root)]
        (.mkdirs ^java.io.File f)
        (when-not (.isDirectory ^java.io.File f)
          (throw
            (error/arg
              :backup/not-a-directory
              (str "Not a directory: " (.getAbsolutePath ^java.io.File f)))))
        (datomic.fsbackup.FileSystemStorage. f))))
  (reset-meta!
    #'file-system-storage
    (assoc
      {:arglists (clojure.core/list ['root]), :column (int 1)}
      :name
      'file-system-storage
      :ns
      *ns*))
  ;; Resolve a hostless file URI to backup storage.
  (defn storage-from-uri
    ([uri]
      (if (.getHost ^java.net.URI uri)
        (error/arg :db.error/invalid-backup-uri "Storage file URI can not include host")
        (file-system-storage (.getPath (jio/as-url uri))))))
  (reset-meta!
    #'storage-from-uri
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uri {:tag 'URI})]), :column (int 1)}
      :name
      'storage-from-uri
      :ns
      *ns*)))
