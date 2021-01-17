/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s
package testing

import cats.effect.{ContextShift, IO, Timer}
import org.specs2.matcher.{IOMatchers => Specs2IOMatchers}

trait Http4sLegacyMatchersIO extends Http4sLegacyMatchers[IO] with Specs2IOMatchers {
  self: Http4sSpec =>
  override val catsEffectContextShift: ContextShift[IO] = contextShift
  override val catsEffectTimer: Timer[IO] = timer
}
