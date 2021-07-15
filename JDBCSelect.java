package datasourceAutowired;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// You can use the sample postgres database available at /postgres-deploy/README.md
//
// kubectl create secret generic my-datasource --from-file=datasource.properties
// 
// kamel run JDBCSelect.java --dev --build-property quarkus.datasource.camel.db-kind=postgresql 
//                                 --config secret:my-datasource
//                                 -d mvn:io.quarkus:quarkus-jdbc-postgresql
// 

import org.apache.camel.builder.RouteBuilder;

public class JDBCSelect extends RouteBuilder {
  @Override
  public void configure() throws Exception {
   from("timer://foo?period=10000")
   .setBody(constant("SELECT * FROM test LIMIT 5 OFFSET 0"))
   .to("jdbc:camel")
   .to("log:info");
  }

}