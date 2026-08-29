(do
  (clojure.core/in-ns 'datomic.fsbackup)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.java.io :as 'jio]
        ['clojure.string :as 'str]
        ['datomic.backup :as 'backup]
        ['datomic.io :as 'io]
        ['datomic.error :as 'error])
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
          ['datomic.error :as 'error])
        (clojure.core/import 'java.io.File)
        (clojure.core/import 'java.io.FileInputStream)
        (clojure.core/import 'java.io.FileOutputStream)
        (clojure.core/import 'java.net.URI))))
  (.setMeta
    (clojure.lang.RT/var "datomic.fsbackup" "file-system-storage")
    {:declared true, :column (int 1)})
  (defn split-path
    ([path]
      (let [temp__5802__auto__ (re-find #"(.*)/([^/]*)" path)]
        (if temp__5802__auto__
          (let [vec__31609 temp__5802__auto__
                _ (nth vec__31609 (int 0) nil)
                parent (nth vec__31609 (int 1) nil)
                name (nth vec__31609 (int 2) nil)]
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
          (fn fn__31614
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
        (fn fn__31618
          ([p__31617]
            (let [vec__31619 p__31617
                  _ (nth vec__31619 (int 0) nil)
                  s (nth vec__31619 (int 1) nil)]
              (invert-case s)))))))
  (reset-meta!
    #'invert-prefix-case
    (assoc
      {:private true, :arglists (clojure.core/list ['s]), :column (int 1)}
      :name
      'invert-prefix-case
      :ns
      *ns*))
  (deftype
    FileSystemStorage
    [root]
    datomic.backup.Storage
    (retrieve
      [this k]
      (let [f (jio/file root k)
            f (if (.exists ^java.io.File f) f (jio/file root (invert-prefix-case k)))]
        (when (.exists ^java.io.File f)
          (let [len (.length ^java.io.File f)]
            (with-open [s (java.io.FileInputStream. ^java.io.File f)]
              {:v (io/read-n-bytes (long len) (.getChannel ^java.io.FileInputStream s))})))))
    (exists?
      [this k]
      (or (.exists (jio/file root k)) (.exists (jio/file root (invert-prefix-case k)))))
    (list-keys
      [this raw_prefix]
      (let [vec__31630 (split-path raw_prefix)
            parent (nth vec__31630 (int 0) nil)
            prefix (nth vec__31630 (int 1) nil)
            base (if parent (jio/file root parent) root)
            len (inc (count (.getPath ^java.io.File root)))]
        (when (.isDirectory ^java.io.File base)
          {:ks
           (filter
             (fn fn__31633
               ([p1__31626#]
                 (.startsWith ^java.lang.String p1__31626# ^java.lang.String raw_prefix)))
             (map
               (fn fn__31635 ([p1__31625#] (subs (.getPath ^java.io.File p1__31625#) (long len))))
               (remove
                 (fn fn__31637 ([p1__31624#] (.isDirectory ^java.io.File p1__31624#)))
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
  (def storage-from-uri
   (fn storage_from_uri
     ([uri]
       (if (.getHost ^java.net.URI uri)
         (error/arg :db.error/invalid-backup-uri "Storage file URI can not include host")
         (file-system-storage (.getPath (jio/as-url uri)))))))
  (reset-meta!
    #'storage-from-uri
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'uri {:tag 'URI})]), :column (int 1)}
      :name
      'storage-from-uri
      :ns
      *ns*)))