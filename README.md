MiniJarvis
==========
<b>Project Goal:</b> To design and implement a solution that allows a user wearing Google Glass to perform searches using facial and object recognition software to learn more information about those people or objects. 

Working

Glassware takes images every 10 seconds. The image is sent to the server by RESTful web service. Server does face/object detection and recognition using face/object trained XML files and color extraction to formulate
the annotation required for google search. The server retrieves search results using Google Custom Search API. The server response is formed by the annotation and search results
The annotation is displayed on the glassware.Long press on the annotation yields search results.

Softwares used

Glassware is built using Android SDK add-on Glass Development Kit (GDK). Server side code is developed in Java technology using Eclipse Juno IDE.
RESTful webservice is in Java with the JAX-RS reference implementation Jersey. REST (Representational State Transfer) is an architectural style which uses HTTP protocol for the CRUD operations (GET, POST, PUT and DELETE).
Web service has JSON request and response and is deployed in Apache Tomcat 7, an open source software implementation of the Java Servlet and JavaServer Pages technologies.
OpenCV Library or Open Source Computer Vision is a software library for computer vision. It is cross-platform and mainly written in C++. It has interfaces for Java.
Google Custom Search API returns JSON response of search results for the annotation. The API allows 100 free requests per day. The response depends on the query parameters.

Inside the server

Face detection is done by OpenCV haarcascade classifiers. For face/object recognition the system must be trained and the face/object should be in the database (file system). The identified face/object is enclosed within a rectangle.
For color extraction, the image within the rectangle is rescaled to 32X32. The most dominant color is found by iterating through the pixels of the rescaled image.
Including the color of the object along with the object identified returns more relevant search results.
