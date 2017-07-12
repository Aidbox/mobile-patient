(ns patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [reg-event-db after reg-event-fx reg-fx
                                   dispatch subscribe reg-sub-raw reg-sub]]
            [mobile-patient.color :as color]
            [re-frame.loggers :as rf.log]
            [mobile-patient.ui :as ui]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [mobile-patient.lib.jwt :as jwt]
            [mobile-patient.lib.helper :as h]))


(def warn (js/console.warn.bind js/console))
(rf.log/set-loggers!
 {:warn (fn [& args]
          (cond
            (= "re-frame: overwriting" (first args)) nil
            :else (apply warn args)))})

;; -- Handlers --------------------------------------------------------------
(reg-event-db
 :on-chats
 (fn [db [_ value]]
   (let [chats (map :resource (:entry value))]
     (if (not (= (:chats db) chats))
       (assoc db :chats chats)
       db))))

(reg-event-db
 :on-get-medication-statements
 (fn [db [_ value]]
   (let [medication-statements (sort-by #(-> % :effective :dateTime) (map :resource (:entry value)))
         groups (group-by #(= (:status %) "active") medication-statements)]
     (-> db
         (assoc :active-medication-statements (groups true))
         (assoc :other-medication-statements (groups false))))))

(reg-event-db
 :set-chat
 (fn [db [_ chat]]
   (-> db
       (assoc :chat chat)
       (assoc :messages []))))

(reg-event-db
 :set-message
 (fn [db [_ value]]
   (assoc db :message value)))

(reg-event-db
 :on-messages
 (fn [db [_ value]]
   (let [messages (map :resource (:entry value))]
     (if (not (= (:messages db) messages))
       (assoc db :messages messages)
       db))))

(reg-event-fx
 :on-send-message
 (fn [db [_ value]]
   {}))

(reg-event-fx
 :get-chats
 (fn [_]
   (let [user (subscribe [:user-id])]
     {:fetch {:uri "/Chat"
              :success :on-chats
              :opts {:parms {:participant @user}}}})))

(reg-event-fx
 :get-messages
 (fn [_ [_ chat-id]]
   (let [user (subscribe [:user-id])]
     {:fetch {:uri "/Message"
              :success :on-messages
              :opts {:parms {:chat chat-id}}}})))

(reg-event-fx
 :send-message
 (fn [_]
   (let [message @(subscribe [:get-in [:message]])
         user @(subscribe [:user-id])
         chat @(subscribe [:get-in [:chat]])
         msg {:resourceType "Message"
              :body message
              :chat {:id (:id chat)
                     :resourceType "Chat"}
              :author {:id user
                       :resourceType "User"}}]
     (if (and message (not (clojure.string/blank? message)))
       {:fetch {:uri "/Message"
                :success :on-send-message
                :opts {:method "POST"
                       :headers {"content-type" "application/json"}
                       :body (.stringify js/JSON (clj->js msg))}}
        :dispatch [:set-message ""]}
       {}))))

(reg-event-fx
 :create-chat
 (fn [_ [_ participants]]
   (let [user @(subscribe [:user-id])
         chat-name (first participants) ; todo: correct chat name
         chat {:resourceType "Chat"
               :name chat-name
               :participants (map (fn [p] {:id p :resourceType "User"}) (conj participants user))}]
     {:fetch {:uri "/Chat"
              :opts {:method "POST"
                     :headers {"content-type" "application/json"}
                     :body (.stringify js/JSON (clj->js chat))}}})))


(reg-event-db
 :set-contacts
 (fn [db [_ value ids]]
   (let [users (map :resource (:entry value))
         contacts (filter #((set ids) (-> % :ref :id)) users)]
     (assoc db :contacts contacts))))

(reg-event-fx
 :on-get-users
 (fn [cofx [_ value]]
   {:db (assoc (:db cofx) :users (map :resource (:entry value)))
    :dispatch [:get-contacts]}))

(reg-event-fx
 :set-user
 (fn [cofx [_ value]]
   {:db (->  (:db cofx)
             (assoc :user value)
             (assoc :chats [])
             (assoc :contacts [])
             (assoc :current-screen :main))
    :dispatch [:on-set-user]}))

(reg-event-fx
 :on-set-user
 (fn [_]
   (let [user-ref @(subscribe [:user-ref])]
     (case (:resourceType user-ref)
       "Patient" {:fetch {:uri (str "/Patient/" (:id user-ref))
                          :success :on-get-patient
                          :opts {:method "GET"}}}
       "Practitioner" {:fetch {:uri "/Patient"
                               :success :on-get-patients
                               :opts {:method "GET"}}}
       {}))))

(reg-event-fx
 :on-get-patient
 (fn [_ [_ value]]
   (let [prac-ids (map :id (filter #(= (:resourceType %) "Practitioner") (:generalPractitioner value)))]
     {:fetch {:uri "/User"
              :success :set-contacts
              :success-parms prac-ids
              :opts {:method "GET"}}})))

(reg-event-fx
 :on-get-patients
 (fn [_ [_ value]]
   (let [pat-ids (map #(-> % :resource :id) (:entry value))]
     {:fetch {:uri "/User"
              :success :set-contacts
              :success-parms pat-ids
              :opts {:method "GET"}}})))

(reg-event-fx
 :set-demographics
 (fn [{:keys [db]} [_ resource-type-data]]
   (if (h/contains-many? resource-type-data :gender :birthDate :address)
     {:db (merge db {:resource-type-data resource-type-data})
      :dispatch [:set-user (:user db)]}

     {:db (merge db {:resource-type-data resource-type-data
                     :current-screen :demographics})})))

(reg-event-fx
 :on-user-load
 (fn [{:keys [db]} [_ user-data]]
   {:fetch {:uri (str "/" (get-in user-data [:ref :resourceType])
                      "/" (get-in user-data [:ref :id]))
            :opts {:method "GET"}
            :success :set-demographics}
    :db (merge db {:user user-data})}))

(reg-event-fx
 :on-login
 (fn [{:keys [db]} [_ resp-body _ resp]]
   (if resp.ok
     (let [invalid (boolean (re-find #"Wrong credentials" resp-body))]
       (if invalid
         (ui/alert "" "Wrong credentials")
         (let [auth-data (-> (.-url resp) (str/split #"#") second h/query->params)
               id-token (:id_token auth-data)
               token-data (jwt/get-data-from-token id-token)
               user-id  (:user-id token-data)]
           (assert user-id)
           {:fetch {:uri (str "/User/" user-id)
                    :opts {:method "GET"}
                    :success :on-user-load}
            :db (merge db {:access-token (:access_token auth-data)})})))
     (ui/alert "Error" (str resp.status " " resp.statusText)))))


(reg-event-fx
 :get-medication-statements
 (fn [_]
   (let [user-ref @(subscribe [:user-ref])]
     {:fetch {:uri "/MedicationStatement"
              :success :on-get-medication-statements
              :opts {:parms {:subject (:id user-ref)}
                     :method "GET"}}})))
(reg-event-fx
 :on-submit-demographics
 (fn [{:keys [db]} [_ resource-type-data]]
   {:db (merge db {:resource-type-data resource-type-data})
    :dispatch [:set-user (:user db)]
    }))

(reg-event-fx
 :submit-demographics
 (fn [_ [_ form-data]]
   (let [user @(subscribe [:get-in [:user]])
         resource-type-data @(subscribe [:get-in [:resource-type-data]])]
     {:fetch {:uri (str "/" (get-in user [:ref :resourceType ])
                        "/" (get-in user [:ref :id ]))
              :success :on-submit-demographics
              :opts {:method "PUT"
                     :headers {"content-type" "application/json"}
                     :body (js/JSON.stringify
                            (clj->js
                             (merge resource-type-data
                                    {:gender (:sex form-data)
                                     :birthDate (:birthday form-data)
                                     :address [{:use "home"
                                                :type "postal"
                                                :text (:address form-data)
                                                }]})))}}})))

(reg-event-db
 :on-vitals-sign-screen
 (fn [db _]
   (assoc db :observations
          [{
            :resourceType "Observation",
            :id "blood-pressure",
            :meta {
                   :profile [
                             "http//hl7.org/fhir/StructureDefinition/vitalsigns"
                             ]
                   },
            :text {
                   :status "generated",
                   :div "<div xmlns=\"http//www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b> blood-pressure</p><p><b>meta</b> </p><p><b>identifier</b> urnuuid187e0c12-8dd2-67e2-99b2-bf273c878281</p><p><b>basedOn</b> </p><p><b>status</b> final</p><p><b>category</b> Vital Signs <span>(Details  {http//hl7.org/fhir/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b> Blood pressure systolic &amp; diastolic <span>(Details  {LOINC code '85354-9' = 'lood pressure panel with all children optional', given as 'Bood pressure panel with all children optional'})</span></p><p><b>subject</b> <a>Patient/example</a></p><p><b>effective</b> 17/09/2012</p><p><b>performer</b> <a>Practitioner/example</a></p><p><b>interpretation</b> Below low normal <span>(Details  {http//hl7.org/fhir/v2/0078 code 'L' = 'Low', given as 'low'})</span></p><p><b>bodySite</b> Right arm <span>(Details  {SNOMED CT code '368209003' = 'Right upper arm', given as 'Right arm'})</span></p><blockquote><p><b>component</b></p><p><b>code</b> Systolic blood pressure <span>(Details  {LOINC code '8480-6' = 'Systolic blood pressure', given as 'Systolic blood pressure'}; {SNOMED CT code '271649006' = 'Systolic blood pressure', given as 'Systolic blood pressure'}; {http//acme.org/devices/clinical-codes code 'bp-s' = 'bp-s', given as 'Systolic Blood pressure'})</span></p><p><b>value</b> 107 mmHg<span> (Details UCUM code mm[Hg] = 'mmHg')</span></p><p><b>interpretation</b> Normal <span>(Details  {http//hl7.org/fhir/v2/0078 code 'N' = 'Normal', given as 'normal'})</span></p></blockquote><blockquote><p><b>component</b></p><p><b>code</b> Diastolic blood pressure <span>(Details  {LOINC code '8462-4' = 'Diastolic blood pressure', given as 'Diastolic blood pressure'})</span></p><p><b>value</b> 60 mmHg<span> (Details UCUM code mm[Hg] = 'mmHg')</span></p><p><b>interpretation</b> Below low normal <span>(Details  {http//hl7.org/fhir/v2/0078 code 'L' = 'Low', given as 'low'})</span></p></blockquote></div>"
                   },
            :identifier [
                         {
                          :system "urnietfrfc3986",
                          :value "urnuuid187e0c12-8dd2-67e2-99b2-bf273c878281"
                          }
                         ],
            :basedOn [
                      {
                       :identifier {
                                    :system "https//acme.org/identifiers",
                                    :value "1234"
                                    }
                       }
                      ],
            :status "final",
            :category [
                       {
                        :coding [
                                 {
                                  :system "http//hl7.org/fhir/observation-category",
                                  :code "vital-signs",
                                  :display "Vital Signs"
                                  }
                                 ]
                        }
                       ],
            :code {
                   :coding [
                            {
                             :system "http//loinc.org",
                             :code "85354-9",
                             :display "Bood pressure panel with all children optional"
                             }
                            ],
                   :text "Blood pressure systolic & diastolic"
                   },
            :subject {
                      :reference "Patient/example"
                      },
            :effectiveDateTime "2012-09-17",
            :performer [
                        {
                         :reference "Practitioner/example"
                         }
                        ],
            :interpretation {
                             :coding [
                                      {
                                       :system "http//hl7.org/fhir/v2/0078",
                                       :code "L",
                                       :display "low"
                                       }
                                      ],
                             :text "Below low normal"
                             },
            :bodySite {
                       :coding [
                                {
                                 :system "http//snomed.info/sct",
                                 :code "368209003",
                                 :display "Right arm"
                                 }
                                ]
                       },
            :component [
                        {
                         :code {
                                :coding [
                                         {
                                          :system "http//loinc.org",
                                          :code "8480-6",
                                          :display "Systolic blood pressure"
                                          },
                                         {
                                          :system "http//snomed.info/sct",
                                          :code "271649006",
                                          :display "Systolic blood pressure"
                                          },
                                         {
                                          :system "http//acme.org/devices/clinical-codes",
                                          :code "bp-s",
                                          :display "Systolic Blood pressure"
                                          }
                                         ]
                                },
                         :valueQuantity {
                                         :value 107,
                                         :unit "mmHg",
                                         :system "http//unitsofmeasure.org",
                                         :code "mm[Hg]"
                                         },
                         :interpretation {
                                          :coding [
                                                   {
                                                    :system "http//hl7.org/fhir/v2/0078",
                                                    :code "N",
                                                    :display "normal"
                                                    }
                                                   ],
                                          :text "Normal"
                                          }
                         },
                        {
                         :code {
                                :coding [
                                         {
                                          :system "http//loinc.org",
                                          :code "8462-4",
                                          :display "Diastolic blood pressure"
                                          }
                                         ]
                                },
                         :valueQuantity {
                                         :value 60,
                                         :unit "mmHg",
                                         :system "http//unitsofmeasure.org",
                                         :code "mm[Hg]"
                                         },
                         :interpretation {
                                          :coding [
                                                   {
                                                    :system "http//hl7.org/fhir/v2/0078",
                                                    :code "L",
                                                    :display "low"
                                                    }
                                                   ],
                                          :text "Below low normal"
                                          }
                         }
                        ]
            },
           {
            :resourceType "Observation"
            :id "f202"
            :text {
                     :status "generated"
                     :div "<div xmlns=\"http//www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b> f202</p><p><b>status</b> entered-in-error</p><p><b>category</b> Vital Signs <span>(Details  {http//hl7.org/fhir/observation-category code 'vital-signs' = 'Vital Signs' given as 'Vital Signs'})</span></p><p><b>code</b> Temperature <span>(Details  {http//acme.lab code 'BT' = 'BT' given as 'Body temperature'}; {LOINC code '8310-5' = 'Body temperature' given as 'Body temperature'}; {LOINC code '8331-1' = 'Oral temperature' given as 'Oral temperature'}; {SNOMED CT code '56342008' = 'Temperature taking' given as 'Temperature taking'})</span></p><p><b>subject</b> <a>Roel</a></p><p><b>issued</b> 04/04/2013 12700 PM</p><p><b>performer</b> <a>Practitioner/f201</a></p><p><b>value</b> 39 degrees C<span> (Details UCUM code Cel = 'Cel')</span></p><p><b>interpretation</b> High <span>(Details  {http//hl7.org/fhir/v2/0078 code 'H' = 'High)</span></p><p><b>bodySite</b> Oral cavity <span>(Details  {SNOMED CT code '74262004' = 'Oral cavity' given as 'Oral cavity'})</span></p><p><b>method</b> Oral temperature taking <span>(Details  {SNOMED CT code '89003005' = 'Oral temperature taking' given as 'Oral temperature taking'})</span></p><h3>ReferenceRanges</h3><table><tr><td>-</td><td><b>High</b></td></tr><tr><td>*</td><td>38.2 degrees C</td></tr></table></div>"
                     }
            :status "entered-in-error"
            :category [
                         {
                          :coding [
                                     {
                                      :system "http//hl7.org/fhir/observation-category"
                                      :code "vital-signs"
                                      :display "Vital Signs"
                                      }
                                     ]
                          }
                         ]
            :code {
                     :coding [
                                {
                                 :system "http//acme.lab"
                                 :code "BT"
                                 :display "Body temperature"
                                 }
                                {
                                 :system "http//loinc.org"
                                 :code "8310-5"
                                 :display "Body temperature"
                                 }
                                {
                                 :system "http//loinc.org"
                                 :code "8331-1"
                                 :display "Oral temperature"
                                 }
                                {
                                 :system "http//snomed.info/sct"
                                 :code "56342008"
                                 :display "Temperature taking"
                                 }
                                ]
                     :text "Temperature"
                     }
            :subject {
                        :reference "Patient/f201"
                        :display "Roel"
                        }
            :issued "2013-04-04T132700+0100"
            :performer [
                          {
                           :reference "Practitioner/f201"
                           }
                          ]
            :valueQuantity {
                              :value 39
                              :unit "degrees C"
                              :system "http//unitsofmeasure.org"
                              :code "Cel"
                              }
            :interpretation {
                               :coding [
                                          {
                                           :system "http//hl7.org/fhir/v2/0078"
                                           :code "H"
                                           }
                                          ]
                               }
            :bodySite {
                         :coding [
                                    {
                                     :system "http//snomed.info/sct"
                                     :code "74262004"
                                     :display "Oral cavity"
                                     }
                                    ]
                         }
            :method {
                       :coding [
                                  {
                                   :system "http//snomed.info/sct"
                                   :code "89003005"
                                   :display "Oral temperature taking"
                                   }
                                  ]
                       }
            :referenceRange [
                               {
                                :high {
                                         :value 38.2
                                         :unit "degrees C"
                                         }
                                }
                               ]
            },
           {
            :resourceType "Observation",
            :id "heart-rate",
            :meta {
                   :profile [
                             "http//hl7.org/fhir/StructureDefinition/vitalsigns"
                             ]
                   },
            :text {
                   :status "generated",
                   :div "<div xmlns=\"http//www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b> heart-rate</p><p><b>meta</b> </p><p><b>status</b> final</p><p><b>category</b> Vital Signs <span>(Details  {http//hl7.org/fhir/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b> Heart rate <span>(Details  {LOINC code '8867-4' = 'Heart rate', given as 'Heart rate'})</span></p><p><b>subject</b> <a>Patient/example</a></p><p><b>effective</b> 02/07/1999</p><p><b>value</b> 44 beats/minute<span> (Details UCUM code /min = '/min')</span></p></div>"
                   },
            :status "final",
            :category [
                       {
                        :coding [
                                 {
                                  :system "http//hl7.org/fhir/observation-category",
                                  :code "vital-signs",
                                  :display "Vital Signs"
                                  }
                                 ],
                        :text "Vital Signs"
                        }
                       ],
            :code {
                   :coding [
                            {
                             :system "http//loinc.org",
                             :code "8867-4",
                             :display "Heart rate"
                             }
                            ],
                   :text "Heart rate"
                   },
            :subject {
                      :reference "Patient/example"
                      },
            :effectiveDateTime "1999-07-02",
            :valueQuantity {
                            :value 44,
                            :unit "beats/minute",
                            :system "http//unitsofmeasure.org",
                            :code "/min"
                            }
            },
           {
            :resourceType "Observation",
            :id "respiratory-rate",
            :meta {
                   :profile [
                             "http//hl7.org/fhir/StructureDefinition/vitalsigns"
                             ]
                   },
            :text {
                   :status "generated",
                   :div "<div xmlns=\"http//www.w3.org/1999/xhtml\"><p><b>Generated Narrative with Details</b></p><p><b>id</b> respiratory-rate</p><p><b>meta</b> </p><p><b>status</b> final</p><p><b>category</b> Vital Signs <span>(Details  {http//hl7.org/fhir/observation-category code 'vital-signs' = 'Vital Signs', given as 'Vital Signs'})</span></p><p><b>code</b> Respiratory rate <span>(Details  {LOINC code '9279-1' = 'Respiratory rate', given as 'Respiratory rate'})</span></p><p><b>subject</b> <a>Patient/example</a></p><p><b>effective</b> 02/07/1999</p><p><b>value</b> 26 breaths/minute<span> (Details UCUM code /min = '/min')</span></p></div>"
                   },
            :status "final",
            :category [
                       {
                        :coding [
                                 {
                                  :system "http//hl7.org/fhir/observation-category",
                                  :code "vital-signs",
                                  :display "Vital Signs"
                                  }
                                 ],
                        :text "Vital Signs"
                        }
                       ],
            :code {
                   :coding [
                            {
                             :system "http//loinc.org",
                             :code "9279-1",
                             :display "Respiratory rate"
                             }
                            ],
                   :text "Respiratory rate"
                   },
            :subject {
                      :reference "Patient/example"
                      },
            :effectiveDateTime "1999-07-02",
            :valueQuantity {
                            :value 26,
                            :unit "breaths/minute",
                            :system "http//unitsofmeasure.org",
                            :code "/min"
                            }
            }])))

#_(reg-event-fx
 :on-vitals-sign-screen
 (fn [_ _]
   {:fetch {:base-url "http://samurai.demo/"
            :uri "/observations.json"
            :success :on-vitals-sign-load
            }}))
