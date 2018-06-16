#test suite
import socket
import sys
import random
import time
import pygame
import ezpygame
import threading
import queue

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
    def __init__(self, id_, nickname, x=0, y=0, hp=100):
        self.nickname = nickname
        self.id = id_
        self.connected = False

        self.x = x
        self.x_rate = 5
        self.y = y
        self.y_rate = 5
        self.hp = hp
        self.velocity_x = 0
        self.velocity_y = 0
        self.inventory = {0:None, 1:None, 2:None,
                          3:None, 4:None, 5:None,
                          6:None, 7:None, 8:None}

class Communication(threading.Thread):
    def __init__(self, ip, port):
        self.ip, self.port = ip, port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((self.ip, self.port))
        self.rand_int = 0
        self.queue = queue.Queue()
        self.result_queue = queue.Queue()
        self.counter = 0
        threading.Thread.__init__(self)
    
    def on_thread(self, function, *args, **kwargs):
        self.queue.put((function, args, kwargs))
    
    def get_result(self):
        try:
            return self.result_queue.get()
        except queue.Empty:
            return None

    def run(self):
        while True:
            try:
                function, args, kwargs = self.queue.get()
                result = ""
                try:
                    if type(args[0]) is (tuple or list):
                        result = function(*args[0], *kwargs)
                except IndexError:
                    result = function(*args, *kwargs)
                if result == "":
                    result = function(*args, *kwargs)
                self.result_queue.put(result)
            except queue.Empty:
                self.idle()

    def idle(self):
        self.counter += 1
        if self.counter >= 1000:
            self.ping()
            self.counter = 0
        pass

    def ping(self):
        self.rand_int = random.randint(100000, 999999)
        self.sock.sendall(bytes("a%s" % self.rand_int, "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def join(self, nick):
        self.sock.sendall(bytes("j%s" % nick, "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received

    def disconnect(self):
        self.sock.sendall(bytes("d", "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received

    def position(self, x, y):
        self.sock.sendall(bytes("p%s,%s" % (x, y), "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def firing_vector(self, w, h):
        self.sock.sendall(bytes("f%s,%s" % (w, h), "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def item_pickup(self, id_, slot):
        self.sock.sendall(bytes("i%s,%s" % (id_, slot), "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def heal(self, amount):
        self.sock.sendall(bytes("h%s" % amount, "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def player_list(self):
        self.sock.sendall(bytes("l", "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def get_pos_of_a_player(self, id):
        self.sock.sendall(bytes("g%s" % id, "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received
    
    def get_nickname_of_a_player(self, id):
        self.sock.sendall(bytes("n%s" % id, "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received

    def check_for_events(self):
        self.sock.sendall(bytes("c", "utf-8"))
        received = str(self.sock.recv(64), "utf-8")
        return received

class Game(ezpygame.Scene):
    resolution = (800,600)
    def __init__(self, nick):
        super().__init__()
        self.communication = Communication("localhost", 9999)
        self.communication.start()
        self.player = Player(-1, nick, x=400, y=300)
        self.mouse_vector = [0,0]

    def on_enter(self, previous_scene):
        super().on_enter(previous_scene)
        self.previous_scene = previous_scene
    
    def test_protocol(self, received):
        try:
            #ping
            if (received[0] == "b"):
                if int(received[1:]) == self.communication.rand_int:
                    print("PONGed")
            #join
            if (received[0] == "j"):
                if (received[1] == "c"):
                    print("Joined at %s id" % received[2:])
                    self.player.id = int(received[2:])
                    self.player.connected = True
                if (received[1] == "r"):
                    print("Already connected at %s id" % received[2:])
            #disconnect
            if (received[0] == "d"):
                if (received[1] == "c"):
                    print("Disconnected correctly")
                    self.player.id = -1
                    self.player.connected = False
                if (received[1] == "r"):
                    print("Cannot disconnect")
            if (received[0] == "p"):
                if (received[1] == "c"):
                    pos = received[2:].split(",")
                    self.player.x, self.player.y = int(pos[0]), int(pos[1])
                    #print("Position confirmed %s" % received[2:])
                if (received[1] == "r"):
                    #print("Position rejected %s" % received[2:])
                    pass
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
            
            if (received[0] == "f"):
                if (received[1] == "c"):
                    print("Firing vector confirmed")
                if (received[1] == "r"):
                    print("Firing vector rejected")
        except IndexError:
            pass

    def update(self, dt):
        if self.player.connected:
            self.communication.on_thread(self.communication.position, (self.player.x+self.player.velocity_x, self.player.y+self.player.velocity_y))
            result = self.communication.result_queue.get()
            self.test_protocol(result)

    def handle_event(self, event):
        if event.type == pygame.QUIT:
            self.communication.disconnect()
            self.communication.sock.close()
            pygame.quit()
            sys.exit(1)
        if event.type == pygame.MOUSEMOTION:
            mousex, mousey = event.pos
            self.mouse_vector = [mousex-self.player.x, mousey-self.player.y]
            #print(self.mouse_vector)
        result = ""
        if event.type == pygame.KEYUP:
            if event.key == pygame.K_w:
                self.player.velocity_y = 0
            if event.key == pygame.K_s:
                self.player.velocity_y = 0
            if event.key == pygame.K_a:
                self.player.velocity_x = 0
            if event.key == pygame.K_d:
                self.player.velocity_x = 0
        if event.type == pygame.KEYDOWN:
            #movement
            if event.key == pygame.K_w:
                #result = self.communication.position(self.player.x, self.player.y-5)
                if not(self.player.velocity_y == -self.player.y_rate):
                    self.player.velocity_y -= 1
            if event.key == pygame.K_s:
                #result = self.communication.position(self.player.x, self.player.y+5)
                if not(self.player.velocity_y == self.player.y_rate):
                    self.player.velocity_y += 1
            if event.key == pygame.K_a:
                #result = self.communication.position(self.player.x-5, self.player.y)
                if not(self.player.velocity_x == -self.player.x_rate):
                    self.player.velocity_x -= 1
            if event.key == pygame.K_d:
                #result = self.communication.position(self.player.x+5, self.player.y)
                if not(self.player.velocity_x == self.player.x_rate):
                    self.player.velocity_x += 1
        
            if event.key == pygame.K_0:    #join
                self.communication.on_thread(self.communication.join, self.player.nickname)
                result = self.communication.get_result()

            if event.key == pygame.K_1:    #disconnect
                self.communication.on_thread(self.communication.disconnect)
                result = self.communication.get_result()
            if event.key == pygame.K_2:          #ping
                self.communication.on_thread(self.communication.ping)
                result = self.communication.get_result()
            if event.key == pygame.K_3:      #Position
                self.communication.on_thread(self.communication.position, self.player.x, self.player.y)
                result = self.communication.get_result()
            if event.key == pygame.K_4:    #disconnect
                #result = self.communication.firing_vector(*self.mouse_vector)
                self.communication.on_thread(self.communication.firing_vector, *self.mouse_vector)
                result = self.communication.get_result()
            
            if event.key == pygame.K_5:          #ping
                #result = self.communication.item_pickup(2,1)
                self.communication.on_thread(self.communication.item_pickup, 2, 1)
                result = self.communication.get_result()

            if event.key == pygame.K_6:      #Position
                self.communication.on_thread(self.communication.heal, 5)
                result = self.communication.get_result()

            if event.key == pygame.K_7:      #player list
                self.communication.on_thread(self.communication.player_list)
                result = self.communication.get_result()
            
            if event.key == pygame.K_8:      #get pos of a player
                self.communication.on_thread(self.communication.get_pos_of_a_player, 0)
                result = self.communication.get_result()

            if event.key == pygame.K_9:      #get nickname of a player
                self.communication.on_thread(self.communication.get_nickname_of_a_player, 0)
                result = self.communication.get_result()

            if event.key == pygame.K_o:      #check for events
                self.communication.on_thread(self.communication.check_for_events)
                result = self.communication.get_result()

        #print(result)
        self.test_protocol(result)
        pass

    def draw(self, screen):
        screen.fill((0,0,0))
        pygame.draw.rect(screen, (255, 0, 0), (self.player.x-25, self.player.y-25, 50, 50), 0)
        pygame.display.flip()

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
            if event.key >= 32:
                ignore = [300, 301, 302, 303, 304, 305, 306, 307, 308]
                mods = pygame.key.get_mods()
                char = chr(event.key)
                if mods == 2 or mods == 8192:
                    char = char.upper()
                print("%s - %s" % (mods,ord(char)))
                if not ord(char) in ignore:
                    self.buffer += char
            if event.key == pygame.K_RETURN:
                self.application.change_scene(Game(self.buffer))
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
