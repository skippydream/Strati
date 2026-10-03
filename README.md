![GitHub Release](https://img.shields.io/github/v/release/skippydream/Strati)
![Github latest release](https://img.shields.io/github/last-commit/skippydream/Strati)
![GitHub repo size](https://img.shields.io/github/repo-size/skippydream/Strati)

# Strati
  
**Strati** (which is the Italian for "Layers") is an Android app built with Jetpack Compose that presents thought-provoking philosophical and social questions. The questions become progressively more challenging, guiding users through increasingly deeper layers of reflection and conversation.

## Main Features

- **Navigation between Topics and Layers:** select a topic and then a layer to start the question session.
- **Multilanguage support:** easily switch between Italian and English with a simple language toggle.

## Screenshots
<p align="center">
  <img src="https://github.com/skippydream/Strati/blob/main/Images/1.png?raw=true" width="350"/>
  <img src="https://github.com/skippydream/Strati/blob/main/Images/3.png?raw=true" width="350"/>
  <img src="https://github.com/skippydream/Strati/blob/main/Images/2.png?raw=true" width="350" />
</p>

## Build

This repository is ready to be imported into Android Studio.

| | |
|---|---|
| `minSdk` | 24 (Android 7.0) |
| `compileSdk` / `targetSdk` | 36 |
| JDK | 17-21 (the one bundled with Android Studio works out of the box) |
| Gradle | 8.14.5 via the wrapper |

```bash
./gradlew assembleRelease
```

Release builds are shrunk with R8 and resource shrinking.

## Project layout

```
app/src/main/java/com/skippydream/strati/
  data/      topic catalogue, question loading, language handling
  ui/        navigation graph, screens, reusable components, theme
app/src/main/res/
  raw/       questions in Italian
  raw-en/    questions in English (picked automatically by locale)
```

Adding a question means editing the matching file under `res/raw` and `res/raw-en`: one question per line.

## Credits

The title is set in [Fraunces](https://fonts.google.com/specimen/Fraunces), used under the
SIL Open Font License — see [licenses/Fraunces-OFL.txt](licenses/Fraunces-OFL.txt). The bundled
`app/src/main/res/font/fraunces.ttf` is a static instance (weight 600, optical size 72) subset to
the Latin characters the app needs.
