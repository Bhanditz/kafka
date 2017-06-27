/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kafka.common.security.auth;

import java.security.Principal;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.network.Authenticator;
import org.apache.kafka.common.network.TransportLayer;

/*
 * CommonNamePrincipalBuilder, get the common name from cert to build a KafkaPrincipal
 */

public class CommonNamePrincipalBuilder implements PrincipalBuilder {

  /**
   * Configures this class with given key-value pairs.
   */
  @Override
  public void configure(Map<String, ?> configs) {

  }

  /**
   * Returns Principal, this builder will extract the common name from cert.
   *
   * Kafka uses the DefaultPrincipalBuilder by default, `CN=writeuser,OU=Unknown,O=Unknown,L=Unknown,ST=Unknown,C=Unknown`.
   *
   * This class gets the Common Name, which is `writeuser`
   */
  @Override
  public Principal buildPrincipal(TransportLayer transportLayer,
                                  Authenticator authenticator) throws KafkaException {
    try {
      Principal principal = transportLayer.peerPrincipal();

      if (!(principal instanceof X500Principal)) {
        return principal;
      }

      String commonName = getCommonName(principal);

      Principal kafkaPrincipal = new KafkaPrincipal(KafkaPrincipal.USER_TYPE, commonName);
      return kafkaPrincipal;
    } catch (Exception e) {
      throw new KafkaException("Failed to build Kafka principal due to: ", e);
    }
  }

  private String getCommonName(Principal principal){
    String distinguishedName = principal.getName();

    // Dname example: `CN=writeuser,OU=Unknown,O=Unknown,L=Unknown,ST=Unknown,C=Unknown`
    return distinguishedName.split(",", 6)[0].split("=")[1];
  }

  /**
   * Closes this instance.
   */
  @Override
  public void close() throws KafkaException {
  }
}
