# Online-Client-Server-Android-Restaurant-Application

### Introduction
With the rapid development of information technology, Android application have been increasing in recent years. 
The advantage of the Android application: 
- Mobile application is convenient to carry 
- Global partnerships and large install base 
- Powerful development framework
- Open marketplace for distributing apps 

Based on these advantages, we have developed an Online Client/Server Android Restaurant Food Ordering Application. Assumption has been made that the restaurant is using internet technology to run its business. The customer could use mobile devices ordering food online and pick up when the food is ready. 

In general, we used Producer-Consumer pattern, socket communication, multiple threads, and Handler & Looper to develop this application.

We included one Jar package in both Client and Server applications, which includes: ClientOrder, Contacts, OrderCommand, OrderDetails, OrderRespond. In addition, we built Socket to communicate with these two applications. In both ends, we created one socket receiver thread to receive from the sender from the other side. For example, we could receive OrderCommand from Client Socket Sender Thread in Server side, and we could receive OrderRespond from Server Socket Sender in Client side. Furthermore, we created five threads: UI Thread, which is to update UI; Socket Server Thread, which is to communicate with both applications; Prepare Thread, which is to prepare orders from customers; Package Thread, which is to package and deliver orders to customers; Inventory Thread, which is to check availability of orders.
