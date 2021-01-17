/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s.websocket

import org.specs2.mutable.Specification

class WebSocketHandshakeSpec extends Specification {
  "WebSocketHandshake" should {
    "Be able to split multi value header keys" in {
      val totalValue = "keep-alive, Upgrade"
      val values = List("upgrade", "Upgrade", "keep-alive", "Keep-alive")
      values.foldLeft(true) { (b, v) =>
        b && WebSocketHandshake.valueContains(v, totalValue)
      } should_== true
    }

    "Do a round trip" in {
      val client = WebSocketHandshake.clientHandshaker("www.foo.com")
      val valid = WebSocketHandshake.serverHandshake(client.initHeaders)
      valid must beRight

      val Right(headers) = valid
      client.checkResponse(headers) must beRight
    }
  }
}
