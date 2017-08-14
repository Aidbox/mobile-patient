(ns patient.drawer
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
  (let [username @(subscribe [:user-name])
        user-picture @(subscribe [:user-picture])]
    [ui/view {:style {:background-color "#f4f4f4"
                      :flex 1}}
     [ui/view {:style {:background-color "white"
                       :height 100
                       :flex-direction :row
                       :justify-content :flex-start
                       :padding-left 30}}

      [ui/avatar (str "data:image/png;base64," user-picture)]
      [ui/text {:style {:font-size 18
                        :margin-left 10
                        :color "#333"
                        :align-self :center}} username]]
     [ui/view {:style {:flex 1
                       :margin-top 30}}
      [Item "Medications" #(props.navigation.navigate "Medications")]
      [Item "Vitals Signs" #(props.navigation.navigate "Vitals Signs")]
      [Item "Chats" #(props.navigation.navigate "Chats")]
      [Item "Settings" #(props.navigation.navigate "Settings")]]

     [Item "Logout" #(do (dispatch [:initialize-db])
                         (dispatch [:set-current-screen :login]))]
     [Item "About app" #(props.navigation.navigate "About App") {:margin-bottom 60}]]))
