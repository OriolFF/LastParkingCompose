---
trigger: manual
---

# Main goal
This app is about store the las location of the car. It has only one screen that shows the last location of the car, if it exists.
When the user open the app, if theres a recent location stored, it will show it-
The screen has a picture of the last location. At one side it has a static map with the last location.
Under the picture and the map, an editable field with the last location address.
And under this field another field where the user can add some comment.
It will have a fab button to create a new location. 
When the user creates a new location, the app will start to ask to gps for location, showing a progress indicator 
that shows the gps accuracy. When the accuracy is good enough it will ask for the address.
The address will be shouwn in the editable field and the comment field will be empty.
The image will be replaced by a button to take a picture using an intent.
The static map is replaced by a dynamic map that shows the current user location.

# Architecture 
- Kotlin
- MVI
- Jetpack Compose
- Room
- Clean Architecture
- Coroutines and Flow
- Hilt for dependency injection
Use separation of responsabilities, so each layer should have its own responsibility.
- Data layer: 
  - Room for local database
  - Ktor for network requests
  - Repository pattern to abstract data sources
- Domain
  - Use cases for business logic
- Presentation
  - ViewModel for UI state management
  - Screen 
  - Actions
  - State
  - Events
    
## General indication 
- Don't add comments unless is very necessary.
- Put the strings in the strings.xml file.
- Use english as default language.
