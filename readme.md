# Camel K JDBC Quickstart

This example demonstrates how to get started with `Camel K` and an SQL database via `JDBC` drivers. We will show how to quickly set up an integration producing data into a Postgres database (you can use any relational database of your choice). We will show also how to read data from the same database.

The quickstart is based on the [Apache Camel K upstream database examples](https://github.com/apache/camel-k/tree/release-1.10.x/examples/databases/).


## Preparing the cluster

This example can be run on any OpenShift 4.3+ cluster or a local development instance (such as [CRC](https://github.com/code-ready/crc)). Ensure that you have a cluster available and login to it using the OpenShift `oc` command line tool.

You need to create a new project named `camel-k-jdbc` for running this example. This can be done directly from the OpenShift web console or by executing the command `oc new-project camel-k-jdbc` on a terminal window.

```
oc new-project camel-k-jdbc
```

You need to install the Camel K operator in the `camel-k-jdbc` project. To do so, go to the OpenShift 4.x web console, login with a cluster admin account and use the OperatorHub menu item on the left to find and install **"Red Hat Integration - Camel K"**. You will be given the option to install it globally on the cluster or on a specific namespace.
If using a specific namespace, make sure you select the `camel-k-jdbc` project from the dropdown list.
This completes the installation of the Camel K operator (it may take a couple of minutes).

When the operator is installed, from the OpenShift Help menu ("?") at the top of the WebConsole, you can access the "Command Line Tools" page, where you can download the **"kamel"** CLI, that is required for running this example. The CLI must be installed in your system path.

Refer to the **"Red Hat Integration - Camel K"** documentation for a more detailed explanation of the installation steps for the operator and the CLI.

You can use the following section to check if your environment is configured properly.

## Requirements

**OpenShift CLI ("oc")**

The OpenShift CLI tool ("oc") will be used to interact with the OpenShift cluster.

**Connection to an OpenShift cluster**

In order to execute this demo, you will need to have an OpenShift cluster with the correct access level, the ability to create projects and install operators as well as the Apache Camel K CLI installed on your local system.

**Apache Camel K CLI ("kamel")**

Apart from the support provided by the VS Code extension, you also need the Apache Camel K CLI ("kamel") in order to access all Camel K features.

### Optional Requirements

The following requirements are optional. They don't prevent the execution of the demo, but may make it easier to follow.

**VS Code Extension Pack for Apache Camel**

The VS Code Extension Pack for Apache Camel by Red Hat provides a collection of useful tools for Apache Camel K developers, such as code completion and integrated lifecycle management. They are **recommended** for the tutorial, but they are **not** required.

You can install it from the VS Code Extensions marketplace.

## 1. Preparing the project

We'll connect to the `camel-k-jdbc` project and check the installation status. To change project, open a terminal tab and type the following command:

```
oc project camel-k-jdbc
```

We should now check that the operator is installed. To do so, execute the following command on a terminal:

```
oc get csv
```

When Camel K is installed, you should find an entry related to `red-hat-camel-k-operator` in phase `Succeeded`.

You can now proceed to the next section.

## 2. Preparing the database instance

We assume you already have a database up and running. If it's not the case, you can easily create a new instance on [Openshift Online](https://www.openshift.com/products/online/) or [create your own Postgres sample database](https://docs.openshift.com/online/pro/using_images/db_images/postgresql.html). You can also deploy any other database instance through a wizard using the _+Add_ button on your **Openshift Console**.

### Prepare the database

For this tutorial, we're using a `Postgres` database service named `postgresql` deployed in the same `camel-k-jdbc` project. In order to configure the `JDBC` you will need to get the following settings: the `JDBC url`, `driver`, `username` and `password`. You may require assistance from your database administrator if you are not managing directly the database instance.

To set up the database tables and some test data you can use the sample postgres database available at the [Apache Camel K upstream database examples](https://github.com/apache/camel-k/tree/release-1.8.x/examples/databases/postgres-deploy)                          

If you are using the Postgres instance mentioned above, you will set up the credentials as part of the deployment process.

> **NOTE**: make sure your Openshift cluster have connectivity with your database if it's deployed outside the cluster.

## Setting up complementary database

This example uses a PostgreSQL database. We want to install it on the project `camel-transformations`. We can go to the OpenShift 4.x WebConsole page, use the OperatorHub menu item on the left hand side menu and use it to find and install "Crunchy Postgres for Kubernetes". This will install the operator and may take a couple of minutes to install.

Once the operator is installed, we can create a new database using

```
oc create -f test/resources/postgres.yaml
```

We connect to the database pod to create a table and add data to be extracted later.

```
oc rsh $(oc get pods -l postgres-operator.crunchydata.com/role=master -o name)
```

```
psql -U postgres test \
-c "CREATE TABLE test (data TEXT PRIMARY KEY);
INSERT INTO test(data) VALUES ('hello'), ('world');
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgresadmin;"
```
```
exit
```

Now, we need to find out Postgres username, password and hostname and update the values in the `datasource.properties``.

```
USER_NAME=$(oc get secret postgres-pguser-postgresadmin --template={{.data.user}} | base64 -d)
USER_PASSWORD=$(oc get secret postgres-pguser-postgresadmin --template={{.data.password}} | base64 -d)
HOST=$(oc get secret postgres-pguser-postgresadmin --template={{.data.host}} | base64 -d)
PASSWORD_SKIP_SPEC_CHAR=$(sed -e 's/[&\\/]/\\&/g; s/$/\\/' -e '$s/\\$//' <<<"$USER_PASSWORD")

sed -i "s/^quarkus.datasource.username=.*/quarkus.datasource.username=$USER_NAME/" datasource.properties
sed -i "s/^quarkus.datasource.password=.*/quarkus.datasource.password=$PASSWORD_SKIP_SPEC_CHAR/" datasource.properties
sed -i "s/^quarkus.datasource.jdbc.url=.*/quarkus.datasource.jdbc.url=jdbc:postgresql:\/\/$HOST:5432\/test/" datasource.properties
```

For macOS use these commands:
```
sed -i '' "s/^quarkus.datasource.username=.*/quarkus.datasource.username=$USER_NAME/" datasource.properties
sed -i '' "s/^quarkus.datasource.password=.*/quarkus.datasource.password=$PASSWORD_SKIP_SPEC_CHAR/" datasource.properties
sed -i '' "s/^quarkus.datasource.jdbc.url=.*/quarkus.datasource.jdbc.url=jdbc:postgresql:\/\/$HOST:5432\/test/" datasource.properties
```

### Setting cluster secret

You should set a Kubernetes `Secret` in order to avoid exposing sensitive information. You can bundle the configuration expected by the application in a secret. For convenience we have put them into a file named `datasource.properties`, however, they can be provided in the cluster differently. Please, notice that these values have to correspond to the ones expected by your instance, so, you can replace the values provided in our examples with yours.

```
oc create secret generic my-datasource --from-file=datasource.properties
```

## 3. Running a JDBC Producer (SQL insert)

At this stage, run a producer integration. This one will insert a row in a `test` table, every 10 seconds. 

```
kamel run JDBCInsert.java --dev --config secret:my-datasource
```

Please, notice that we must specify certain dependencies that will be needed at runtime. In particular, we will have to provide the `JDBC driver` that is expected by the database. In our example, we're using the `Postgresql` drivers: make sure you pick up the proper driver according to your database instance.

The producer will create a new message and push into the database and log some information.

```
...
[2] 2021-06-18 13:39:22,590 INFO  [info] (Camel (camel-1) thread #0 - timer://sql-insert) Exchange[ExchangePattern: InOnly, BodyType: String, Body: INSERT INTO test (data) VALUES ('message #3')]
[2] 2021-06-18 13:39:32,574 INFO  [info] (Camel (camel-1) thread #0 - timer://sql-insert) Exchange[ExchangePattern: InOnly, BodyType: String, Body: INSERT INTO test (data) VALUES ('message #4')]
...
```

## 4. Running a JDBC Consumer (SQL select)

Now we can run a consumer integration. This one will read 5 rows from a `test` table, every 10 seconds.

```
kamel run JDBCSelect.java --dev --config secret:my-datasource
```

Also, here we had to specify certain dependencies that may change if you use a different database. A consumer will start logging the events found in the table:

```
...
[1] 2021-06-18 13:40:05,609 INFO  [info] (Camel (camel-1) thread #0 - timer://foo) Exchange[ExchangePattern: InOnly, BodyType: java.util.ArrayList, Body: [{data=message #1}, {data=message #2}, {data=message #3}, {data=message #4}]]
...
```

> **NOTE**: You may have run the consumer and producer as separate routes in the same integration.

## 5. Stop Integrations

Your integrations are running in the background terminal processes.

**To exit dev mode and terminate the execution**, hit `ctrl+c` on the terminal window.
## 6. Uninstall

To clean up everything, execute the following command:

```
oc delete project camel-k-jdbc
```
