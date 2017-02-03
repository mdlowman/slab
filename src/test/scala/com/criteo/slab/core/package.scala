package com.criteo.slab

import com.twitter.util.Future

package object core {

  // Value stores
  object TestStore extends ValueStore {
    override def fetch(id: String, context: Context) = {
      id match {
        case "app.version" =>
          Future.value(Seq("version" -> context.when.getMillis))
        case "app.latency" =>
          Future.value(Seq("latency" -> context.when.getMillis))
        case _ =>
          Future.exception(new Exception("network error"))
      }
    }

    override def upload(id: String, values: Seq[(String, Long)]) = Future.value(())
  }

  // Version check
  case class Version(
                      underlying: Int
                    )

  implicit def versionMetric = new Metrical[Version] {
    override def toMetrics(value: Version): Seq[(String, Long)] = Seq(("version", value.underlying))

    override def fromMetrics(ms: Seq[(String, Long)]): Version = Version(ms.head._2.toInt)
  }

  val versionCheck = Check(
    "app.version",
    "app version",
    () => Future.value(Version(9000)),
    display = (v: Version, context: Context) => View(Status.Success, s"version ${v.underlying}")
  )

  // Latency check
  case class Latency(
                      underlying: Long
                    )

  implicit def latencyMetric = new Metrical[Latency] {
    override def toMetrics(value: Latency): Seq[(String, Long)] = Seq(("latency", value.underlying))

    override def fromMetrics(ms: Seq[(String, Long)]): Latency = Latency(ms.head._2)
  }

  val latencyCheck = Check(
    "app.latency",
    "app latency",
    () => Future.value(Latency(2000)),
    display = (l: Latency, context: Context) => View(Status.Warning, s"latency ${l.underlying}")
  )
}
