(ns mobile-patient.ui
  (:require [reagent.core :as r]
            [mobile-patient.color :as color]))


(def react-navigation (js/require "react-navigation"))

(def Header (.-Header react-navigation))
(def NavigationActions (.-NavigationActions react-navigation))
(def StackNavigator (.-StackNavigator react-navigation))
(def TabNavigator (.-TabNavigator react-navigation))
(def DrawerNavigator (.-DrawerNavigator react-navigation))

(def ReactNative (js/require "react-native"))

(def Dimensions (.-Dimensions ReactNative))
(def Platform (.-Platform ReactNative))

(def date-picker (.-DatePickerAndroid ReactNative))
(def date-picker-ios (r/adapt-react-class (.-DatePickerIOS ReactNative)))
(def View (.-View ReactNative))
(def view (r/adapt-react-class (.-View ReactNative)))
(def modal (r/adapt-react-class (.-Modal ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def input (r/adapt-react-class (.-TextInput ReactNative)))
(def Switch (.-Switch ReactNative))
(def switch (r/adapt-react-class (.-Switch ReactNative)))
(def rn-button (r/adapt-react-class (.-Button ReactNative)))
(def list-view (r/adapt-react-class (.-ListView ReactNative)))
(def virtualized-list (r/adapt-react-class (.-VirtualizedList ReactNative)))
(def flat-list (r/adapt-react-class (.-FlatList ReactNative)))
(def section-list (r/adapt-react-class (.-SectionList ReactNative)))
(def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))
(def activity-indicator (r/adapt-react-class (.-ActivityIndicator ReactNative)))

(def ART (.-ART ReactNative))

(def surface (r/adapt-react-class (.-Surface ART)))
(def group (r/adapt-react-class (.-Group ART)))
(def shape (r/adapt-react-class (.-Shape ART)))

(def picker (r/adapt-react-class (.-Picker ReactNative)))
(def picker-item (r/adapt-react-class (.-Picker.Item ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity ReactNative)))

(defn alert [title msg]
  (.alert (.-Alert ReactNative) title msg   #js[#js{:text "Close"}]))

(def Icon (js/require "react-native-vector-icons/MaterialIcons"))
(def icon (r/adapt-react-class (aget Icon "default")))

(def ReactNativeTableviewSimple (js/require "react-native-tableview-simple"))
(def table-view (r/adapt-react-class (.-TableView ReactNativeTableviewSimple)))
(def section (r/adapt-react-class (.-Section ReactNativeTableviewSimple)))
(def cell (r/adapt-react-class (.-Cell ReactNativeTableviewSimple)))

(def victory-native (js/require "victory-native"))
(def victory-axis (r/adapt-react-class (.-VictoryAxis victory-native)))
(def victory-bar (r/adapt-react-class (.-VictoryBar victory-native)))
(def victory-chart (r/adapt-react-class (.-VictoryChart victory-native)))
(def victory-line (r/adapt-react-class (.-VictoryLine victory-native)))
(def victory-scatter (r/adapt-react-class (.-VictoryScatter victory-native)))
(def victory-group (r/adapt-react-class (.-VictoryGroup victory-native)))


(defn button [{:keys [title on-press] :as props}]
  [touchable-highlight {:style {:background-color color/light-grey
                                :border-width 1
                                :border-color color/grey
                                :padding 10
                                :margin-top 20
                                }
                        :on-press on-press}
   [text {:style {:color color/grey
                  :font-weight :bold
                  :text-align :center}}
    title]])

(defn link [{:keys [title on-press] :as props}]
  [touchable-highlight {:style {:padding 10}
                        :on-press on-press}
   [text {:style {:color color/grey
                  ;;:font-weight :bold
                  :text-align :center}}
    title]])

(defn shadow-box [& childs]
  (into [view {:style {:flex 1
                       :margin 10
                       :padding 20
                       :background-color :white
                       :border-radius 10
                       :elevation 20 ;; Android specific
                       ;; shadows are not supported on RN Android
                       ;; adjust next lines for iOS
                       :shadow-offset {:width 0 :height 2}
                       :shadow-color :black
                       :shadow-opacity 0.8
                       :shadow-radius 20
                       }}]
        childs))

(defn screen-activity-indicator []
  [view {:style {:flex 1
                 :justify-content :center
                 :background-color :white}}
   [activity-indicator]])

(defn show-remote-data [remote-data render-fn]
  (let [data-key (->> remote-data
                      keys
                      (map name)
                      (filter #(clojure.string/ends-with? % "-data"))
                      first
                      keyword)]
    (case (:status remote-data)
      :not-asked [text]
      :loading [screen-activity-indicator]
      :failure [text (get remote-data data-key)]
      :succeed (render-fn (get remote-data data-key)))))

(defn avatar
  ([img-url]
   (avatar img-url 38))
  ([img-uri size]
   (if img-uri
     [image {:style {:width size
                     :height size
                     :align-self :center
                     }
             :source {:uri img-uri}}]
     [view {:style {:width size
                    :height size
                    :border-radius 19
                    :background-color "#9e9e9e"
                    :align-self :center}}])))

(defn badge [txt]
  [view {:style {:border-width 1
                    :border-radius 5
                    ;;:border-color color/pink
                    :height 22
                    :padding-left 4
                    :padding-right 4
                    :margin-left 4
                    :margin-right 4
                    }}
   [text {:style { ;;:color color/pink
                     :font-size 14
                     :font-weight :bold}}
    txt]])
