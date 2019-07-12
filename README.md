# Gravity Box
[![Build Status](https://travis-ci.org/Luca1152/gravity-box.svg?branch=develop)](https://travis-ci.org/Luca1152/gravity-box) 

<a href="https://play.google.com/store/apps/details?id=ro.luca1152.gravitybox"><img src="https://i.imgur.com/nmfa0AR.png" width="auto" height="75"></a>

A minimalist physics game written in Kotlin & libGDX. 

This is the mobile port of my [Game Maker's Toolkit Jam 2018](https://itch.io/jam/gmtk-2018) entry, made in 48 hours, which placed 152nd (out of 1038). You can play the original version [here](https://luca1152.itch.io/gravity-box). 

<img src="https://i.imgur.com/cuhzvX0.gif" width=450px>

## Built with
- [Gradle](https://gradle.org/) - Build automation system
- [Kotlin](https://kotlinlang.org/) - Programming language
- [LibGDX](https://libgdx.badlogicgames.com/) - Game framework
- [shadow](https://github.com/johnrengelman/shadow) - Gradle plugin for creating JARs
- [Ashley](https://github.com/libgdx/ashley/wiki) - Entity-component-system library
- [LibKTX](https://github.com/libktx/ktx) - Kotlin extensions for LibGDX
- [LibKTX.inject](https://github.com/libktx/ktx/tree/master/inject) - Dependency injection library
- [Box2D](https://github.com/libgdx/libgdx/wiki/Box2d) - 2D physics library
- [gdx-pay](https://github.com/libgdx/gdx-pay) - Cross-platform in-app purchases API
- [gdx-fireapp](https://github.com/mk-5/gdx-fireapp) - Cross-platform Firebase API
- [Firebase Ads](https://firebase.google.com/docs/admob/admob-firebase) - Mobile advertising API
- [Firebase Authentication](https://firebase.google.com/docs/auth) - Authentication API
- [Firebase Performance](https://firebase.google.com/docs/perf-mon) - Performance monitoring API
- [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics) - Crash reporting API
- [Firebase Realtime Database](https://firebase.google.com/docs/database) - NoSQL cloud database API

## Tools used
- [Android Studio](https://developer.android.com/studio) - IDE
- [Inkscape](https://inkscape.org/) - Vector graphics editor
- [TexturePacker](https://github.com/libgdx/libgdx/wiki/Texture-packer) - Texture atlas packing tool
- [Hiero](https://github.com/libgdx/libgdx/wiki/Hiero) - Bitmap font packing tool
- [gdx-liftoff](https://github.com/tommyettinger/gdx-liftoff) - Setup tool for libGDX projects
- [Google BigQuery](https://cloud.google.com/bigquery/) - Web service for SQL queries

## Running the game
If you just want to run the game on Desktop (Windows/Linux/macOS), without downloading the source-code and compiling it yourself, check out the [Releases](https://github.com/Luca1152/gravity-box/releases) tab. To run the game on Android, install it from [Google Play](https://play.google.com/store/apps/details?id=ro.luca1152.gravitybox).

#### Command-line
Windows: `gradlew desktop:run`  
Linux/macOS: `./gradlew desktop:run`

#### Android Studio / IntelliJ IDEA
More often than not, running the game from the command-line won't work. I suggest installing [Android Studio](https://developer.android.com/studio) and opening the project there. Wait for Gradle to download all dependencies, then run either the `android` or `Desktop` configuration.

If you want to run the game only on Desktop (excluding Android), you can use [IntelliJ IDEA](https://www.jetbrains.com/idea/).

The configurations previously mentioned, `android` and `Desktop` won't show up in [Eclipse](https://www.eclipse.org/eclipseide/).

## License
All code is licensed under the GNU General Public License v3.0 License - see the [LICENSE](https://github.com/Luca1152/gravity-box/blob/master/LICENSE) file for details.

All assets including graphics, sounds, icons and maps are licensed under the [Creative Commons BY-NC 4.0 license](https://creativecommons.org/licenses/by-nc/4.0/legalcode) unless otherwise indicated.

## Acknowledgments
- [∞ Infinity Loop](https://play.google.com/store/apps/details?id=com.balysv.loop&hl=en) - for the UI design, color scheme
