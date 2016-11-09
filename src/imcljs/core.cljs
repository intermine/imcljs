(ns imcljs.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(enable-console-print!)




;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (go (.log js/console "d" (<! (fetch/table-rows {:root  "www.flymine.org/query"
                                                  :model {:name "genomic"}}
                                                 {:from   "Gene"
                                                  :select ["Gene.symbol"]
                                                  :where  [{:path  "Gene.symbol"
                                                            :op    "="
                                                            :value "a*"}]})))))

