# Countries Quartet --- Android Educational Card Game

## 1. Project Goal

Develop an Android educational card game based on the classic
**Quartet** card-game mechanic.

Theme: **Countries of the World**

The game contains: - 52 country cards - 13 quartets - 4 countries per
quartet - 4 players - 1 human player - 3 AI players

The application should eventually be suitable for publication on Google
Play.

Primary goals: - Simple rules - Fast gameplay - Educational value -
Attractive country cards - Child-friendly interface - Fully playable
offline - No backend - No accounts - No Internet requirement

The initial target audience is approximately ages 7--14, but the game
should also be enjoyable for adults interested in geography.

## 1A. Product Name and Branding

The official application name for Version 1 is:

**Country Quartet**

Use this name consistently in:
- Android application label
- Main menu
- Splash screen
- Game title
- Documentation
- Release configuration
- Google Play preparation

Recommended package/application ID:

```text
com.countryquartet.game
```

Recommended short marketing description:

**Collect countries, complete quartets, and learn geography!**

The name **Country Quartet** is the current approved product name. Do NOT rename the application, change the package/application ID, or introduce an alternative brand name without explicit user approval.

## 2. Technology

Use: - Android Studio - Kotlin - Jetpack Compose - Material 3 - Gradle -
ViewModel - StateFlow - Kotlin Coroutines where appropriate - Navigation
Compose - Local JSON for country/quartet data - DataStore for
settings/statistics when persistence is implemented

Minimum Android: Android 8.0 / API 26.

Use the current stable target SDK available in the development
environment.

The game must work completely offline.

Do NOT introduce: - Firebase - Backend - Authentication - User
accounts - Cloud database - Multiplayer server - Ads in the first
version - Analytics in the first version - Unnecessary third-party
frameworks

Keep dependencies minimal.

## 3. Game Structure

The complete deck contains **52 cards**, divided into **13 quartets**,
with **4 countries per quartet**.

There are four players: - Human - AI Player 1 - AI Player 2 - AI Player
3

At the beginning of the game all 52 cards are shuffled and distributed
equally. Each player receives **13 cards**.

## 4. Quartet Concept

Every country card belongs to exactly one quartet.

Example --- Nordic Countries: - Sweden - Norway - Denmark - Finland

Every card displays all four countries belonging to its quartet.

For example, the Sweden card shows:

**NORDIC COUNTRIES**

**SWEDEN**

Capital: Stockholm\
Language: Swedish\
Currency: Swedish krona

**Collect all 4:** - ★ SWEDEN - ○ Norway - ○ Denmark - ○ Finland

The current country must be visually highlighted. When the player owns
Sweden, they can immediately see that they still need Norway, Denmark,
and Finland.

## 5. Initial 13 Quartets

### Quartet 1 --- Nordic Countries

-   Sweden
-   Norway
-   Denmark
-   Finland

### Quartet 2 --- Southern Europe

-   Italy
-   Spain
-   Portugal
-   Greece

### Quartet 3 --- Central Europe

-   Germany
-   Austria
-   Switzerland
-   Czechia

### Quartet 4 --- Eastern Europe

-   Poland
-   Ukraine
-   Romania
-   Hungary

### Quartet 5 --- Middle East

-   Israel
-   Jordan
-   Lebanon
-   Saudi Arabia

### Quartet 6 --- East Asia

-   Japan
-   South Korea
-   China
-   Mongolia

### Quartet 7 --- South Asia

-   India
-   Pakistan
-   Bangladesh
-   Sri Lanka

### Quartet 8 --- Southeast Asia

-   Thailand
-   Vietnam
-   Malaysia
-   Indonesia

### Quartet 9 --- North Africa

-   Egypt
-   Morocco
-   Algeria
-   Tunisia

### Quartet 10 --- East Africa

-   Kenya
-   Tanzania
-   Ethiopia
-   Uganda

### Quartet 11 --- North America

-   United States
-   Canada
-   Mexico
-   Cuba

### Quartet 12 --- South America

-   Brazil
-   Argentina
-   Chile
-   Peru

### Quartet 13 --- Oceania

-   Australia
-   New Zealand
-   Papua New Guinea
-   Fiji

These are initial proposed groups. Before final content release, review
all geographic groupings for educational accuracy. Do not silently
change groups. If a grouping is questionable, document it and request
manual review.

## 6. Country Information

Each country should contain: - Stable ID - English name - Quartet ID -
Capital - Primary/official language - Currency - Flag asset - Short fun
fact

Suggested Kotlin model:

``` kotlin
data class Country(
    val id: String,
    val name: String,
    val quartetId: String,
    val capital: String,
    val language: String,
    val currency: String,
    val flagAsset: String,
    val funFact: String
)
```

Quartet:

``` kotlin
data class Quartet(
    val id: String,
    val name: String,
    val countryIds: List<String>
)
```

Every quartet MUST contain exactly four unique countries. Every country
MUST belong to exactly one quartet. Use IDs internally; do not use
display names as identifiers.

## 7. Data Storage

Store content separately from game logic.

Suggested files:

``` text
app/src/main/assets/countries.json
app/src/main/assets/quartets.json
```

Do NOT hardcode country information inside Compose screens, the game
engine, AI, or ViewModels.

The architecture should allow future expansion with additional packs
without rewriting the game engine.

## 8. Basic Game Rules

The objective is to collect more complete quartets than the other
players.

A quartet is complete when one player owns all four country cards
belonging to that quartet.

Completed quartets are removed from the player's active hand and placed
in their completed collection. Each completed quartet counts as 1 point.
There are 13 total points available.

## 9. Turn Rules

During a turn, a player: 1. Chooses a quartet represented in their
current hand. 2. Chooses one missing country from that quartet. 3.
Chooses another player. 4. Requests that country card.

A player may only request a country from a quartet for which they
currently own at least one card.

A player cannot request: - A country already owned by themselves - A
country outside their represented quartets - A country already belonging
to a completed quartet

## 10. Successful Request

If the selected opponent owns the requested country: 1. Transfer the
card to the requesting player. 2. Check whether a quartet has been
completed. 3. If completed, move the four cards into completedQuartets.
4. Increase score. 5. Display completion feedback. 6. The requesting
player continues their turn.

## 11. Failed Request

If the selected opponent does NOT own the requested country, display a
short failure message and end the current player's turn. The next player
begins their turn.

## 12. Completed Quartet

Whenever a player receives the fourth card of a quartet: - Display
"QUARTET COMPLETED!" - Show the quartet name and all four countries -
Remove the cards from the active hand - Add the quartet to
completedQuartets - Increase score by 1

The player continues their turn if the quartet was completed as the
result of a successful request.

## 13. AI Players

The first release contains three AI opponents.

Initial AI decision process: 1. Examine its cards. 2. Group cards by
quartet. 3. Prefer quartets where it already owns the most cards. 4.
Choose one missing country. 5. Choose an opponent. 6. Request the
country. 7. Process the request using the SAME game engine as the human
player.

Never duplicate game rules inside AI.

## 14. Future AI Design

Structure AI so smarter strategies can be introduced later. Future AI
may remember which player previously owned a card, which player denied
having a card, card ownership changes, and which opponents are close to
completing quartets.

Do NOT implement sophisticated inference for the initial MVP. Initial AI
may select opponents randomly.

## 15. End of Game

The game ends when all 13 quartets have been completed.

Display final standings for Human and all three AI players, highlight
the winner, and support draws.

Provide: - Play Again - Main Menu

## 16. Application Screens

### Splash Screen

Display game logo/title. Do not add an artificial loading delay.

### Main Menu

-   PLAY
-   HOW TO PLAY
-   COUNTRIES
-   SETTINGS

### Game Screen

Display: - Current turn - Human cards - Human score - Opponent scores -
Completed quartets - Selected card/quartet - Missing countries -
Opponent selection - Current action/status

### Countries Screen

Allow educational browsing of all 13 quartets and all 52 countries.
Selecting a country displays its full card.

### How to Play

Explain visually and simply: 1. You have country cards. 2. Every card
belongs to a group of four. 3. Ask opponents for missing countries. 4.
Collect all four countries. 5. Complete more quartets than your
opponents.

### Settings

Initial options: - Sound ON/OFF - Animation ON/OFF - Reset statistics

Architecture may allow future language selection.

## 17. Country Card Design

Each full card should show: - Flag - Country name - Quartet name -
Capital - Language - Currency - Fun fact - "COLLECT ALL 4" with the four
country names

The current card's country must be clearly highlighted.

## 18. Card Components

Create reusable Compose components: - `CountryCard()` -
`CompactCountryCard()` - `CompletedQuartetCard()`

CountryCard should support states such as Normal, Selected, Disabled,
Owned, and Requested.

Do not duplicate card layouts across screens.

## 19. Architecture

Suggested packages:

``` text
com.countriesquartet.game
data/
model/
game/
ai/
repository/
ui/
ui/components/
ui/screens/
navigation/
viewmodel/
```

Core game rules must be pure Kotlin and MUST NOT depend on Activity,
Fragment, Compose, Android View, or Context unless absolutely required
for non-game infrastructure.

Game logic should be testable on JVM without an emulator.

## 20. Game State

Use controlled central state.

Example:

``` kotlin
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val completedQuartetsCount: Int,
    val status: GameStatus,
    val winnerIds: List<String>
)
```

Example player:

``` kotlin
data class Player(
    val id: String,
    val name: String,
    val isHuman: Boolean,
    val cards: List<String>,
    val completedQuartets: List<String>
)
```

Avoid global mutable state. Expose game state to Compose through
ViewModel + StateFlow.

## 21. Testing Requirements

### Dataset tests

Verify: - Exactly 52 countries - Exactly 13 quartets - Exactly 4
countries per quartet - No duplicate country IDs - Every country belongs
to exactly one quartet - Every referenced country exists

### Deck tests

Verify: - Deck contains 52 cards - No duplicates

### Deal tests

Verify: - Every player receives exactly 13 cards - Total distributed
cards = 52 - No card appears twice

### Request tests

Test successful requests, failed requests, invalid country requests,
requesting an owned card, requesting from an invalid quartet, and
requesting a completed card.

### Quartet tests

Test quartet detection, removal from active hand, score increase, and
completed-quartet storage.

### Turn tests

Test that successful requests keep the turn and failed requests advance
the turn.

### End-game tests

Test all 13 quartets completed, winner detection, and draw detection.

### AI tests

Verify AI never makes illegal requests, never requests its own card, and
can finish a game.

## 22. Game Simulation Test

Create automated AI-vs-AI-vs-AI-vs-AI simulations.

Run at least 100 complete games and verify: - No crashes - No infinite
loops - No duplicate cards - No lost cards - Exactly 13 quartets
completed - Total card ownership remains consistent

This must be done before connecting the game engine to the UI.

# DEVELOPMENT PHASES

Development must be incremental. Do NOT implement the entire application
at once.

There are exactly **10 development phases**.

## PHASE 1 --- Android Project Skeleton

Goal: create a clean working Android application.

Implement: - Android project - Kotlin - Jetpack Compose - Material 3 -
Navigation Compose - Package structure - Placeholder Main Menu, Game,
Countries, How to Play, and Settings screens

Acceptance criteria: - Project compiles - APK builds - App launches -
Navigation works - No crashes

Run:

``` text
gradlew.bat assembleDebug
```

STOP after Phase 1. Do not start Phase 2 automatically.

## PHASE 2 --- Countries and Quartets

Goal: create the complete data layer.

Implement: - Country model - Quartet model - JSON loader - 52
countries - 13 quartets - Data validation

Acceptance criteria: - 52 countries - 13 quartets - 4 countries per
quartet - All validation tests pass

Run:

``` text
gradlew.bat test
gradlew.bat assembleDebug
```

STOP after Phase 2.

## PHASE 3 --- Core Game Engine

Implement: - Deck creation - Shuffle - Deal - Player state - Turns -
Requests - Card transfer - Failed requests - Quartet detection -
Completed quartets - Score - End-game detection - Winner detection -
Draw detection

Acceptance criteria: - Game engine works entirely through unit tests -
No Compose dependency - No Android UI dependency

STOP after Phase 3.

## PHASE 4 --- Computer AI

Implement: - AI decision making - Quartet selection - Missing-country
selection - Opponent selection - Legal request validation - AI-vs-AI
simulation

Run at least 100 simulated complete games.

Acceptance criteria: - Every simulated game terminates - Every game
produces exactly 13 completed quartets - No invalid game state occurs

STOP after Phase 4.

## PHASE 5 --- Playable Game

Connect Game Engine + ViewModel + Compose UI.

Implement: - Human hand - Card selection - Missing-country selection -
Opponent selection - Ask action - AI turns - Scores - Completed
quartets - Game-over screen - Play Again

Acceptance criteria: - Human can play a complete game against three AI
opponents from beginning to end

This is the MVP milestone. Functionality first; do not focus heavily on
animations yet.

STOP after Phase 5 and request manual gameplay testing.

## PHASE 6 --- Professional Card Design

Implement: - Full CountryCard - CompactCountryCard - Flag display -
Country information - Quartet information - Selection state - Completed
quartet view

Optimize for portrait phones.

Acceptance criteria: - All information remains readable on common
Android phone sizes - Cards clearly show which three additional
countries are needed

STOP after Phase 6.

## PHASE 7 --- UX and Animations

Implement appropriate: - Card selection animations - Card transfer
feedback - Successful request feedback - Failed request feedback -
Quartet completion animation - AI thinking delay - Turn transitions -
Game-over presentation

Keep animations short and respect the animation-disabled setting.

STOP after Phase 7.

## PHASE 8 --- Content Verification

Review all 52 countries and verify: - Country name - Capital -
Language - Currency - Flag - Fun fact - Quartet membership

Do not invent uncertain information.

Generate a report containing: - Verified entries - Questionable
entries - Suggested corrections - Geographic grouping concerns

Require manual approval for questionable changes.

STOP after Phase 8.

## PHASE 9 --- Settings and Statistics

Use DataStore to persist: - Sound setting - Animation setting - Games
played - Games won - Games lost - Draws - Total quartets collected

Optional: - Best score

Do not add accounts or cloud synchronization.

Acceptance criteria: - Settings and statistics survive application
restart

STOP after Phase 9.

## PHASE 10 --- Google Play Release Preparation

Review: - Application ID - Application name - Version name - Version
code - App icon - Adaptive icon - Release build - Signing
configuration - Permissions - Debug logging - Crash handling - Back
navigation - Screen sizes - Android versions

The game should NOT request Internet permission unless a future feature
genuinely requires it.

Generate:

``` text
app-release.aab
```

Do NOT publish anything automatically. Provide a final release checklist
and STOP.

## 23. Mandatory Development Rules

1.  Never proceed to the next phase automatically. Wait for explicit
    user instruction such as "Proceed to Phase 2."
2.  Every phase must leave the repository in a buildable state.
3.  Never report "Build successful" unless the build command actually
    executed successfully.
4.  Run relevant tests after game-engine changes.
5.  Do not silently modify game rules.
6.  Do not silently modify the list of countries or quartets.
7.  Do not introduce unnecessary architecture or frameworks.
8.  Prefer simple, maintainable Kotlin.
9.  Inspect existing code before modifying it.
10. Fix build/test failures before completing a phase.

## 24. Phase Completion Report

At the end of EVERY phase provide:

### Phase X Complete

#### Implemented

List implemented functionality.

#### Files Created

List important new files.

#### Files Modified

List important modified files.

#### Tests

List tests executed.

#### Build

Report exact build command and result.

#### Manual Testing Required

Explain what the user should test manually.

#### Known Issues

List remaining issues.

#### Next Phase

Describe the next phase in 2--4 sentences.

Then STOP. Do NOT start the next phase.

## 25. Git

Use Git throughout development. Prefer one stable commit after each
completed phase.

Suggested commit history:

``` text
Phase 1: Create Android project skeleton
Phase 2: Add 52 countries and 13 quartets
Phase 3: Implement Quartet game engine
Phase 4: Add computer player AI
Phase 5: Implement playable game
Phase 6: Add country card design
Phase 7: Polish gameplay UX
Phase 8: Verify educational content
Phase 9: Add settings and statistics
Phase 10: Prepare Google Play release
```

Do not intentionally commit broken builds.

## 26. Priorities

Use this priority order: 1. Correct game rules 2. No crashes 3.
Maintainable architecture 4. Good user experience 5. Educational
accuracy 6. Visual polish 7. Advanced features

Do not sacrifice correctness for animations or visual effects.

## 27. Out of Scope for Version 1

Do NOT implement unless explicitly requested later: - Online
multiplayer - Bluetooth multiplayer - Accounts - Login - Backend -
Firebase - Cloud saves - Leaderboards - Achievements - Advertising -
In-app purchases - Paid country packs - Chat - Social features - Complex
AI - iOS version

## 28. First Claude Code Task

Start with **PHASE 1 --- Android Project Skeleton**.

Before modifying files: 1. Inspect the current directory. 2. Check
whether an Android project already exists. 3. If it exists, inspect and
preserve useful existing configuration. 4. State a short Phase 1
implementation plan. 5. Implement only Phase 1. 6. Run the build. 7. Fix
all build errors. 8. Provide the Phase 1 completion report. 9. STOP.

Do NOT implement country data. Do NOT implement game rules. Do NOT
implement AI. Do NOT begin Phase 2 until explicitly instructed.
