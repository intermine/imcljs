(ns imcljs.bgproperties-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.fetch :as fetch]
            [imcljs.save :as save]
            [imcljs.env :refer [service]]
            [imcljs.auth :as auth]
            [clojure.edn :as edn]))

(def bg-key "ole.dole.doffen")

(defn read-prop [value]
  (edn/read-string value))

(defn write-prop [value]
  (pr-str value))

(deftest bg-properties
  (async done
    (go
      (let [login-response (<! (auth/login service "test_user@mail_account" "secret"))
            token (get-in login-response [:output :token])
            service (assoc service :token token)]
        (testing "Add new key to bluegenes properties"
          (let [value {:foo "bar"}
                _save-value (<! (save/bluegenes-properties service bg-key (write-prop value)))
                saved (-> (<! (fetch/bluegenes-properties service))
                          (get (keyword bg-key))
                          (read-prop))]
            (is (= value saved))))
        (testing "Update an existing key in bluegenes properties"
          (let [value {:foo "baz"}
                _update-value (<! (save/update-bluegenes-properties service bg-key (write-prop value)))
                updated (-> (<! (fetch/bluegenes-properties service))
                            (get (keyword bg-key))
                            (read-prop))]
            (is (= value updated))))
        (testing "Delete an existing kkey in bluegenes properties"
          (let [_delete-value (<! (save/delete-bluegenes-properties service bg-key))
                deleted (-> (<! (fetch/bluegenes-properties service))
                            (get (keyword bg-key))
                            (read-prop))]
            (is (nil? deleted))))
        (done)))))
