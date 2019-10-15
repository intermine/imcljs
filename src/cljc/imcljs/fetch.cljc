(ns imcljs.fetch
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

  (:require [imcljs.internal.io :refer [restful]]
            [imcljs.query :as im-query]
            #?(:cljs [cljs.core.async :refer [<! >! chan timeout]]
               :clj
               [clojure.core.async :refer [<! >! timeout go go-loop chan]])))


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
  and returns false if the unique values exceed the limit. This is primarily for the column
  summary in im-tables-3, where we don't want to summarize columns with more than limit=1000
  values, so we avoid sending the massive request here instead."
  [service query path & [limit]]
  (let [return-chan (chan)
        <!rows-size #(<! (rows service query {:summaryPath path :size % :format "jsonrows"}))]
    (go
      ;; We send an initial size=1 request to check how many values there are.
      (let [{unique-count :uniqueValues :as res} (<!rows-size 1)]
        (cond
          ;; If there's only 1 value, we simply return our initial response.
          (= unique-count 1)           (>! return-chan res)
          ;; If there's more, we request the rest of them...
          (or (not limit)
              (<= unique-count limit)) (>! return-chan (<!rows-size limit))
          ;; ...except in the case where there are more than our limit.
          :else                        (>! return-chan false))))
    return-chan))

; Assets

(defn lists
  [service & [options]]
  (restful :get "/lists" service options :lists))

(defn one-list
  [service name & [options]]
  (restful :get "/lists" service (merge {:name name} options) (comp first :lists)))

(defn model
  [service & [options]]
  (restful :get "/model" service options :model))

(defn class-keys
  [service & [options]]
  (restful :get "/classkeys" service options :classes))

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
  ;;remove any old tokens passed in as they'll cause an auth failure
  (let [token-free-service (dissoc service :token)]
    (restful :get "/session" token-free-service options :token)))


; Widgets


(defn widgets
  "Returns list of widgets available for a given mine"
  [service & [options]]
  (restful :get "/widgets" service {:format "json"} :widgets))

; Enrichment

(defn enrichment
  "Returns enrichment results"
  [service & [options]]
  (restful :post "/list/enrichment" service (merge imcljs.internal.defaults/default-enrichment options)))


; Versions


(defn version-web-service
  "Returns the version of the InterMine WebService being run, e.g. '27'"
  [service]
  (restful :get "/version" service {:format "text"}))

(defn version-release
  [service]
  "Returns the string identifying the build - 'flymine 23', for example."
  (restful :get "/version/release" service {:format "text"}))

(defn version-intermine
  "Returns the version of InterMine being run, e.g. '1.6.6'"
  [service]
  (restful :get "/version/intermine" service {:format "text"}))

; Mine-specific config endpoint

(defn web-properties
  "Returns the default configs for a given mine. These are important as they're
   used to initialise most bluegenes page sections! "
  [service]
  (restful :get "/web-properties" service {} :web-properties))


; ID Resolution


(defn fetch-id-resolution-job-results
  "Fetches the results of an id resolution job"
  [service uid]
  (restful :get (str "/ids/" uid "/results") service {} :results))

(defn fetch-id-resolution-job-status
  "Fetches the status of an id resolution job"
  [service uid]
  (restful :get (str "/ids/" uid "/status") service {}))

(defn fetch-id-resolution-job
  "Starts an id resolution job"
  [service {:keys [identifiers type case-sensitive wild-cards extra] :as options}]
  (restful :post-body "/ids" service
           {:body (cond-> {:identifiers identifiers}
                    type (assoc :type type)
                    case-sensitive (assoc :caseSensitive true)
                    wild-cards (assoc :wildCards true)
                    extra (assoc :extra extra))
            :headers {"Content-Type" "application/json"}}))

(defn resolve-identifiers
  "Resolves identifiers. Automatically handles polling"
  [service {:keys [timeout-ms] :as options}]
  (let [return-chan (chan 1)]
    (go (let [{:keys [uid] :as job}
              (<! (fetch-id-resolution-job service options))]
          (if (not-empty uid)
            (loop [ms 100
                   total-ms 0]
              (let [{:keys [status] :as res}
                    (<! (fetch-id-resolution-job-status service uid))]
                (case status
                  "SUCCESS" (let [results
                                  (<! (fetch-id-resolution-job-results service uid))]
                              (>! return-chan results))
                  "RUNNING" (do
                              (<! (timeout ms))
                              (when (< total-ms (or timeout-ms 30000))
                                (recur (min 1000 (* ms 1.5))
                                       (+ total-ms ms))))
                  (>! return-chan res))))
            (>! return-chan job))))
    return-chan))

; Code Generation

(defn code
  "Returns generated code to run the query in a given language"
  [service model & [{:keys [lang query] :as options}]]
  (restful :get "/query/code"
           service
           (-> options
               ; Enforce JSON response so that the :code function below works
               (merge {:format "json"})
               (update :query (partial im-query/->xml model)))
           :code))

; Registry

(defn registry
  "Returns list of InterMines from the InterMine registry. dev-mines? needs to
   be set to true if you want to return non-prod mines, or otherwise set to false"
  [dev-mines?]
  (restful :raw :get "/instances"
           {:root "https://registry.intermine.org/service"}
           (when dev-mines? {:query-params {:mines "all"}})
           :instances))
