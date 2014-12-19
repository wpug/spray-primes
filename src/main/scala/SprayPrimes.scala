import akka.actor.{Actor, ActorSystem, Props}
import scala.concurrent.duration._
import spray.routing.{RequestContext, SimpleRoutingApp}
import akka.pattern.ask
import akka.util.Timeout

// Przykładowe komunikaty
sealed abstract class Msg
case class Arg(n: Int) extends Msg
case class Res(n: Int) extends Msg

object Main3 extends App with SimpleRoutingApp {
  implicit val system = ActorSystem()
  import system.dispatcher // pula wątków
  implicit val timeout = Timeout(5.seconds)

  startServer(interface = "localhost", port = 3000) {
    get {
      path("getprimes" / IntNumber) { arg =>
        complete {
          (system.actorOf(Props[MyActor]) ? Arg(arg)).mapTo[Res]
          .map(el => el match {
            case Res(k) => s"wynik to $k"
            case _ => "to się nie mogło zdarzyć …"
          })
        } // : Route
      } // : Route
    } // : Route
  }

  class MyActor extends Actor {
     def receive = {
      case Arg(n) =>
        sender() ! Res(n * 2)
     }
  }

}
