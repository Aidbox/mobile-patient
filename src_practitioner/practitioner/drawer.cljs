(ns practitioner.drawer
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))

(defn Item
  ([title on-press]
   (Item title on-press nil))
  ([title on-press style-props]
   [ui/touchable-highlight {:style (merge {:height 40
                                           :justify-content :center}
                                          style-props)
                            :on-press on-press
                            :underlay-color color/grey}
    [ui/text {:style {:margin-left 40
                      :font-size 18}} title]]))

(defn Drawer [props]
  (let [username @(subscribe [:get-in [:user :id]])
        patient @(subscribe [:get-in [:patient]])]
    [ui/view {:style {:background-color "#f4f4f4"
                      :flex 1}}
     [ui/view {:style {:background-color "white"
                       :height 120
                       :flex-direction :row
                       :justify-content :flex-start
                       :padding-left 30}}
      [ui/view {:style {:width 36
                        :height 36
                        :border-radius 18
                        :background-color "#9e9e9e"
                        :align-self :center}}]
      [ui/text {:style {:font-size 18
                        :margin-left 20
                        :color "#333"
                        :align-self :center}} username]]

     [ui/view {:style {:flex 1
                       :margin-top 30}}
      [Item "Select patient" #(props.navigation.navigate "Patients")]

      ;; patients's specific items
      (when @(subscribe [:patient-ref])
        [ui/view
         [ui/text {:style {:margin-left 35 :margin-top 20 :font-size 18 :color color/pink}}
           (str @(subscribe [:get-in [:patient-data :username]])  "'s:")]
         [Item "Medications" #(props.navigation.navigate "Medications")]
         [Item "Vitals Signs" #(props.navigation.navigate "Vitals Signs")]
         [Item "Chats" #(props.navigation.navigate "Chats")]
         ])
      ]
     [Item "Logout" #(dispatch [:initialize-db])]
     [Item "About app" #(props.navigation.navigate "About App") {:margin-bottom 60}]
     ])
  )
