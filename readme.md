# Camel K JDBC Quickstart

This example demonstrates how to get started with `Camel K` and an SQL database via `JDBC` drivers. We will show how to quickly set up an integration producing data into a Postgres database (you can use any relational database of your choice). We will show also how to read data from the same database.

The quickstart is based on the [Apache Camel K upstream database examples](https://github.com/apache/camel-k/blob/main/examples/databases/).

## Preparing the database instance

We assume you already have a database up and running. If it's not the case, you can create an instance of `Postgres` on your Openshift cluster by setting the following yaml descriptors:

```
oc create -f postgres-configmap.yaml
oc create -f postgres-storage.yaml
oc create -f postgres-deployment.yaml
oc create -f postgres-service.yaml
```

NOTE: please, consider the above as a simple database deployment to run this tutorial. It cannot be considered ready for any production purposes. If you use another database, the procedure may be different according to each database vendors.

### Prepare the database

In order to configure the `JDBC` you will need to get the following settings: the `JDBC url`, `driver`, `username` and `password`. You may require assistance from your database administrator if you're not managing directly the database instance.

If you are using the Postgres instance mentioned above, you will find the credentials in the _postgres-configmap.yaml_ file.

> **NOTE**: make sure your Openshift cluster have connectivity with your database.

## Preparing the cluster

This example can be run on any OpenShift 4.3+ cluster or a local development instance (such as [CRC](https://github.com/code-ready/crc)). Ensure that you have a cluster available and login to it using the OpenShift `oc` command line tool.

You need to create a new project named `camel-k-jdbc` for running this example. This can be done directly from the OpenShift web console or by executing the command `oc new-project camel-k-jdbc` on a terminal window.

You need to install the Camel K operator in the `camel-k-jdbc` project. To do so, go to the OpenShift 4.x web console, login with a cluster admin account and use the OperatorHub menu item on the left to find and install **"Red Hat Integration - Camel K"**. You will be given the option to install it globally on the cluster or on a specific namespace.
If using a specific namespace, make sure you select the `camel-k-jdbc` project from the dropdown list.
This completes the installation of the Camel K operator (it may take a couple of minutes).

When the operator is installed, from the OpenShift Help menu ("?") at the top of the WebConsole, you can access the "Command Line Tools" page, where you can download the **"kamel"** CLI, that is required for running this example. The CLI must be installed in your system path.

Refer to the **"Red Hat Integration - Camel K"** documentation for a more detailed explanation of the installation steps for the operator and the CLI.

You can use the following section to check if your environment is configured properly.

### Optional Requirements

The following requirements are optional. They don't prevent the execution of the demo, but may make it easier to follow.

**VS Code Extension Pack for Apache Camel**

The VS Code Extension Pack for Apache Camel by Red Hat provides a collection of useful tools for Apache Camel K developers, such as code completion and integrated lifecycle management. They are **recommended** for the tutorial, but they are **not** required.

You can install it from the VS Code Extensions marketplace.

## 1. Preparing the project

We'll connect to the `camel-k-jdbc` project and check the installation status.

To change project, open a terminal tab and type the following command:

```
oc project camel-k-jdbc
```

We should now check that the operator is installed. To do so, execute the following command on a terminal:

```
oc get csv
```

When Camel K is installed, you should find an entry related to `red-hat-camel-k-operator` in phase `Succeeded`.

You can now proceed to the next section.

## 2. Running a JDBC Producer (SQL insert)

At this stage, run a producer integration. This one will insert a row in a `test` table, every 10 seconds. Before executing the `Integration` you will have to change the `JDBC username`, `password` `driver` and `url` that you can find in _JDBCInsert.java_ at lines 38 to 41. Please, notice that these values have to correspond to the ones expected by your instance, so, you can replace the values provided in our examples with yours.

```
kamel run JDBCInsert.java --dev -d mvn:org.postgresql:postgresql:42.2.21 -d mvn:org.apache.commons:commons-dbcp2:2.8.0
```

Please, notice that we must specify certain dependencies that will be needed at runtime. In particular, we will have to provide the `JDBC driver` that is expected by the database. In our example, we're using the `Postgresql` drivers: make sure you pick up the proper driver according to your database instance.

The producer will create a new message and push into the database and log some information.

```
...
???
...
```

## 3. Running a JDBC Consumer (SQL select)

Now we can run a consumer integration. This one will read 5 rows from a `test` table, every 10 seconds. Before executing the `Integration` you will have to change the `JDBC username`, `password` `driver` and `url` that you can find in _JDBCSelect.java_ at lines 38 to 41 in the same way you did in the previous section.

```
kamel run JDBCSelect.java --dev -d mvn:org.postgresql:postgresql:42.2.21 -d mvn:org.apache.commons:commons-dbcp2:2.8.0
```

Also here we had to specify certain dependencies that may change if you use a different database. A consumer will start logging the events found in the table:

```
???

```

> **NOTE**: You may have run the consumer and producer as separate routes in the same integration.

## 5. Uninstall

To cleanup everything, execute the following command:

```
oc delete project camel-k-jdbc
```
