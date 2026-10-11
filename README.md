# 🎵 Instrument Tuner

A professional-grade, offline chromatic tuner for Android supporting **49 cultural
instruments**, microtonal practice, and structured training — built with Kotlin +
Jetpack Compose, YIN pitch detection, and FFT analysis.

## Features
- **Detection**: YIN pitch detection (27.5 Hz–2 kHz), adaptive noise gate, FFT spectrum
- **Displays**: gauge, strobe, spectrum, combo, and full-screen **Stage mode**
- **Theory**: 4 temperaments + tonic, octave stretch, per-string offsets, transposition
- **Calibration**: ± Hz, quick presets, capture-by-ear, per-instrument A4 memory + drift log
- **Practice**: timed rounds, history, progress dashboard, beat ear trainer, scale/maqam trainer
- **Workflow**: auto-advance, play-all, drone, lineup queue + templates, detect-by-sound
- **Data**: custom presets with labels, import/export (JSON), CSV export, tuning certificates
- **Polish**: 6 themes, home-screen widget, haptics

## Build & run
Open in Android Studio (AGP 8.7+, Kotlin 2.0) → Sync → run on a device or emulator.

## Tests
| Layer | Command |
|---|---|
| Unit + Robolectric | `./gradlew testDebugUnitTest` |
| Instrumented / E2E | `./gradlew connectedDebugAndroidTest` |
| Lint | `./gradlew lintDebug` |

The E2E suite injects a `FakePitchSource` so no physical microphone is needed —
deterministic on CI. The detector is unit-tested with synthetic sine/harmonic/noise signals.

## Project layout
music/ Note math, 49 instruments, temperaments, microtonal scales
audio/ YIN detector, FFT, AudioEngine (PitchSource seam), TonePlayer
presets/ Custom presets, import/export, string offsets (SharedPreferences/JSON)
session/ Practice sessions, lineup templates
ui/ Theme (6 palettes), gauge/strobe/spectrum/sparkline, 13 sheets
