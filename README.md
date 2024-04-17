# Movie Night

A simple proof of concept for streaming videos to friends using WebRTC.

The live site is available at: https://azgoalie.github.io/movie-night

## Developed with

- [ClojureScript](https://clojurescript.org/)
- [shadow-cljs](https://github.com/thheller/shadow-cljs)
- [Firebase](https://firebase.google.com/)
- [PicoCSS](https://picocss.com/)

## Development

To start development: `npm run start`

To start the firebase emulators: `npm run start:emulators`

### Note about HTML hot reloading

When making changes to the hiccup code, the changes won't be relfected in the browser until you force a recompile of the clojurescript code.

This can be done by simply saving any `.cljs` file which will cause `shadow-cljs` to refresh and run the `build-hooks` which will re-generate the HTML files.
