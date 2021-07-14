# Mental Health Tracker

The Mental Health Tracker (MHT) is an Android app that provides a platform for tracking your mood.
It's my first contact with Android and Kotlin and helped me understand the basics of the platform and language :)
You can choose a rating from a selection of options and leave notes for each day, either to just let off some steam or keep track of what might have caused your mood.
The options to choose from can be modified to your liking, e.g. the text of the rating, the rating value itself or the color that will be shown in the calendar.
You can view all your ratings in a calendar view, the history, were each entry is marked with a color. This is the tool that should aid you in tracking and possibly improving your mental health.
E.g. if you always leave bad ratings on Mondays you can recognize these patterns and do something about it.

# Installation

To install the app on your device you can download the project and load it into Android Studio, where you can build it and deploy it to your device.
An .apk will be available in the future.

# External libraries

The history view and the color picker make use of external libraries/modules.
The calendar widget/view (https://github.com/SpongeBobSun/mCalendarView) is provided by SpongeBobSun (https://github.com/SpongeBobSun).
The color picker dialog makes use of Jaewoong Eum's (https://github.com/skydoves) ColorPickerView (https://github.com/skydoves/ColorPickerView).
For the implementation the example code for creating and extracting data from the dialog has been adapted and used.

