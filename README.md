# ntis-proxy

The NTIS is the National Traffic Information Service which provides the current information about the journey times and events on the Highways and A-roads in England. The service is subscriber based push service where NTIS is a client and a subscriber is a server that recives the pushes in Datex II format. 

NTIS is supposed to keep the subscribers in sync and when detecting that the connection has been broken or a message has not been delivered is should send the "full refresh" - a message with all the information from the feeds the subscriber is interested in. Unfortunately the NTIS accepts 5XX and 4XX error codes as successfull data transmission. The connection broken or message not delivered is detected only when connection times out. 

This project is a simple workaround to this issue. The proxy passes the POST request from the NTIS to the subscriber and intercepts the response. If the response is an http status code different than 2XX it closes the connection between the NTIS and proxy triggering the "full refresh". In any other case the actual response from client is send back to NTIS.
