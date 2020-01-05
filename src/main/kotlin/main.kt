import krpc.client.Connection
import krpc.client.services.KRPC
import krpc.client.services.SpaceCenter
import com.tuokor.kerbal.api.*
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

fun main() {
    Connection.newInstance().use { conn ->
        val instance = KRPC.newInstance(conn)
        println("instance: ${instance.status.version}")
        val spaceCenter = SpaceCenter.newInstance(conn)
        val vessel = spaceCenter.activeVessel
        println(vessel.name)
        val server = KerbalServer(spaceCenter)
        server.start()
        server.blockUntilShutdown()

    }
}



class KerbalAPI(val spaceCenter: SpaceCenter) : KerbalAPIGrpc.KerbalAPIImplBase() {
    override fun vessel(responseObserver: StreamObserver<VesselState>?): StreamObserver<Control> {
        return object : StreamObserver<Control> {
            override fun onNext(value: Control?) {
                value?.let {
                    KerbalServer.logger.info("received value ${it.pitch}")
                    val vessel = spaceCenter.activeVessel
                    vessel.control.pitch = it.pitch
                    vessel.control.yaw = it.yaw
                    //return back values
                    responseObserver?.onNext(VesselState.newBuilder()
                        .setPitch(vessel.control.pitch)
                        .setYaw(vessel.control.yaw)
                        .build())
                }
            }

            override fun onError(t: Throwable?) {
                responseObserver?.onError(t)
            }

            override fun onCompleted() {
                responseObserver?.onCompleted()
            }
        }
    }
}

class KerbalServer(val spaceCenter: SpaceCenter) {
    private var server : Server? = null
    @Throws(IOException::class)
    fun start() {
        /* The port on which the server should run */
        val port = 50051
        server = ServerBuilder.forPort(port)
            .addService(KerbalAPI(spaceCenter))
            .build()
            .start()
        logger.log(Level.INFO, "Server started, listening on {0}", port)
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@KerbalServer.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    private fun stop() {
        server?.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    fun blockUntilShutdown() {
        server?.awaitTermination()
    }


    companion object {
        val logger = Logger.getLogger(KerbalServer::class.java.name)
    }
}