# MultiHighlight

<!-- Plugin description -->
Highlight identifiers with custom colors, helpful for reading source code.

Default shortcut: <kbd>Ctrl</kbd>+<kbd>'</kbd> (or <kbd>⌘</kbd>+<kbd>'</kbd> on mac).

![preview](https://raw.githubusercontent.com/huoguangjin/MultiHighlight/master/screenshot/preview.gif)

<!-- Plugin description end -->

![screenshot-studio](screenshot/screen-darcula.png)

## Install

+ Install MultiHighlight directly from the IDE: **File > Settings > Plugins > Browse Repositories... > search "MultiHighlight"**.
+ Install from [file](https://github.com/huoguangjin/MultiHighlight/releases).

## Customize

+ You can customize your highlight text style (bold/italic, foreground, background, stripe and effect style) in setting page.

    > Tips: Here is [material design color palette](palette.json), they are bright and vibrant colors. ([what is material design?](https://material.io/guidelines/style/color.html))

    ![setting](screenshot/setting-default.png)

+ change the default keyboard shortcut: **File > Settings > Keymap > search "MultiHighlight"**.

    ![keymap](screenshot/keymap.png)

+ If you like [IdeaVim](https://plugins.jetbrains.com/plugin/164), add following line to `~/.ideavimrc` and trigger MultiHighlight:

    ```vim
    " Press `'` to toggle highlight
    map ' :action MultiHighlight<CR>
    " Press `"` to clear all highlights in current editor
    map " :action MultiHighlight.ClearAction<CR>
    ```

## Build & Run

+ JDK 11 required, you can set [`org.gradle.java.home`](https://docs.gradle.org/current/userguide/build_environment.html) in `gradle.properties`.

+ To build MultiHighlight, clone and run `./gradlew buildPlugin`.

+ To run IntelliJ IDEA with MultiHighlight installed, run ` ./gradlew runIde`.

## Change Log

[check CHANGELOG.md for details](CHANGELOG.md)
