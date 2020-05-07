# 3D Window for Android
![Screenshot of 3D Window for Android][screen]

## Description
This project is an attempt to turn your smartphone into a window allowing you to peer into a 3D scene, creating a realistic perspective effect. The inspiration for this project was this cool video I remember watching many years ago: [Head Tracking for Desktop VR Displays using the WiiRemote][1]

The app uses the phone's front facing camera to track the position of the user's eyes, and renders a 3D scene using OpenGL, by matching the user's eye positions to the OpenGL eye position, thereby creating the perspective effect. Face tracking is done using the Firebase ML Kit.

The important class in the project is ViewCalc, which converts the Firebase face landmark data into a set of eye position coordinates. It actually returns a rolling average of the generated coordinates over multiple image frames in order to reduce shaking, and also does additional smoothing in order to make the rendering more fluid. It calculates the eye positions using basic linear optics, using the following parameters:

* The inter-pupillary distance, fixed to 6.5cm (the average for males)
* A calibration factor, set using the "calibrate" button (see next section). The calibration factor use the apparent size of the inter-pupillary distance when the user is positioned at 20cm from the front camera.

## Usage
Once you run the app, position your face at 20cm of the front facing camera, and tap on the "calibrate" button. This will start the perspective effect.

For best results: keep your smartphone at a fixed position (for example leaning against a stack of books), calibrate, move back by 50cm or more, and then move your head around.

## Status
This project is still at an early stage, but all the basic components (face tracking, eye position calculating, rendering) are implemented. To view the effect, I've implemented a minimal scene consisting of three slightly offset triangles.

The results so far are not great: I personally don't see the perspective effect. I think one big reason might be the latency between the head movements and the rendering which kills the illusion. But it's also very possible that I've made an error somewhere.

## Installation
This project depends on the Firebase ML Kit, so in order to run it you will have to create a Firebase account (if you don't already have one), and register the app. Full steps are available in the [firebase documentation][2].

## Contribute
Feel free to contribute to this project or fork it for your own experimentations.

## License
This project is licensed under GPL-3.0

[screen]: window3d.png
[1]: https://www.youtube.com/watch?v=Jd3-eiid-Uw
[2]: https://firebase.google.com/docs/android/setup
