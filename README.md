# DashCam

This project is my own implementation of a dashcam that leverages my limited knowledge of hardware and software. All in all, it would have been cheaper to just buy one from Amazon, but I don’t regret spending the extra money and the many hours debugging. This was a challenging project due to the fact that so variables had to be taken into consideration to ensure it would work properly. Solutions I thought were going to work, just flat out failed. Here are some of the problems I encountered:
-	Heat sensor to detect driver to know when to turn off Raspberry Pi failed because the range was too limited.
-	Utilized a portable battery so that when the car turns off, the dashcam is running off the portable battery, but this failed because there is a split second where it switches from car power to only battery resulting in the Raspberry Pi turning off.
-	Attempted to send live feed of front and back cameras to android app, but due to software/hardware limitations, the cameras could not be used concurrently to send live feed and facial recognition on the front camera.

Given these limitations, I decided that the auto-turnoff feature of the Raspberry Pi was out of the scope of this project and that sending live feed of both cameras was unnecessary.

Project Scope:
This purpose of this project is to create a custom implementation of a dashcam that utilities two cameras. The first camera is facing the driver and its purpose is to determine if the owner (picture of owner is sent to Raspberry Pi via android app on initial setup) is driving the car. The front facing camera saves footage just like a regular dashcam and saved footage is neatly saved in file directory for easy viewing in the future.


## Table of contents
* [App Layout](#app-layout)
* [Features](#features)
* [Tools](#tools)
* [Download](#download)

## App Layout

TO DO | TO DO | TO DO | TO DO
:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:


## Features

* TO DO
	
## Tools
* Android Studio
* Java
* Python
* Raspberry Pi

## Download
You can download the apk to test out the app (TO DO).
