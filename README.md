# webstorm-styled-components
Support for styled-components 💅 in WebStorm

The plugin can be installed in WebStorm, IntelliJ IDEA Ultimate, PhpStorm, PyCharm Pro, and RubyMine v2017.2 and above.

# Installation
To install the plugin:
- Download the latest (release)[https://github.com/styled-components/webstorm-styled-components/releases/]
- In WebStorm, hit <kbd>Cmd</kbd>/<kdb>Ctrl</kdb>-<kbd>Shift</kbd>-<kbd>A</kbd> to bring up the actions pane. Then select *Add Plugin From Disk*
- Select `webstorm-styled-components.zip` to install the plugin

# Features
With this plugin you can enjoy the full coding assistance for styled-components 💅

- Start typing to get code completion for CSS properties and values

<img src="https://blog.jetbrains.com/webstorm/files/2017/09/ws-st-comp-css-completion.png" width=680>

- Hit <kbd>Alt</kbd>-<kbd>Enter</kbd> to see available intentions and quick-fixes

<img src="https://blog.jetbrains.com/webstorm/files/2017/09/ws-st-comp-intentions.png" width=680>

- Start typing in the interpolation to see completion suggestions for JavaScript variables, methods and functions

<img src="https://blog.jetbrains.com/webstorm/files/2017/09/ws-st-comp-js-completion.png" width=680>

- Cmd/Ctrl-click on the JavaScript symbol to do to its definition

# Limitations
The plugin only works with the JetBrains IDEs version 2017.2 or above.

Semicolons at the end on the line should be outside the string, e.g. `color: ${props => props.primary ? 'white' : 'palevioletred'};`

# Contributing to the plugin
Please report any issue with the plugin on [GitHub](https://github.com/styled-components/webstorm-styled-components/issues). We welcome your pull requests.

The plugin is written in [Kotlin](https://kotlinlang.org/) and uses [Gradle](https://gradle.org/). Check out this [tutorial](http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/prerequisites.html) on how to run and build the plugins for the IntelliJ IDEA platform with Gradle.

# Todo
- Smart Indentation when opening backticks and pressing enter.

# License (MIT)
Copyright 2017 Hossam Saraya

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
