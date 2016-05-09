#Handler for individual subscription connections.
import socket
import asyncore
import json

kTempReadBufSize = 4096

class SubscriptionHandler(asyncore.dispatcher):
	global kTempReadBufSize

	def __init__(self, address, socket, server):
		#Constants:
		self.kJSONTagClientID = "client_id"
		
		#Our local buffer.
		self.outBuffer = []
		#The parent server that spawned this handler.
		self.parent = server
		
	#Called when we send data to the client.
	def write(self, data):
		self.outBuffer.append(data)
		
	def writable(self):
		return self.outBuffer
		
	#Called when the handler shuts down (the connection is closing).
	def shutdown(self):
		#Paste a null entry to our output.
		self.outBuffer.append(None)
		
	#Called when client sends data.
	#We expect JSON containing an instance ID.
	def handle_read(self):
		#Pull data from the socket.
		data = self.recv(kTempReadBufSize)
		#Can print debug statement here...
		#In any case, the data should be the following JSON:
		#{ "client_id" : "[Instance ID Here]" }
		#Reject anything else.
		subscribeSuccessful = False
		try:
			parsedData = json.loads(data)
			#Data was valid JSON; does it have the ID we want?
			if self.kJSONTagClientID in parsedData:
				#Pull that and add to our parent's client list.
				parent.addClient(parsedData[self.kJSONTagClientID])
				#Mark a successful parse!
				subscribeSuccessful = True
			else:
				#Mark parse failure.
				subscribeSuccessful = False
		except ValueError:
			#Decode failed, can print debug statement.
			subscribeSuccessful = False
		#Tell the user if they submitted properly.
		self.write(subscribeSuccessful)
		
	#Called when we send a response to the client.
	def handle_write(self):
		#If we have a null entry, that indicates we should quit.
		if self.outBuffer[0] is None:
			self.close()
			return
			
		#Otherwise we send the topmost entry.
		elemsSent = self.send(self.outBuffer[0])
		#Did we send that entire entry?
		if elemsSent >= len(self.outBuffer[0]):
			#If so, pop it.
			self.outBuffer.pop(0)
		#Otherwise:
		else:
			#Keep the remainder we didn't send.
			self.outBuffer[0] = self.outBuffer[0][elemsSent:]
			
	#Called when the handler's connection has closed.
	def handle_close(self):
		#Remove ourselves from our parent.
		parent.removeHandler(self)
