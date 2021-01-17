/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s.server.middleware

import cats.effect.IO
import cats.effect.concurrent.Ref
import io.chrisdavenport.vault.Vault
import org.http4s._
import org.http4s.Request.Connection
import org.http4s.Uri.uri
import org.http4s.dsl.io._
import org.http4s.testing.Http4sLegacyMatchersIO

import java.net.{InetAddress, InetSocketAddress}

class ErrorActionSpec extends Http4sSpec with Http4sLegacyMatchersIO {
  val remote = "192.168.0.1"

  def httpRoutes(error: Throwable = new RuntimeException()) =
    HttpRoutes.of[IO] {
      case GET -> Root / "error" => IO.raiseError(error)
    }

  val req = Request[IO](
    GET,
    uri("/error"),
    attributes = Vault.empty.insert(
      Request.Keys.ConnectionInfo,
      Connection(
        new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 80),
        new InetSocketAddress(InetAddress.getByName(remote), 80),
        false
      )
    )
  )

  def testApp(app: Ref[IO, Vector[String]] => HttpApp[IO], expected: Vector[String])(
      req: Request[IO]) =
    (for {
      logsRef <- Ref.of[IO, Vector[String]](Vector.empty)
      _ <- app(logsRef).run(req).attempt
      logs <- logsRef.get
    } yield logs) must returnValue[Vector[String]](expected)

  def testHttpRoutes(
      httpRoutes: Ref[IO, Vector[String]] => HttpRoutes[IO],
      expected: Vector[String]
  ) =
    testApp(logsRef => httpRoutes(logsRef).orNotFound, expected)(_)

  "ErrorAction" >> {
    "default middleware" should {

      "run the given function when an error happens" in {
        testApp(
          logsRef =>
            ErrorAction(
              httpRoutes().orNotFound,
              (_: Request[IO], _) => logsRef.getAndUpdate(_ :+ "Error was handled").void
            ),
          Vector("Error was handled")
        )(req)
      }

      "be created via httpApp constructor" in {
        testApp(
          logsRef =>
            ErrorAction.httpApp(
              httpRoutes().orNotFound,
              (_: Request[IO], _) => logsRef.getAndUpdate(_ :+ "Error was handled").void
            ),
          Vector("Error was handled")
        )(req)
      }

      "be created via httRoutes constructor" in {
        testHttpRoutes(
          logsRef =>
            ErrorAction.httpRoutes(
              httpRoutes(),
              (_: Request[IO], _) => logsRef.getAndUpdate(_ :+ "Error was handled").void
            ),
          Vector("Error was handled")
        )(req)
      }
    }

    "log middleware" should {
      "provide prebaked error message in case of a runtime error" in {
        testApp(
          logsRef =>
            ErrorAction.log(
              httpRoutes().orNotFound,
              (_, _) => IO.unit,
              (_, message) => logsRef.getAndUpdate(_ :+ message).void
            ),
          Vector(s"Error servicing request: GET /error from $remote")
        )(req)
      }

      "provide prebaked error message in case of a message failure" in {
        testApp(
          logsRef =>
            ErrorAction.log(
              httpRoutes(ParseFailure("some-erroneous-message", "error")).orNotFound,
              (_, message) => logsRef.getAndUpdate(_ :+ message).void,
              (_, _) => IO.unit
            ),
          Vector(s"Message failure handling request: GET /error from $remote")
        )(req)
      }

      "should be created via httpApp.log constructor" in {
        testHttpRoutes(
          logsRef =>
            ErrorAction.httpRoutes.log(
              httpRoutes(),
              (_, _) => IO.unit,
              (_, message) => logsRef.getAndUpdate(_ :+ message).void
            ),
          Vector(s"Error servicing request: GET /error from $remote")
        )(req)
      }
    }
  }
}
