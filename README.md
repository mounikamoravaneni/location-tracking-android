# location-tracking-android
# Location Tracking App

## Overview
This is a location tracking app that allows users to record sessions, track routes, calculate distance and duration, view session history, export sessions as CSV, and fetch toll information via TollTally API. The app supports **light and dark modes** and continues tracking in the background, even if the app is killed from recent apps.

---

## Features

- Start and Stop location tracking
- Background tracking with live notification showing duration and distance
- Store session data in **Room Database**
  - Date
  - Start Time
  - End Time
  - Duration
  - Distance
  - Route
- View session history and map view of recorded routes
- Export session data as **CSV** and share via WhatsApp or other apps
- Fetch toll cost using **TollTally API** (currently returning server errors)
- Dark and light mode support

---

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/mounikamoravaneni/location-tracking-android
   cd location-tracking-app
