# styled-components-webstorm-syntax
React Styled Components Language Support in IntelliJ Platform

# Motivation
https://github.com/styled-components/styled-components/issues/176

# Current Progress
![alt text](https://d26dzxoao6i3hh.cloudfront.net/items/1Z0q2R2Y3F0b0H091436/Image%202017-08-30%20at%204.23.48%20PM.png?v=589202df)

# Done
- Capture tagged template literals for styled components (starting with `styled.*`, `styled()`, `keyframes`)
- Filter ES6 interpolations in selector position (Otherwise SCSS would complain about incorrect syntax)
- Inject SCSS in the matched literals

# TODO
- Treat ES6 Interpolation in property position as a css value
- Smart Indentation when opening backticks and pressing enter.
