# Recognize: A Visual Recognition Game

### Author(s):

Last iteration:
Nischal Shrestha (nshrest@ncsu.edu)

Current iteration: Arjun Sharma(asharm24@ncsu.edu) Dani Chiavegatto(dychiave@ncsu.edu) Mason Lesser(malesser@ncsu.edu) Julian Patterson(jcpatte2@ncsu.edu) Vasil Uhnyuck(vuhnyuc2@ncsu.edu)

### What is Recognize?

Recognize is a simple image-based quiz game. There are plenty of apps out there that let you participate in quiz games, but most of these are primarily text based. Recognize is different in that it uses images. Specifically, players are asked to identify one of the image choices as being somehow related to the main image, which is filtered with some effect (ex: blur) and slowly turns into the original image on the screen. 

For example, one of the categories is "Famous Moustaches". Players might be shown a heavily distorted image of Burt Reynolds and challenged to select his image from a group of similar looking people as the main image slowly becomes more clear.

### Progress:

This iteration our team integrated the backend Google App Engine with the Android Application. This allows the application to store all of the content
required for the application on the server. Users are able to import new content from the server, thus allowing future teams to remotely manage content.
 This has drasticly improved the application performance, and the size of the application because content is no longer requried to be downloaded upon
downloading the application. 

### Future Work:
- Secure the entire website with OAuth (currently, it isn't authenticated)
- It would be cool if users can create an album on the website and share them with specific groups of people
- Difficulty settings to play at easy and medium level is not implemented.
- It would also be convenient for the Gallery screen to have a filter (on the ActionBar) public/private content or categories

### GAE Website:

https://recognize-1210.appspot.com

### GAE github repo:

https://github.com/nischalshrestha/recognize