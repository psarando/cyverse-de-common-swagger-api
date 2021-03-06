(ns common-swagger-api.schema.data
  (:use [clojure-commons.error-codes]
        [common-swagger-api.schema :only [describe NonBlankString]])
  (:require [schema.core :as s]
            [schema-tools.core :as st])
  (:import [java.util UUID]))

(def CommonErrorCodeResponses [ERR_UNCHECKED_EXCEPTION ERR_SCHEMA_VALIDATION])
(def CommonErrorCodeDocs "Potential Error Codes returned by this endpoint.")

(def DataIdPathParam (describe UUID "The UUID assigned to the file or folder"))

(def PermissionEnum (s/enum :read :write :own))

(s/defschema Paths
  {:paths (describe [(s/one NonBlankString "path") NonBlankString] "A list of iRODS paths")})

(s/defschema OptionalPaths
  {(s/optional-key :paths) (describe [NonBlankString] "A list of iRODS paths")})

(s/defschema DataIds
  {:ids (describe [UUID] "A list of iRODS data-object UUIDs")})

(s/defschema OptionalPathsOrDataIds
  (-> (merge DataIds OptionalPaths)
      st/optional-keys
      (describe "The path or data ids of the data objects to gather status information on.")))
