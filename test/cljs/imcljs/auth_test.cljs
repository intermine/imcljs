(ns imcljs.auth-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.fetch :as fetch]))

(def service {:root "www.flymine.org/query"
              :model {:name "genomic"}
              ;;imcljs should cope with an expired token just fine
              :token "THIS_TOKEN_IS_EXPIRED"})

(deftest anon-token
  (testing "Able to retrieve anon token, even if passed an old/expired token"
    (async done
           (go
             (let [response (<! (fetch/session service))]
               ;; InterMine returns a 401 if you request a new token and pass an
               ;; old/bad/expired token. We can work around it though! 
               (is (not (= 401 (:status response))))
               (done))))))
