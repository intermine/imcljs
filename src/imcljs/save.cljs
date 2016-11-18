(ns imcljs.save
  (:require [imcljs.internal.io :refer [restful]]))


(defn im-list
  [service query & [options]]
  (restful :post "/query/tolist" service (merge {:query query :format "json"} options)))

;(defn humanify [model path]
;  (let [parts (map keyword (clojure.string/split path "."))]
;    (->>
;      (reduce (fn [total next]
;                (let [referenced-type (referenced-type model (last total) next)]
;                  (conj total referenced-type))) [(first parts)] (rest parts))
;      (map (partial display-name model)))))


;
;(defn quicksearch
;  "Returns quicksearch results."
;  [service search-term & [options]]
;  (restful :get "/search" service (merge {:q search-term} options)))
;
;; Queries
;
;(defn table-rows
;  [service query & [options]]
;  (restful :post "/query/results/tablerows" service {:query query}))
;
;(defn records
;  [service query & [options]]
;  (restful :post "/query/results" service (merge {:query query :format "jsonobjects"} options)))
;

;
;(defn row-count
;  [service query & [options]]
;  (restful :post "/query/results" service (merge {:query query :format "count"} options)))
;
;; Assets
;
;(defn lists
;  [service & [options]]
;  (restful :get "/lists" service options :lists))
;
;(defn model
;  [service & [options]]
;  (restful :get "/model" service options :model))
;
;(defn summary-fields
;  [service & [options]]
;  (restful :get "/summaryfields" service options :classes))
;
;(defn templates
;  [service & [options]]
;  (restful :get "/templates" service options :templates))
;
;; User
;
;(defn session
;  "Returns a temporary API token."
;  [service & [options]]
;  (restful :get "/session" service options :token))
;
;; Enrichment
;
;(defn enrichment
;  "Returns enrichment results"
;  [service & [options]]
;  (restful :post "/list/enrichment" service (merge imcljs.internal.defaults/default-enrichment options)))
;
;





