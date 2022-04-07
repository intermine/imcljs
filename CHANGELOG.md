## 1.6.0 (2022-04-07)

- Make `query/deconstruct-by-class` and `query/group-views-by-class` match legacy im-tables behaviour
    - See [imcljs PR](https://github.com/intermine/imcljs/pull/59) and [Bluegenes issue](https://github.com/intermine/bluegenes/issues/797) for details

## 1.5.0 (2021-12-17)

- New `:put-body` method, primarily for `imcljs.save/update-bluegenes-properties` to avoid hitting query param payload limit
    - `legacy?` flag added to use for IM<5.0.4 which don't read the PUT body
- Make `imcljs.query/->xml` support generating XML for templates to be uploaded
    - **BREAKING:** This means template-specific constraint attributes like `editable` and `switchable` are no longer ignored, and InterMine will respond with an error if these are present within a regular query
    - Includes comprehensive escaping for attribute value characters not compatible with XML
- Handle `:query` with a value of string, in which case it will be passed on instead of throwing
- New methods for template-related web services
    - `save/template`
    - `save/delete-template`
    - `fetch/template-tags`
    - `save/template-add-tags`
    - `save/template-remove-tags`
    - `fetch/precompute` **IM 5.0.4**
    - `fetch/summarise` **IM 5.0.4**
    - `save/precompute` **IM 5.0.4**
    - `save/summarise` **IM 5.0.4**

## 1.4.5 (2021-06-28)

- Support new entity representation web service with `fetch/entity-representation`

## 1.4.4 (2021-05-20)

- Support for `/list/chart` widget with `fetch/chart-widget`
- Support for `/list/table` widget with `fetch/table-widget`

## 1.4.3 (2021-03-02)

- Support new elinks webservice with `fetch/external-links`

## 1.4.2 (2021-02-03)

- Avoid throwing when generating PathQuery XML with nil attributes (likely model name)
    - Bug introduced with `query/escape-attribute` in 1.4.0

## 1.4.1 (2021-01-29)

- CLJ: Always return body for successful response when no xform (makes behaviour consistent with CLJS)

## 1.4.0 (2021-01-28)

[#54](https://github.com/intermine/imcljs/pull/54)
- Changes to `query/sterilize-query`
    - Handle loop constraint loopPath as value
    - Remove value/values from IS NULL and IS NOT NULL constraints
- Escape invalid characters in XML attribute value when generating PathQuery XML
- Support more webservices `fetch/semantic-markup` and `save/feedback`
- Rename `query/stringiy-map` typo to `query/stringify-map`

## 1.3.2 (2021-01-20)

[#53](https://github.com/intermine/imcljs/pull/53)
- New password reset webservices `auth/request-password-reset` and `auth/password-reset`
- Create token (specifically for type=api) with `auth/create-token`
- Handle ID resolution job for `TO_UPGRADE` lists, when `fetch/resolve-identifiers` is passed list name instead of identifiers
- Upgrade `TO_UPGRADE` list by passing list name and new object IDs to `save/im-list-upgrade`

## 1.3.1 (2020-12-15)

- Support permanent-url webservice to generate a URL to access an object even when its object ID has changed

## 1.3.0 (2020-11-25)

[#52](https://github.com/intermine/imcljs/pull/52)
- Support more webservices
    - `fetch/lists-containing` for getting lists that contain an object
    - `fetch/branding` to get logo and colors of mine
- Support new bluegenes-properties webservice
    - `fetch/bluegenes-properties`
    - `save/bluegenes-properties`
    - `save/update-bluegenes-properties`
    - `save/delete-bluegenes-properties`
- Fix GET parameters not passed when used from JVM
- Support OAuth2 via Intermine backend (see `auth/oauth2*`)
- Fixes and improvements to `imcljs.path`
    - Rewrite `path/walk` to support walking properties
    - Fix `path/split-path` not returning vector as documented
    - Make `path/display-name` work with subclasses
    - More tests for `path/walk`
- Fix `save/im-list` not able to pass more than one identifier

## 1.2.0 (2020-10-06)

- Support subclasses by passing type-constraints with model [#51](https://github.com/intermine/imcljs/pull/51)
    - Enables `imcljs.path/walk` to traverse subclasses specified via type constraints, and enables other functions dependent on it to handle subclasses correctly
- Do not add constraint code to type constraints when sterilizing query [#51](https://github.com/intermine/imcljs/pull/51)

## 1.1.0 (2020-02-20)

- Support new web services [#42](https://github.com/intermine/imcljs/pull/42)
    - `save/im-list-remove-tag` for removing a tag
    - `auth/login` for logging in and merging lists
    - `auth/logout` for invalidating token
    - `save/im-list-update` for updating list descriptions
    - `auth/change-password` for setting new password
    - `auth/register` for registering a new user
    - `fetch/preferences` for fetching user preferences
    - `save/preferences` for saving user preferences
    - `save/delete-preference` for deleting user preferences
    - `auth/deregistration` for acquiring a deregistration token
    - `auth/delete-account` for deleting a user account with a deregistration token
    - `fetch/saved-queries` for fetching saved queries
    - `save/query` for saving queries
    - `save/delete-query` for deleting saved queries
- Changes to `sterilize-query` [#42](https://github.com/intermine/imcljs/pull/42)
    - Rename `:orderBy` to `:sortOrder` if the latter is missing in PathQuery XML
    - Enforce `:sortOrder` being a vector instead of a list
- Make path/walk handle path vector with string values [#42](https://github.com/intermine/imcljs/pull/42)

## 1.0.2 (2019-11-01)

- Error handling for `fetch/resolve-identifiers` [#39](https://github.com/intermine/imcljs/pull/39)
- Fix response body being returned on erroneous requests when xform is not specified [#39](https://github.com/intermine/imcljs/pull/39)
- Fix `fetch/fetch-id-resolution-job-status` throwing ArityException when used from Clojure [#39](https://github.com/intermine/imcljs/pull/39)
- Improve efficiency of `fetch/unique-values` [#40](https://github.com/intermine/imcljs/pull/40)
- Fix warnings caused by `path/class` and `path/class?` [#41](https://github.com/intermine/imcljs/pull/41) Thanks to [@BadAlgorithm](https://github.com/BadAlgorithm)

## 1.0.1 (2019-10-09)

- Use https by default instead of http [#38](https://github.com/intermine/imcljs/pull/38)

## 1.0.0 (2019-09-24)

- Change implementation of `imcljs.fetch/registry` so it returns instances directly [#36](https://github.com/intermine/imcljs/pull/36)
- Run assertions on arguments to give a helpful error message if they are invalid [#35](https://github.com/intermine/imcljs/pull/35)
- Update dependencies [#37](https://github.com/intermine/imcljs/pull/37)
