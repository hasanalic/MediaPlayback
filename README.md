# MediaPlayback

A simple Android sample project that shows how to build a clean media playback feature with:

- a custom playback slider
- playback controls
- a dedicated `MediaPlaybackManager`
- a `ViewModel` that transforms playback data into UI state
- a clean screen structure with `Screen` and `ScreenContent`

## Preview

This project demonstrates a media playback screen built with a focus on clean architecture and state management.

## What’s inside

- **Custom Slider**  
  A custom playback slider with drag and seek handling

- **Playback Controller**  
  Play, pause, backward, and forward controls

- **MediaPlaybackManager**  
  A dedicated manager that owns ExoPlayer and handles playback operations

- **ViewModel**  
  Combines playback-related data into a UI-friendly state model

- **Clean UI Structure**  
  Keeps the UI layer simple and focused on rendering state and sending user actions

## Architecture Idea

The main goal of this sample is to keep responsibilities clear:

- the **UI** renders state and sends actions
- the **ViewModel** prepares UI state and handles user intent
- the **MediaPlaybackManager** owns ExoPlayer and playback logic

## Tech

- Kotlin
- Jetpack Compose
- ViewModel
- StateFlow
- ExoPlayer
- Hilt
