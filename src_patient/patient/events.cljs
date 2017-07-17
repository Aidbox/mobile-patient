(ns patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [mobile-patient.ui :as ui]
            [clojure.string :as str]
            [mobile-patient.lib.helper :as h]))


(reg-event-fx
 :boot
 (fn [_ [_ user-id]]
   {:async-flow
    {:first-dispatch [:do-load-user user-id]
     :rules
     [
      {:when     :seen?
       :events   :success-load-user
       :dispatch-n '([:do-load-patient] [:do-load-all-users])}

      {:when     :seen-both?
       :events   [:success-load-patient :success-load-all-users]
       :dispatch [:do-check-is-set-demographics]}

      {:when :seen-any-of?
       :events [:success-submit-demographics]
       :dispatch [:do-load-medication-statements]}

      {:when :seen?
       :events :success-load-medication-statements
       :dispatch [:set-current-screen :main]}

      ]}}))

;;
;; load-patient
;;
(reg-event-fx
 :do-load-patient
 (fn [_ [_]]
   {:fetch {:uri (str "/Patient/" @(subscribe [:user-ref]))
            :success :success-load-patient
            :opts {:method "GET"}}}))

(reg-event-db
 :success-load-patient
 (fn [db [_ patient-data]]
   (assoc db :patient-data patient-data)))

;;
;; check-is-set-demographics
;;
(reg-event-fx
 :do-check-is-set-demographics
 (fn [{:keys [db]} [_]]
   (let [patient-data (:patient-data db)]
     (if-not (h/contains-many? patient-data :gender :birthDate :address)
       {:dispatch [:set-current-screen :demographics]}
       {:dispatch [:do-load-medication-statements]}
       ))))

;;
;; submit-demographics
;;
(reg-event-fx
 :do-submit-demographics
 (fn [_ [_ form-data]]
   (let [user @(subscribe [:get-in [:user]])
         patient-data @(subscribe [:get-in [:patient-data]])]
     {:fetch {:uri (str "/" (get-in user [:ref :resourceType ])
                        "/" (get-in user [:ref :id ]))
              :success :success-submit-demographics
              :opts {:method "PUT"
                     :headers {"content-type" "application/json"}
                     :body (js/JSON.stringify
                            (clj->js
                             (merge patient-data
                                    {:gender (:sex form-data)
                                     :birthDate (:birthday form-data)
                                     :address [{:use "home"
                                                :type "postal"
                                                :text (:address form-data)
                                                }]})))}}})))
(reg-event-db
 :success-submit-demographics
 (fn [db [_ patient-data]]
   (assoc db :patient-data patient-data)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

