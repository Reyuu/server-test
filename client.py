import socket
import sys
import random
import time

#test suite

host, port = "localhost", 9999
rand_int = random.randint(100000, 999999)
nick = "Reyuu"
print(rand_int, nick)
#data = ["a%s" % rand_int, "j%s" % nick, "p523,12", "d"]
data = ["a%s" % rand_int, "j%s" % nick, "p523,12", "f1.453,-2.321", "i0,0", "d"]
received_list = []

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((host, port))
for i in data:
    sock.sendall(bytes(i, "utf-8"))

    received = str(sock.recv(64), "utf-8")
    received_list += [received]
    try:
        #ping
        if (received[0] == "b"):
            if int(received[1:]) == rand_int:
                print("PONGed")
        #join
        if (received[0] == "j"):
            if (received[1] == "c"):
                print("Joined at %s id" % received[2:])
            if (received[1] == "r"):
                print("Already connected at %s id" % received[2:])
        #disconnect
        if (received[0] == "d"):
            if (received[1] == "c"):
                print("Disconnected correctly")
            if (received[1] == "r"):
                print("Cannot disconnect")
        if (received[0] == "p"):
            if (received[1] == "c"):
                print("Position confirmed")
            if (received[1] == "r"):
                print("Position rejected")
        #heal
        if (received[0] == "h"):
            if (received[1] == "c"):
                print("Heal confirmed")
            if (received[1] == "r"):
                print("Heal rejected")
        #item
        if (received[0] == "i"):
            if (received[1] == "c"):
                print("Item confirmed")
            if (received[1] == "r"):
                print("Item rejected")
    except IndexError:
        pass
    #time.sleep(1/10000)
else:
    print("aa")
    sock.close()

print("sent: %s" % data)
print("received: %s" % received_list)
