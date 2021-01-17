/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s.dsl.impl

import org.http4s.{AuthedRequest, Request}

trait Auth {
  object as {
    def unapply[F[_], A](ar: AuthedRequest[F, A]): Option[(Request[F], A)] =
      Some(ar.req -> ar.context)
  }
}
