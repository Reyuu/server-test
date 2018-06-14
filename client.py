#test suite
import socket
import sys
import random
import time
import pygame
import ezpygame
import threading

#client?

class Item:
    def __init__(self, x, y, typ, player=None, picked=False):
        self.x = x
        self.y = y
        self.type = typ
        self.picked = picked
        #id
        self.owner = player
    
    def pick(self, player):
        self.x = -600
        self.y = -600
        self.owner = player
        self.picked = True
        return True

class Player:
    def __init__(self, id_, ip, nickname, x=0, y=0, hp=100):
        self.nickname = nickname
        self.id = id_
        self.ip = ip

        self.x = 0
        self.x_rate = 5
        self.y = 0
        self.y_rate = 5
        self.hp = 100

        self.inventory = {0:None, 1:None, 2:None,
                          3:None, 4:None, 5:None,
                          6:None, 7:None, 8:None}

class Communication():
    def __init__(self, ip, port):
        self.ip, self.port = ip, port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((self.ip, self.port))
        self.rand_int = 0
    
    def test_protocol(self, received):
        #ping
        if (received[0] == "b"):
            if int(received[1:]) == self.rand_int:
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

    def ping(self):
        self.rand_int = random.randint(100000, 999999)
        self.sock.sendall(bytes("a%s" % self.rand_int, "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def join(self, nick):
        self.sock.sendall(bytes("j%s" % nick), "utf-8")
        received = str(self.sock.recv(64), "")

    def disconnect(self):
        pass

    def position(self, x, y):
        pass
    
    def firing_vector(self, w, h):
        pass
    
    def item_pickup(self, id, slot):
        pass
    
    def heal(self, amount):
        pass


class Game(ezpygame.Scene):
    resolution = (800,600)
    def __init__(self, nick):
        super().__init__()
        self.communication = Communication()
        self.player = Player(-1, -1, nick)

    def on_enter(self, previous_scene):
        super().on_enter(previous_scene)
        self.previous_scene = previous_scene

    def handle_event(self, event):
        #join, disconnect, ping
        result = ""
        if event.type == jointke:
            result = self.communication.join(self.player.nickname)

        if event.type == disconnect:
            result = self.communication.disconnect()

        if event.type == ping:
            result = self.communication.ping()                                         

        if event.type == Position:
            result = self.communication.position(self.pl)
            
        pass

    def draw(self, screen):
        screen.fill((255,255,255))

class NickSelection(ezpygame.Scene):
    def __init__(self):
        self.font = pygame.font.SysFont("Arial", 16)
        self.buffer = ""

    def draw(self, screen):
        screen.fill((255, 255, 255))
        #pygame.draw.rect(screen, (0,0,0), (400,300, 400, 300), 2)
        text = self.font.render("Enter nickname:", 1, (0,0,0))
        screen.blit(text,(400,300))
        buffer_rendered = self.font.render(self.buffer, 1, (0,0,0))
        screen.blit(buffer_rendered, (400, 320))
        pygame.display.flip()

    def handle_event(self, event):
        if event.type == pygame.KEYDOWN:
            if event.key < 32:
                if event.key == 8:
                    self.buffer = self.buffer[:-1]
            elif event.key >= 32:
                ignore = [300, 301, 302, 303, 304, 305, 306, 307, 308]
                mods = pygame.key.get_mods()
                char = chr(event.key)
                if mods == 2 or mods == 8192:
                    char = char.upper()
                print("%s - %s" % (mods,ord(char)))
                if not ord(char) in ignore:
                    self.buffer += char
        if event.type == pygame.QUIT:
            pygame.quit()
            sys.exit(1)

if __name__ == "__main__":
    app = ezpygame.Application(title="Pancakes killer", resolution=(800,600), update_rate=75,)
    app.run(NickSelection())

"""  
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
"""