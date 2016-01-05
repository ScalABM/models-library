import scala.concurrent.duration._
import scala.util.Random

def waitTime(prng: Random, rate: Double): FiniteDuration = {
  (-Math.log(prng.nextDouble()) / rate).micros  // @todo get rid of implicit conversions!
}

val prng = new Random(42)

waitTime(prng, 1)
waitTime(prng, 1.5)
waitTime(prng, 2.0)
waitTime(prng, 0.001)

Duration.fromNanos(5.5)