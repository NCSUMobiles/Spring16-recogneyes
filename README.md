# Recognize: A Visual Recognition Game

### Author(s):

Last iteration:
Chetan Pawar (chpawar@ncsu.edu) and his team: https://github.com/chetanpawar0989/Recognize-App/tree/score_screen

Current iteration:
Nischal Shrestha (nshrest@ncsu.edu)

### What is Recognize?

Recognize is a simple image-based quiz game. There are plenty of apps out there that let you participate in quiz games, but most of these are primarily text based. Recognize is different in that it uses images. Specifically, players are asked to identify one of the image choices as being somehow related to the main image, which is filtered with some effect (ex: blur) and slowly turns into the original image on the screen. 

For example, one of the categories is "Famous Moustaches". Players might be shown a heavily distorted image of Burt Reynolds and challenged to select his image from a group of similar looking people as the main image slowly becomes more clear.

Recognize is a continuing project. It is the summation of work from a series of past semesters. The last iteration (Spring 2015) was primarily focused on user experience and interface design. While making these UX improvements, however, Chetan's team noticed several backend changes that would make the application much better from a development perspective. 

The primary area of improvement they wanted to implement was image filtering algorithms. Previously, all images were stored statically, and this meant the application was rather large (close to 200MB). 

### Progress:

Nischal worked on revamping the entire app and iterating 2 different versions of the game and a brand new game called Odd Man Out: 

- Match: Match the main image to an image identical to one of the choices.
- Correlate: Match the main image to an image that isn't identical but correlated to the main iamge.
- Odd Man Out: Pick the odd one out of four image choices in the given category

Image filtering algorithms were added to combat the inefficient strategy of storing intermediate filtered images. 

Currently, the three branches excluding master, includes the updated games that each has a module called 'recognize' used to communicate with a Google App Engine Server. These 3 branches represents 3 different games, which are all a reasonably small size thanks to the filtering algorithms.

Nischal also added a backend with the help of Google App Engine, which implements an Endpoints API, used to communicate with the Python Datastore on GAE. Currently, the app can talk to the Endpoints API by authenticating with a Google Account on their device. This allows the user to retrieve newly created Albums on the website below.

### Future Work:
- Secure the entire website with OAuth (currently, it isn't authenticated)
- It would be cool if users can create an album on the website and share them with specific groups of people
- Difficulty settings to play at easy and medium level is not implemented.
- It would also be convenient for the Gallery screen to have a filter (on the ActionBar) public/private content or categories

### GAE Website:

https://recognize-1210.appspot.com

### GAE github repo:

https://github.com/nischalshrestha/recognize

### Link to last iteration's github repo:
https://github.com/chetanpawar0989/Recognize-App/tree/score_screen
