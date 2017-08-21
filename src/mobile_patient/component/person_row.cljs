(ns mobile-patient.component.person-row
  (:require [reagent.core :as r]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.model.patient :as patient-model]))

(defn person-row-component [{:keys [item index]}
                            navigation
                            on-press
                            icon-name]

  (let [chat-name (patient-model/get-official-name item)
        unread (get-in item [:chat :unread])]

    [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                     :border-width 1
                                     :border-top-width (if (zero? index) 0 1)
                                     :border-bottom-width 0
                                     :padding 10
                                     :background-color "#ffffff"}
                             :on-press #(on-press item navigation chat-name)}
     [ui/view {:style {:flex-direction :row}}
      [ui/view {:style {:flex-direction :row
                        :flex 0.9
                        :justify-content :flex-start
                        :align-items :center
                        :padding-left 15
                        :padding-right 10
                        }}
       [ui/avatar (str "data:image/png;base64," (patient-model/get-photo item))
        20]
       [ui/text {:style {:margin-left 15
                         :margin-right 15
                         :text-align "left"
                         :font-size 16
                         :color "#333"
                         :font-weight :bold}} chat-name]
       (if unread
         [ui/badge (str unread " new")])
       ]
      [ui/icon {:style {:flex 0.1}
                :name icon-name :size 30 :color "#FF485C"}]
      ]
     ]))
