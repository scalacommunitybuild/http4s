/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s

import cats.kernel.laws.discipline.OrderTests
import org.http4s.laws.discipline.HttpCodecTests
import cats.implicits._

class MediaRangeSpec extends Http4sSpec {
  checkAll("Order[MediaRange]", OrderTests[MediaRange].order)
  checkAll("HttpCodec[MediaRange]", HttpCodecTests[MediaRange].httpCodec)
}
