package world.gregs.hestia

import world.gregs.hestia.network.NetworkConstants
import world.gregs.hestia.network.codec.Encoder
import world.gregs.hestia.network.codec.Pipeline
import world.gregs.hestia.network.codec.inbound.PacketInboundHandler
import world.gregs.hestia.network.login.LobbyAttempt
import world.gregs.hestia.network.login.LoginHandshake
import world.gregs.hestia.network.packets.PacketLoader
import world.gregs.hestia.network.server.Network
import world.gregs.hestia.network.worlds.WorldsInboundHandler

class LoginServer {

    fun start() {
        val loader = PacketLoader(Settings.get("packetPath", ""))

        //List of game servers (worlds) packets
        val worldPackets = loader.load("world.gregs.hestia.network.worlds.in")
        //Handles communication with servers (worlds)
        val worldHandler = WorldsInboundHandler(worldPackets)

        //List of lobby packets
        val lobbyPackets = loader.load("world.gregs.hestia.network.lobby.in")
        //Handles communication with client lobby's
        val lobbyHandler = PacketInboundHandler(lobbyPackets)

        //List of login requests from client
        val loginPackets = loader.load("world.gregs.hestia.network.login.in")
        //Handles login requests (lobby or direct)
        val loginHandler = LoginHandshake(loginPackets, LobbyAttempt(lobbyHandler))


        //Login server - handles client authentication, worlds & lobby
        val login = Network(name = "Login Server", channel = Pipeline(loginHandler, Encoder()))
        //World server - handles game servers (worlds) connecting/disconnecting
        val server = Network(name = "World Server", channel = Pipeline(worldHandler, Encoder()))

        login.start(NetworkConstants.BASE_PORT)
        server.start(NetworkConstants.BASE_PORT - 1)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Settings.load()
            LoginServer().start()
        }
    }
}