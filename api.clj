(ns api
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]))

(def URL "http://127.0.0.1:8080")
(def TOKEN "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vMC4wLjAuMDo4MDgwL2hlbGxvIiwiaXNzIjoiaHR0cDovLzAuMC4wLjA6ODA4MC8iLCJ1c2VybmFtZSI6ImFkbWluIiwiZXhwIjoxNzI0NDQ3NDQzfQ.5MRObougKHGxtHmh7oPyfASfYzWpne6KQBbcznk7Iv0")
(def CONTENT_TYPE {:content-type "application/json"})
(def AUTH {:authorization (str "Bearer " TOKEN)})
(defn to-json [data] (json/encode data))

(defn print-response
  [response]
  (let [has-body (not (empty? (:body response)))
        data (json/parse-string (:body response) true)]
    (if has-body
      (println (json/generate-string data {:pretty true}))
      (println response))))

(defn request
  [params]
  (let [other-params (dissoc params :type)
        url (str URL (:endpoint params))
        response 
        (case (:type params)
          :GET (http/get
                 url
                 (merge other-params {:throw false}))
          :POST (http/post
                  url
                  (merge other-params {:throw false})))]
    (print-response response)))

(case (first *command-line-args*)
  "login"
  (request
    {:type :POST
     :endpoint "/login"
     :headers (merge CONTENT_TYPE AUTH)
     :body (to-json {:username "admin", :password "abc123"})})

  "flow"
  (request
    {:type :GET
     :endpoint "/flow"
     :headers AUTH})
  
  "project"
  (request
    {:type :GET 
     :endpoint "/project"
     :headers AUTH})

  "flow-run"
  (request
    {:type :GET
     :endpoint "/flow-run"
     :headers AUTH})

  "user"
  (request 
    {:type :GET
     :endpoint "/user"
     :headers AUTH})
  )

(comment

  (request
    {:type :POST
     :endpoint "/flow-run"
     :headers (merge CONTENT_TYPE AUTH)
     :body (to-json
             {:flowId "00000000-0000-2710-0000-000000000005"
              :durationInMillis 36
              :isSuccess true
              :result "Either.Right(Unit)"})})
  )

