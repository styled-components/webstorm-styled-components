# webstorm-styled-components
React Styled Components Language Support in IntelliJ/Webstorm

# Motivation
https://github.com/styled-components/styled-components/issues/176

# Installation
An alpha version is here! Here are the steps you need for installations
1 - Download the alpha release https://github.com/styled-components/webstorm-styled-components/releases
2 - Extract the archive
3 - In Webstorm, Hit <kbd>CMD</kbd>+<kbd>SHIFT</kbd>+<kbd>A</kbd> to bring up actions pane. Then select *Add Plugin From Filesystem*
4 - Select `webstorm-styled-components.jar` to install the plugin

# Known Issues
The plugin only works with Webstorm 2017

# Current Progress
![alt text](https://d26dzxoao6i3hh.cloudfront.net/items/1Z0q2R2Y3F0b0H091436/Image%202017-08-30%20at%204.23.48%20PM.png?v=589202df)

# Done
- Capture tagged template literals for styled components (starting with `styled.*`, `styled()`, `keyframes`)
- Filter ES6 interpolations in selector position (Otherwise SCSS would complain about incorrect syntax)
- Inject SCSS in the matched literals

# TODO
- Add installation instructions
- Treat ES6 Interpolation in property position as a css value
- Smart Indentation when opening backticks and pressing enter.

# License (MIT)
Copyright 2017

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
