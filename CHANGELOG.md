## 1.0.2 (2019-11-01)

- Error handling for `fetch/resolve-identifiers` [#39](https://github.com/intermine/imcljs/pull/39)
- Fix response body being returned on erroneous requests when xform is not specified [#39](https://github.com/intermine/imcljs/pull/39)
- Fix `fetch/fetch-id-resolution-job-status` throwing ArityException when used from Clojure [#39](https://github.com/intermine/imcljs/pull/39)
- Improve efficiency of `fetch/unique-values` [#40](https://github.com/intermine/imcljs/pull/40)
- Fix warnings caused by `path/class` and `path/class?` [#41](https://github.com/intermine/imcljs/pull/41) Thanks to @BadAlgorithm

## 1.0.1 (2019-10-09)

- Use https by default instead of http [#38](https://github.com/intermine/imcljs/pull/38)

## 1.0.0 (2019-09-24)

- Change implementation of `imcljs.fetch/registry` so it returns instances directly [#36](https://github.com/intermine/imcljs/pull/36)
- Run assertions on arguments to give a helpful error message if they are invalid [#35](https://github.com/intermine/imcljs/pull/35)
- Update dependencies [#37](https://github.com/intermine/imcljs/pull/37)
