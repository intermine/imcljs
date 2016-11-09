(ns imcljs.fetch
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [cljs-http.client :refer [post get]]
            [imcljs.utils :refer [scrub-url]]
            [imcljs.query :refer [->xml]]
            [imcljs.defaults :refer [path wrap-request-defaults wrap-query-defaults wrap-auth]]))



(defn table-rows
  "Returns the results of queries as table rows."
  [{:keys [root token model] :as service} query & [options]]
  (post (path root "/query/results/tablerows")
        (-> {} ; Blank request map
            wrap-request-defaults ; Add defaults such as with-credentials false?
            (wrap-query-defaults model query options)
            (wrap-auth token))))
