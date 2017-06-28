(ns imcljs.fetch
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require [imcljs.internal.io :refer [restful]]
    #?(:cljs [cljs.core.async :refer [<! >! chan]]
       :clj [clojure.core.async :refer [<! >! go chan]])))

; Quicksearch

(defn quicksearch
  "Returns quicksearch results."
  [service search-term & [options]]
  (restful :get "/search" service (merge {:q search-term} options)))

; Queries

(defn table-rows
  [service query & [options]]
  (restful :post "/query/results/tablerows" service (merge {:query query} options)))

(defn fasta
  [service query & [options]]
  (restful :post "/query/results/fasta" service (merge {:query query} options)))

(defn fetch-custom-format
  ;;e.g. csv, tsv formats.
  [service query & [options]]
  (restful :post "/query/results" service (merge {:query query} options)))

(defn records
  [service query & [options]]
  (restful :post "/query/results" service (merge {:query query :format "jsonobjects"} options)))

(defn rows
  [service query & [options]]
  (restful :post "/query/results" service (merge {:query query :format "json"} options)))

(defn row-count
  [service query & [options]]
  (restful :post "/query/results" service (merge {:query query :format "count"} options)))

(defn possible-values
  [service path & [options]]
  (restful :get "/path/values" service (merge {:path path :format "json"} options)))

(defn unique-values
  "Fetches unique values for a path within a query. Providing a limit shortcircuits the request
  and returns false if the unique values exceed the limit"
  [service query path & [limit]]
  (let [return-chan (chan)]
    (go
      (let [{unique-count :uniqueValues} (<! (rows service query {:summaryPath path :size 1 :format "jsonrows"}))]
        (if (or (not limit) (<= unique-count limit))
          (>! return-chan (<! (rows service query {:summaryPath path :size limit :format "jsonrows"})))
          (>! return-chan false))))
    return-chan))

; Assets

(defn lists
  [service & [options]]
  (restful :get "/lists" service options :lists))

(defn model
  [service & [options]]
  (restful :get "/model" service options :model))

(defn summary-fields
  [service & [options]]
  (restful :get "/summaryfields" service options :classes))

(defn templates
  [service & [options]]
  (restful :get "/templates" service options :templates))

; User

(defn session
  "Returns a temporary API token."
  [service & [options]]
  (restful :get "/session" service options :token))

; Widgets

(defn widgets
  "Returns list of widgets available for a given mine"
  [service & [options]]
  (restful :get "/widgets" service {:format "json"} :widets))

; Enrichment

(defn enrichment
  "Returns enrichment results"
  [service & [options]]
  (restful :post "/list/enrichment" service (merge imcljs.internal.defaults/default-enrichment options)))


; Versions

(defn version-release
  [service]
  "Returns the string identifying the build - 'flymine 23', for example."
  (restful :get "/version/release" service {:format "text"}))

(defn version-intermine
  "Returns the version of InterMine being run, e.g. '1.6.6'"
  [service]
  (restful :get "/version/intermine" service {:format "text"}))
