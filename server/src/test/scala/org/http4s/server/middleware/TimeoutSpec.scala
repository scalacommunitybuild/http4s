/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s
package server
package middleware

import cats.data.OptionT
import cats.effect._
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import org.http4s.Uri.uri
import org.http4s.dsl.io._
import org.http4s.testing.Http4sLegacyMatchersIO
import scala.concurrent.duration._

class TimeoutSpec extends Http4sSpec with Http4sLegacyMatchersIO {
  // To distinguish from the inherited cats-effect-testing Timeout
  import org.http4s.server.middleware.{Timeout => TimeoutMiddleware}

  val routes = HttpRoutes.of[IO] {
    case _ -> Root / "fast" =>
      Ok("Fast")

    case _ -> Root / "never" =>
      IO.async[Response[IO]] { _ =>
        ()
      }
  }

  val app = TimeoutMiddleware(5.milliseconds)(routes).orNotFound

  val fastReq = Request[IO](GET, uri("/fast"))
  val neverReq = Request[IO](GET, uri("/never"))

  def checkStatus(resp: IO[Response[IO]], status: Status) =
    resp.unsafeRunTimed(3.seconds).getOrElse(throw new TimeoutException) must haveStatus(status)

  "Timeout Middleware" should {
    "have no effect if the response is timely" in {
      val app = TimeoutMiddleware(365.days)(routes).orNotFound
      checkStatus(app(fastReq), Status.Ok)
    }

    "return a 503 error if the result takes too long" in {
      checkStatus(app(neverReq), Status.ServiceUnavailable)
    }

    "return the provided response if the result takes too long" in {
      val customTimeout = Response[IO](Status.GatewayTimeout) // some people return 504 here.
      val altTimeoutService =
        TimeoutMiddleware(1.nanosecond, OptionT.pure[IO](customTimeout))(routes)
      checkStatus(altTimeoutService.orNotFound(neverReq), customTimeout.status)
    }

    "cancel the loser" in {
      val canceled = new AtomicBoolean(false)
      val routes = HttpRoutes.of[IO] {
        case _ =>
          IO.never.guarantee(IO(canceled.set(true)))
      }
      val app = TimeoutMiddleware(1.millis)(routes).orNotFound
      checkStatus(app(Request[IO]()), Status.ServiceUnavailable)
      // Give the losing response enough time to finish
      canceled.get must beTrue.eventually
    }
  }
}
