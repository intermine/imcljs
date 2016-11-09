(ns imcljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [<!]]))

(enable-console-print!)

(defn on-js-reload []
  (def flymine {:root  "www.flymine.org/query"
                :model {:name "genomic"}})

  (def a-query {:from   "Gene"
                :select ["Gene.secondaryIdentifier Gene.symbol"]
                :where  [{:path  "Gene.symbol"
                          :op    "="
                          :value "a*"}]})

  (go (.log js/console "templates" (<! (fetch/templates flymine))))
  (go (.log js/console "enrichment" (<! (fetch/enrichment flymine {:list "PL FlyTF_putativeTFs"}))))
  (go (.log js/console "rows" (<! (fetch/rows flymine a-query)))))