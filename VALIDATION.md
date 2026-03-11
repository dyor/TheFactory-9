# App-Specific Validation Plan: Factory-9

## Methodology
This validation plan tests the actual core Domain UseCases and app-specific behaviors defined in the `GUIDE.md`. Tests are categorized by their corresponding Phases and features to ensure we are testing what the app actually *does*.

## Phase 1: Foundation & Theme
### Custom Aesthetic
- [ ] **User Action**: Verify the `film_noir.png` custom background image successfully renders on both Android and iOS upon app launch.

## Phase 2: UI & Navigation
### Core Screen Mapping
- [ ] **Agent Action**: Implement automated UI navigation tests verifying the presence of 5 specific buttons on the Home screen.
- [ ] **User Action**: Launch the app and manually verify the Home screen has 5 distinct buttons.
- [ ] **User Action**: Tap each button and verify logical navigation to the 5 corresponding screens: Writers Room, Recording Studio, Editing Studio, Publishing Studio, and Archives.
- [ ] **User Action**: Verify each destination screen has a functioning Home button that returns the user to the Home screen.

## Phase 3: Hardware & Native Integrations
### 1. Writer's Room (Gemini Integration)
- [ ] **Agent Action**: Write a unit test mocking the Gemini 2.5 Flash response to verify script generation logic works correctly for a 60-second script constraint.
- [ ] **User Action**: Open the Writer's Room, submit the default prompt (or a custom one), and verify the app calls Gemini and successfully displays a script.
- [ ] **User Action**: Edit the generated script on the screen and verify the changes can be successfully saved to the local Room database.

### 2. Recording Studio (Camera & Teleprompter)
- [ ] **User Action**: Navigate to the Recording Studio and verify the front-facing camera view is active on the bottom half of the screen, with the script displayed on the top half.
- [ ] **User Action**: Tap the start button and verify a 5-second countdown begins.
- [ ] **User Action**: Verify the app successfully records video after the countdown.
- [ ] **User Action**: Verify the teleprompter displays exactly 3 lines of the script at a time and advances at a pace that finishes the script in 60 seconds.
- [ ] **User Action**: After recording, verify options to re-record, go back (Writer's Room), or go forward (Editing Studio) function correctly and intuitively.

### 3. Editing Studio (Video Trimming)
- [ ] **User Action**: Navigate to the Editing Studio with a recorded video.
- [ ] **User Action**: Mark specific segments (e.g., mistakes, white space) of the video for removal.
- [ ] **User Action**: Tap 'Save' and verify the modified/cut video is saved correctly.
- [ ] **User Action**: Tap 'Restore' and verify the original, unedited video is restored.

### 4. Publishing Studio (YouTube Integration)
- [ ] **User Action**: Navigate to the Publishing Studio.
- [ ] **User Action**: Trigger the publish action and verify the video is successfully saved to the device's native Photo app.
- [ ] **User Action**: Verify the app successfully launches an intent to open the native YouTube app for the user to upload the Short.

## Phase 4: Final Cut
### Regression & Optimization
- [ ] **User Action**: Perform a full regression test of all the above features from end to end (Home -> Generate Script -> Record Video -> Edit Video -> Publish) on both an Android and iOS physical device to ensure there are no integration bugs.