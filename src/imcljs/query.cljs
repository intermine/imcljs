(ns imcljs.query
  (:require [clojure.string :refer [join]]))

(defn value [x] (str "<value>" x "</value>"))

(defn map->xmlstr
  "xml string representation of an edn map.
  (map->xlmstr constraint {:key1 val1 key2 val2}) => <constraint key1=val1 key2=val2 />"
  [elem m]
  (let [m      (select-keys m [:path :value :values :type :op :code])
        values (:values m)]
    (str "<" elem " "
         (reduce (fn [total [k v]]
                   (if (not= k :values)
                     (str total (if total " ") (name k)
                          "="
                          (str \" v \"))
                     total))
                 nil m)
         (if values
           (str ">" (apply str (map value values)) "</" elem ">")
           "/>"))))

(defn stringiy-map
  [m]
  (reduce (fn [total [k v]] (str total (if total " ") (name k) "=" (str \" v \"))) nil m))

(defn enforce-origin [query]
  (if (nil? (:from query))
    (assoc query :from (first (clojure.string/split (first (:select query)) ".")))
    query))

(defn enforce-views-have-class [query]
  (update query :select
          (partial mapv
                   (fn [path]
                     (if (= (:from query) (first (clojure.string/split path ".")))
                       path
                       (str (:from query) "." path))))))

(defn enforce-sorting [query]
  (update query :orderBy
          (partial map
                   (fn [order]
                     (let [order (if (nil? (:path order))
                                   {:path      (str (name (first (first (seq order)))))
                                    :direction (second (first (seq order)))}
                                   order)]
                       (if (= (:from query) (first (clojure.string/split (:path order) ".")))
                         order
                         (assoc order :path (str (:from query) "." (:path order)))))))))


(def sterilize-query (comp enforce-sorting enforce-views-have-class enforce-origin))

(defn ->xml
  "Returns the stringfied XML representation of an EDN intermine query."
  [model query]
  (let [query           (sterilize-query query)
        head-attributes {:model     (:name model)
                         :view      (clojure.string/join " " (:select query))
                         :sortOrder (clojure.string/join " " (flatten (map (juxt :path :direction) (:orderBy query))))}]
    (str "<query " (stringiy-map head-attributes) ">"
         (apply str (map (partial map->xmlstr "constraint") (:where query)))
         "</query>")))