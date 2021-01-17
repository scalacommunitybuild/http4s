/*
 * Copyright 2013-2021 http4s.org
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.http4s.client

import java.io.IOException
import java.net.InetSocketAddress

/** Indicates a failure to establish a client connection, preserving the request key
  * that we tried to connect to.
  */
class ConnectionFailure(
    val requestKey: RequestKey,
    val upstream: InetSocketAddress,
    val cause: Throwable)
    extends IOException(cause) {
  override def getMessage(): String =
    s"Error connecting to $requestKey using address ${upstream.getHostString}:${upstream.getPort} (unresolved: ${upstream.isUnresolved})"
}

object ConnectionFailure {
  def unapply(failure: ConnectionFailure): Option[(RequestKey, InetSocketAddress, Throwable)] =
    Some((failure.requestKey, failure.upstream, failure.cause))
}
