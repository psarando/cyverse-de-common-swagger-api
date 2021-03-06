(ns common-swagger-api.schema.analyses
  (:use [common-swagger-api.schema :only [describe]]
        [common-swagger-api.schema.apps
         :only [AppStepResourceRequirements
                SystemId]]
        [common-swagger-api.schema.containers
         :only [coerce-settings-long-values]]
        [schema.core
         :only [defschema
                enum
                optional-key
                Any
                Keyword]])
  (:require [schema-tools.core :as st])
  (:import (java.util UUID)))

(defn- coerce-analysis-requirements-long-values
  [analysis]
  (if (contains? analysis :requirements)
    (update analysis :requirements (partial map coerce-settings-long-values))
    analysis))

(defn coerce-analysis-submission-requirements
  "Middleware that converts any requirements values in the given analysis submission that should be a Long."
  [handler]
  (fn [request]
    (handler (update request :body-params coerce-analysis-requirements-long-values))))

(def AnalysisParametersSummary "Display the parameters used in an analysis.")
(def AnalysisParametersDocs
  "This service returns a list of parameter values used in a previously executed analysis.")

(def AnalysisRelaunchSummary "Obtain information to relaunch analysis.")
(def AnalysisRelaunchDocs
  "This service allows the Discovery Environment user interface to obtain an app description
   that can be used to relaunch a previously submitted job,
   possibly with modified parameter values.")

(def AnalysesRelauncherSummary "Auto Relaunch Analyses")

(def AnalysisStopSummary "Stop a running analysis.")
(def AnalysisStopDocs
  "This service allows DE users to stop running analyses.")

(def AnalysisIdPathParam (describe UUID "The Analysis UUID"))

(defschema ParameterValue
  {:value
   (describe Any "The value of the parameter.")})

(defschema AnalysisParameter
  {:full_param_id
   (describe String "The fully qualified parameter ID.")

   :param_id
   (describe String "The unqualified parameter ID.")

   (optional-key :param_name)
   (describe String "The name of the parameter.")

   (optional-key :param_value)
   (describe ParameterValue "The value of the parameter.")

   :param_type
   (describe String "The type of the parameter.")

   (optional-key :info_type)
   (describe String "The type of information associated with an input or output parameter.")

   (optional-key :data_format)
   (describe String "The data format associated with an input or output parameter.")

   (optional-key :is_default_value)
   (describe Boolean "Indicates whether the default parameter value was used.")

   (optional-key :is_visible)
   (describe Boolean "Indicates whether the parameter is visible in the app UI.")})

(defschema AnalysisParameters
  {:app_id     (describe String "The ID of the app used to perform the analysis.")
   :system_id  SystemId
   :parameters (describe [AnalysisParameter] "The list of parameters.")})

(defschema AnalysesRelauncherRequest
  {:analyses (describe [UUID] "The identifiers of the analyses to be relaunched.")})

(defschema AnalysisShredderRequest
  {:analyses (describe [UUID] "The identifiers of the analyses to be deleted.")})

(defschema StopAnalysisRequest
  {(optional-key :job_status)
   (describe (enum "Canceled" "Completed" "Failed") "The job status to set. Defaults to `Canceled`")})

(defschema StopAnalysisResponse
  {:id (describe UUID "the ID of the stopped analysis.")})

(defschema FileMetadata
  {:attr  (describe String "The attribute name.")
   :value (describe String "The attribute value.")
   :unit  (describe String "The attribute unit.")})

(defschema AnalysisSubmissionConfig
  {(describe Keyword "The step-ID_param-ID") (describe Any "The param-value")})

(defschema AnalysisStepResourceRequirements
  (st/select-keys AppStepResourceRequirements [:min_memory_limit
                                               :min_cpu_cores
                                               :min_disk_space
                                               :step_number]))

(defschema AnalysisSubmission
  {:system_id
   SystemId

   :app_id
   (describe String "The ID of the app used to perform the analysis.")

   (optional-key :job_id)
   (describe UUID "The UUID of the job being submitted.")

   (optional-key :callback)
   (describe String "The callback URL to use for job status updates.")

   :config
   (describe AnalysisSubmissionConfig "A map from (str step-id \"_\" param-id) to param-value.")

   (optional-key :requirements)
   (describe [AnalysisStepResourceRequirements] "The list of optional resource requirements requested for any step")

   (optional-key :create_output_subdir)
   (describe Boolean "Indicates whether a subdirectory should be created beneath the specified output directory.")

   :debug
   (describe Boolean "A flag indicating whether or not job debugging should be enabled.")

   (optional-key :description)
   (describe String "An optional description of the analysis.")

   :name
   (describe String "The name assigned to the analysis by the user.")

   :notify
   (describe Boolean "Indicates whether the user wants to receive job status update notifications.")

   :output_dir
   (describe String "The path to the analysis output directory in the data store.")

   (optional-key :starting_step)
   (describe Long "The ordinal number of the step to start the job with.")

   (optional-key :uuid)
   (describe UUID "The UUID of the analysis. A random UUID will be assigned if one isn't provided.")

   (optional-key :skip-parent-meta)
   (describe Boolean "True if metadata should not associate metadata with the parent directory.")

   (optional-key :file-metadata)
   (describe [FileMetadata] "Custom file attributes to associate with result files.")

   (optional-key :archive_logs)
   (describe Boolean "True if the job logs should be uploaded to the data store.")})

(defschema AnalysisResponse
  {:id         (describe UUID "The ID of the submitted analysis.")
   :name       (describe String "The name of the submitted analysis.")
   :status     (describe String "The current status of the analysis.")
   :start-date (describe String "The analysis start date as milliseconds since the epoch.")

   (optional-key :missing-paths)
   (describe [String] "Any paths parsed from an HT Analysis Path List that no longer exist.")})

(defschema AnalysisPod
  {:name        (describe String "The name of a pod in Kubernetes associated with an analysis.")
   :external_id (describe UUID "The external ID associated with the pod.")})

(def AnalysisPodListSummary
  "List the Kubernetes pods associated with the analysis.")

(def AnalysisPodListDescription
  "This endpoint returns a listing of pod objects associated with the analysis. Usually will return a single pod.")

(defschema AnalysisPodList
  {:pods (describe [AnalysisPod] "A list of pods in Kubernetes associated with an analysis.")})

(def AnalysisPodLogSummary
  "The logs from a pod associated with the analysis")

(def AnalysisPodLogDescription
  "This endpoint returns the logs from the provided pod associated with the provided analysis.")

(defschema AnalysisPodLogParameters
  {(optional-key :previous)
   (describe Boolean "True if the logs of a previously terminated container should be returned")

   (optional-key :since)
   (describe Long "Number of seconds in the past to start showing logs")

   (optional-key :since-time)
   (describe String "The time at which to start showing log lines. Expressed as seconds since the epoch.")

   (optional-key :tail-lines)
   (describe Long "Number of lines from the end of the log to show")

   (optional-key :timestamps)
   (describe Boolean "True if timestamps should be prepended to the log lines")

   (optional-key :container)
   (describe String "Name of the container to display logs from. Defaults to 'analysis'")})

(defschema AnalysisPodLogEntry
  {:since_time
   (describe String "Contains the seconds since the epoch for the time when the log entry was retrieved")

   :lines
   (describe [String] "The lines that make up the log entry")})

(defschema AnalysisTimeLimit
  {:time_limit
   (describe String "Contains the seconds since the epoch for the analysis's time limit or the string 'null' if the time limit isn't set.")})
