# imcljs

[![Build Status](https://travis-ci.org/intermine/imcljs.svg?branch=master)](https://travis-ci.org/intermine/imcljs)

A library for interacting with Intermine's web services.

## Getting Start

Add the necessary dependency to your project:

![](https://clojars.org/intermine/imcljs/latest-version.svg)

imcljs returns channels so you'll also want to include core.async

```[org.clojure/core.async "0.2.395"]```

## Usage

All imcljs funtions expect a map as their first parameter containing a mandtory `:root` key and two semi-optional keys, `:token` and `:model`.

```clj
(def flymine {:root  "www.flymine.org/query"
              :token nil ; Optional parameter for authentication
              :model nil ; Required by some functions, such as executing a query
              })
```

We recommend fetching the `model` once and storing the above map for re-use across your application.

## Examples

### Fetching assets

```cljs
; Fetch model (you'll need this for later.)
(go (log (<! (fetch/model flymine)))

; Fetch templates
(go (log (<! (fetch/templates flymine)))

; Fetch lists
(go (log (<! (fetch/lists flymine)))

; Fetch summary fields
(go (log (<! (fetch/summary-fields flymine)))
```

### Fetching query results

Most result-fetching functions require that the `:model` key be present in their first parameter.

```cljs

(ns my-app.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [<!]]))


(def my-query {:from   "Gene"
              :select ["Gene.secondaryIdentifier Gene.symbol"]
              :where  [{:path  "Gene.symbol"
                        :op    "="
                        :value "a*"}]})

; Rows
(go (log (<! (fetch/rows flymine my-query))))

; Records
(go (log (<! (fetch/records flymine my-query {:size 10}))))

; Row Count
(go (log (<! (fetch/row-count flymine my-query))))

```
