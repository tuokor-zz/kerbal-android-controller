import krpc.client.Connection
import krpc.client.services.KRPC
import krpc.client.services.SpaceCenter


fun main() {

    Connection.newInstance().use { conn ->
        val instance = KRPC.newInstance(conn)
        println("instance: ${instance.status.version}")
        val spaceCenter = SpaceCenter.newInstance(conn)
        val vessel = spaceCenter.activeVessel
        println(vessel.name)
    }
}