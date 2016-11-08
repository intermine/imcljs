(ns imcljs.query)

(defn map->xmlstr
  "xml string representation of an edn map.
  (map->xlmstr constraint {:key1 val1 key2 val2}) => <constraint key1=val1 key2=val2 />"
  [elem m]
  (str "<" elem " "
       (reduce (fn [total [k v]] (str total (if total " ") (name k) "=" (str \" v \"))) nil m)
       "/>"))

(defn sterilize-query [query]
  (-> query
      (update :select
              (fn [paths]
                (if (contains? query :from)
                  (mapv (fn [path]
                          (if (= (:from query) (first (clojure.string/split path ".")))
                            path
                            (str (:from query) "." path))) paths)
                  paths)))
      (update :orderBy (partial map (fn [{:keys [path] :as order}]
                                      (if (= (:from query) (first (clojure.string/split path ".")))
                                        order
                                        (assoc order :path (str (:from query) "." path))))))))

(defn ->xml
  "Returns the stringfied XML representation of an EDN intermine query."
  [model query]
  (let [query           (sterilize-query query)
        head-attributes {:model     (:name model)
                         :view      (clojure.string/join " " (:select query))
                         :sortOrder (clojure.string/join " " (flatten (map (juxt :path :direction)
                                                                           (:orderBy query))))}]
    (str "<query " (map->xmlstr "constraint" head-attributes) ">"
         (apply str (map (partial map->xmlstr "constraint") (:where query)))
         "</query>")))