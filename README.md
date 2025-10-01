# Umamuse - Horse Racing Game

## Overview

Umamuse is an Android mobile game about horse racing, combining interactive racing gameplay with betting mechanics. The application follows a multi-activity architecture with shared services and utilities for functionality like user preferences and background music.

## Activities

### 1. LoginActivity
- **Purpose**: Entry point for user authentication
- **Main Functions**:
  - User login validation
  - Navigation to OpeningActivity after successful login
  - Remembering login credentials
  - Username and password validation

### 2. OpeningActivity
- **Purpose**: Plays an introductory video before entering the main game
- **Main Functions**:
  - Fullscreen video playback with proper scaling
  - Landscape orientation enforcement
  - "Troll" skip button that moves randomly when clicked
  - Automatic transition to RaceActivity after video completion
  - Video playback controls and event handling

### 3. RaceActivity
- **Purpose**: Main game screen where horse races take place
- **Main Functions**:
  - Horse selection and race preparation
  - Race animation with realistic horse movement
  - Betting system integration
  - Background track for seamless scrolling
  - Balance display and management
  - Navigation to BetActivity and DepositMoneyActivity
  - Race results dialog with winnings calculation
  - Lap counting and race completion logic
  - Background music playback with mute toggle

### 4. BetActivity
- **Purpose**: Interface for placing bets on horses
- **Main Functions**:
  - Displaying available horses with ViewPager2
  - Setting bet amounts for horses
  - Calculating odds for potential winnings
  - Managing user balance for betting
  - Background music playback with mute toggle
  - Summarizing current bets and potential returns

### 5. DepositMoneyActivity
- **Purpose**: Interface for adding funds to user balance
- **Main Functions**:
  - Multiple package selection (Return Pack, Comeback Pack, God of Bet Pack)
  - Package purchase confirmation
  - Balance update and display
  - Background music playback with mute toggle
  - Navigation back to RaceActivity

### 6. MainActivity
- **Purpose**: Potentially used for future functionality or initial app setup

## Models

### 1. Horse
- **Purpose**: Represents a horse in the racing game
- **Properties**:
  - id: Unique identifier
  - name: Horse name
  - imageRes: Resource ID for horse image
  - speed: Current speed value
  - isFinished: Race completion status
  - isForward: Movement direction
  - lastDirectionChange: Timestamp for last direction change
  - directionInterval: Time between direction changes

### 2. HorseBet
- **Purpose**: Represents a bet placed on a horse
- **Properties**:
  - horse: Reference to Horse object
  - odds: Multiplier for potential winnings
  - betAmount: Amount bet on this horse
  - newBetAmount: New additional bet being placed

## Repositories

### 1. HorseRepository
- **Purpose**: Manages horse data across activities
- **Functions**:
  - getAllHorses(): Returns list of all available horses
  - getCurrentRaceHorses(): Gets horses selected for current race
  - setCurrentRaceHorses(): Sets horses for current race

## Adapters

### 1. HorseBetAdapter
- **Purpose**: Connects horse betting data to ViewPager2 in BetActivity
- **Functions**:
  - Displays horse information with images
  - Handles bet amount input
  - Shows betting odds
  - Updates HorseBet objects with user inputs

## Services

### 1. BackgroundMusicService
- **Purpose**: Manages background music playback across all activities
- **Functions**:
  - Plays, pauses, and shuffles background tracks
  - Maintains consistent music between activity transitions
  - Controls volume level (30% by default)
  - Handles mute functionality
  - Remembers mute preference using SharedPreferences
  - Automatically plays the next track when one finishes

## Utilities

### 1. UserPreferences
- **Purpose**: Manages user data persistence and retrieval
- **Functions**:
  - getUserBalance(): Gets current user balance
  - setUserBalance(): Sets user balance to specific value
  - addToUserBalance(): Adds amount to current balance
  - subtractFromUserBalance(): Reduces balance by specified amount

### 2. MusicManager
- **Purpose**: Helper for connecting activities to the BackgroundMusicService
- **Functions**:
  - Binds/unbinds activities to music service
  - Controls music playback and mute state
  - Updates mute button UI state
  - Handles service connection lifecycle

## User Interface Components

### 1. Activity Layouts
- **activity_race.xml**: Main race screen layout with lanes, backgrounds, and control buttons
- **activity_bet.xml**: Betting interface with ViewPager2 for horse selection
- **deposit_money.xml**: Money deposit screen with package options
- **activity_opening.xml**: Video player layout for opening sequence
- **dialog_race_result.xml**: Race results popup with winnings calculation

### 2. Custom Drawables
- **btn_sound_selector.xml**: Toggle button states for mute/unmute
- **rounded_bg.xml**: Rounded background for UI elements

## Game Mechanics

### 1. Race System
- Horses move at variable speeds with natural animation
- Direction changes create realistic movement patterns
- Lap counting with finish line appearance
- Winner selection with visual emphasis

### 2. Betting System
- Odds calculated for each horse
- Multiple bet support on different horses
- Bet accumulation within a race
- Winnings calculation based on odds and bet amount
- Balance management integration

### 3. Monetization
- Virtual currency system with multiple package options
- Balance persistence across sessions
- Clear display of current balance

## Audio System
- Background music with three tracks (bakushin.mp3, chiyo.mp3, mambo.mp3)
- Track shuffling for variety
- Volume control at 30% by default
- Mute toggle with preference persistence
- Seamless playback across activity transitions

## User Experience Features
- Fullscreen immersive video opening
- Interactive "troll" button for engagement
- Landscape orientation for optimal race viewing
- Animated UI elements for better feedback
- Toast notifications for important events
- Consistent audio across the application

## Technical Implementation
- Activity lifecycle management
- Service binding for background processes
- SharedPreferences for data persistence
- Animation systems for UI and game elements
- Media playback optimization
- Resource management for audio and visual assets