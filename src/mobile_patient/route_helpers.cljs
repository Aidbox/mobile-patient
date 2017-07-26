(ns mobile-patient.route-helpers
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))

;; NAVIGATION BUTTONS

(defn add-chat-button [navigation]
  [ui/touchable-highlight {:on-press #(navigation.navigate "Contacts")
                           :style {:margin-right 20}
                           :underlay-color color/grey}
   [ui/icon {:name "add-circle" :size 36 :color color/pink}]])

;; NAVIGATION ROUTES

(defn menu-button [navigation]
  [ui/touchable-highlight {:style {:margin-left 0}
                           :on-press #(navigation.navigate "DrawerOpen")
                           :underlay-color color/grey}
   [ui/icon {:name "menu" :size 24 :color :black}]])



(defn header [props]
  (r/create-element ui/View
                    #js{:style #js{:backgroundColor "#ffffff"}}
                    (r/create-element ui/Header props)))



(defn stack-navigator [routes]
  (r/reactify-component
   (ui/StackNavigator (clj->js routes)
                      (clj->js {;;:initialRouteName "Contacts" ;;for dev
                                :header header
                                :headerTitleStyle {:fontWeight :normal}}))))

(defn stack-navigator-back-button [props]
  (r/as-element
   [ui/touchable-highlight {:on-press #(props.navigation.goBack nil)
                            :style {:margin-left 5}
                            :underlay-color color/grey}
    [ui/icon {:name "chevron-left" :size 36}]]))




(defn drawer-nav-opts
  ([title]
   (drawer-nav-opts title nil))

  ([title header-right]
   (drawer-nav-opts title header-right :menu))

  ([title header-right left-button-type]
   (fn [props]
     #js{:title title
         :header header
         :headerLeft (if (= left-button-type :menu)
                       (r/as-element [menu-button props.navigation])
                       (stack-navigator-back-button props))
         :headerStyle #js{:padding 0
                          :margin 20
                          :borderBottomWidth 1
                          :borderBottomColor "#ddd"
                          :elevation 0
                          :shadowOffset #js {:height 0}
                          :shadowColor "red"
                          :height 100
                          }
         :headerTitleStyle #js{:textAlign "center"
                               :paddingLeft 10
                               :fontSize 19
                               :fontWeight "bold"
                               :color color/pink}
         :headerRight (when header-right (r/as-element [header-right props.navigation]))})))
