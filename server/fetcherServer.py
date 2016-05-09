#Handles server updates.
import time
import asyncore
import socket
import httplib
import urllib
from subscriptionHandler import SubscriptionHandler

kMinsToSecs = 60.0
kHoursToMins = 60.0

class FetcherServer(asyncore.dispatcher):
	global kMinsToSecs
	global kHoursToMins
	
	#Constructs server object.
	def __init__(self, serverID, maxPostDelaySecs, address, port):
		asyncore.dispatcher.__init__(self)
	
		#Declare constants:
		#Minimum required delay between posts.
		self.kMinPostDelaySecs = 1.0 * kMinsToSecs
		#Server loop will sleep for (this times
		#self.kMinPostDelaySecs) seconds on each iteration. 
		self.kSleepFactor = 0.1
		#The maximum number of connections that can be waiting
		#but not yet served by this server.
		self.kMaxQueuedConnections = 5
		#Strings:
		self.kClientFilePath = "fetcher_clients.txt"
		self.kErrStartupFailed = "FetcherServer: Failed to start up essential services, aborting!"
		self.kFmtFileLoaded = "FetcherServer: Client file {0} loaded, got {1} clients"
		self.kFmtErrFileLoadFailed = "FetcherServer: Couldn't open client file {0}!"
		self.kFmtListenStarted = "FetcherServer: Listening to {0}:{1}"
		self.kFmtPostFailed = "FetcherServer: Failed to post to client: HTTP {0}: {1}. Server response: {2}"
		self.kFmtShutdownStarted = "FetcherServer: Server shutting down! Closing {0} connections."
		#HTTP POST strings:
		self.kHTTPNotifyAddress = "https://gcm-http.googleapis.com/gcm/send"
		self.kHTTPHeaderContentType = "Content-type"
		self.kHTTPContentType = "application/json"
		self.kHTTPHeaderAuthorization = "Authorization":
		self.kHTTPAuthorization = "key={0}".format(serverID)
		self.kHTTPHeader = {self.kHTTPHeaderContentType : self.kHTTPContentType, self.kHTTPHeaderAuthorization : self.kHTTPAuthorization}
		self.GCMConnection = httplib.HTTPConnection(self.kHTTPNotifyAddress)
		
		#Our service ID in Google GCM.
		self.serverID = serverID
		#The list of clients.
		self.clients = []
		#The file we back clients up to.
		self.clientFile = None
		#The list of active connection handlers.
		self.handlers = []
		#The address we listen to.
		#Localhost will only allow listening to clients on the server's machine,
		#0.0.0.0 will allow listening to anyone.
		self.listenAddress = address
		#Port to listen to clients on.
		self.listenPort = port
		#The socket we listen to for client subscriptions/
		#unsubscriptions.
		#self.listenSocket = None
		
		#The amount of time elapsed since the last post.
		self.prevTimeStamp = 0.0
		self.currTimeStamp = 0.0
		self.elapsedPostTimeSecs = 0.0
		#The amount of time required to elapse before a post can happen.
		#Cap this to the minimum post delay.
		self.postDelaySecs = maxPostDelaySecs > self.kMinPostDelaySecs ? maxPostDelaySecs : self.kMinPostDelaySecs
	
	### Begin asyncore.dispatcher implementation.
	#Called when a client is ready to connect.
	def handle_accept(self):
		socket, clientAddress = self.accept()
		#Assign a handler for this specific client.
		self.handlers.append(SubscriptionHandler(clientAddress, socket, self))
	
	### Begin FetcherServer methods.
	#Called when a client has subscribed to the server.
	def addClient(self, clientID):
		#Is this client already in our list?
		if clientID in self.clients:
			#If so, quit.
			return
			
		#Otherwise, add it to our list.
		self.clients.append(clientID)
		#Add it to the client list file too.
		self.clientFile.write(clientID)
		self.clientFile.write("\n")
	
	#Removes a handler from the handler list.
	def removeHandler(self, handler):
		self.handlers.remove(handler)
	
	#Loads the saved list of clients.
	def loadClientList(self):
		#Open the default client file.
		try:
			self.clientFile = open(self.kClientFilePath, "rw")
			numClients = 0
			#Read all of the lines in the file, assume they're
			#instance IDs and add them to our client list.
			while nextLine = self.clientFile.readline():
				self.clients.append(nextLine)
				numClients += 1
			print self.kFmtFileLoaded.format(self.kClientFilePath, numClients)
		#If it failed, report failure.
		except IOError:
			print self.kFmtErrFileLoadFailed.format(self.kClientFilePath)
			return False
		#Otherwise report success.
		return True

	#Opens the subscriber socket.
	def startListening(self):
		#Open the socket.
		#It's going to be a streaming internet socket on the default port.
		self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
		#Specify the connection...
		self.bind(self.listenAddress, self.listenPort)
		#and start listening (we'll keep a backlog of kMaxQueuedConnections).
		self.listen(self.kMaxQueuedConnections)
		print self.kFmtListenStarted.format(self.listenAddress, self.listenPort)
		return True
	
	#Loads all needed resources.
	#Returns:
	#	* True if all necessary resources were loaded,
	#	* False otherwise.
	def startup(self):
		#Load up the client list if we have one.
		#If client loading failed, drop the client list (log the error).
		if not self.loadClientList():
			return False
	
		#Open up a listening socket for subscribers.
		if not self.startListening():
			return False
			
		return True

	#Sends a push notification to a client.
	def notify(self, clientID):
		#Write a HTTP POST to Google's GCM.
		postBody = json.dumps({"data":{}, "to":clientID})
		self.GCMConnection.request("POST", "", body=postBody, headers=self.kHTTPHeader)
		#If this was anything besides 200, report it.
		response = self.GCMConnection.getresponse()
		if response.status != httplib.OK:
			#Get the server's explanation and print it out.
			print self.kFmtPostFailed.format(response.status, response.reason, response.read())

	def postLoop(self):
		#While true:
		while True:
			#Update timer.
			self.currTimeStamp = time.clock()
			self.elapsedPostTimeSecs += self.currTimeStamp - self.prevTimeStamp
			self.prevTimeStamp = self.currTimeStamp
			#Is timer up?
			if self.elapsedPostTimeSecs >= self.postDelaySecs:
				#If so, for each client:
				for client in self.clients:
					#Notify that it should make its notification.
					self.notify(client)
				#Subtract timer period from timer.
				self.elapsedPostTimeSecs -= self.postDelaySecs
				
			#Also listen for socket updates.
			asyncore.loop()
			
			#Optionally put a sleep here so we don't devour the CPU.
			time.sleep(self.elapsedPostTimeSecs * self.kSleepFactor)

	def shutdown(self):
		print self.kFmtShutdownStarted.format(len(handlers))
		#Close the GCM connector.
		self.GCMConnection.close()
		#Close the subscriber socket.
		self.close()
		#Close all handlers.
		for h in handlers:
			h.shutdown()
		#Close the client file.
		self.clientFile.close()

	#Entry point for the program.
	def run(self):
		#Initialize resources.
		if not self.startup():
			#We don't have essential resources,
			#Tell admin and quit.
			print self.kErrStartupFailed
			return
	
		#Enter post loop.
		self.postLoop()
	
		#We've left the post loop for some reason;
		#shut everything down.
		self.shutdown()

def main():
	global kMinsToSecs
	global kHoursToMins
	kDefaultPort = 7263
	kDefaultAddress = '0.0.0.0'
	
	#Get this from Google.
	serverID = "AIzaSyCtbZy2lHCOrQXF5Hd7Posimi-NWcKDEeU"
	
	#Test this with a one minute delay.
	server = FetcherServer(serverID, 1.0 * kMinsToSecs, kDefaultAddress, kDefaultPort)
	server.run()

if __name__ == "__main__":
	main()
