# Camel K JDBC Quickstart

This example demonstrates how to get started with `Camel K` and an SQL database via `JDBC` drivers. We will show how to quickly set up an integration producing data into a Postgres database (you can use any relational database of your choice). We will show also how to read data from the same database.

The quickstart is based on the [Apache Camel K upstream database examples](https://github.com/apache/camel-k/blob/main/examples/databases/).

## Before you begin

Make sure you check-out this repository from git and open it with [VSCode](https://code.visualstudio.com/).

Instructions are based on [VSCode Didact](https://github.com/redhat-developer/vscode-didact), so make sure it's installed
from the VSCode extensions marketplace.

From the VSCode UI, right-click on the `readme.didact.md` file and select "Didact: Start Didact tutorial from File". A new Didact tab will be opened in VS Code.

Make sure you've opened this readme file with Didact before jumping to the next section.

## Preparing the cluster

This example can be run on any OpenShift 4.3+ cluster or a local development instance (such as [CRC](https://github.com/code-ready/crc)). Ensure that you have a cluster available and login to it using the OpenShift `oc` command line tool.

You need to create a new project named `camel-k-jdbc` for running this example. This can be done directly from the OpenShift web console or by executing the command `oc new-project camel-k-jdbc` on a terminal window.

```
oc new-project camel-k-jdbc
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20new-project%20camel-k-jdbc&completion=Project%20changed. "Switched to the project that will run Camel K JDBC example"){.didact})

You need to install the Camel K operator in the `camel-k-jdbc` project. To do so, go to the OpenShift 4.x web console, login with a cluster admin account and use the OperatorHub menu item on the left to find and install **"Red Hat Integration - Camel K"**. You will be given the option to install it globally on the cluster or on a specific namespace.
If using a specific namespace, make sure you select the `camel-k-jdbc` project from the dropdown list.
This completes the installation of the Camel K operator (it may take a couple of minutes).

When the operator is installed, from the OpenShift Help menu ("?") at the top of the WebConsole, you can access the "Command Line Tools" page, where you can download the **"kamel"** CLI, that is required for running this example. The CLI must be installed in your system path.

Refer to the **"Red Hat Integration - Camel K"** documentation for a more detailed explanation of the installation steps for the operator and the CLI.

You can use the following section to check if your environment is configured properly.

## Requirements

<a href='didact://?commandId=vscode.didact.validateAllRequirements' title='Validate all requirements!'><button>Validate all Requirements at Once!</button></a>

**OpenShift CLI ("oc")**

The OpenShift CLI tool ("oc") will be used to interact with the OpenShift cluster.

[Check if the OpenShift CLI ("oc") is installed](didact://?commandId=vscode.didact.cliCommandSuccessful&text=oc-requirements-status$$oc%20help&completion=Checked%20oc%20tool%20availability "Tests to see if `oc help` returns a 0 return code"){.didact}

*Status: unknown*{#oc-requirements-status}

**Connection to an OpenShift cluster**

In order to execute this demo, you will need to have an OpenShift cluster with the correct access level, the ability to create projects and install operators as well as the Apache Camel K CLI installed on your local system.

[Check if you're connected to an OpenShift cluster](didact://?commandId=vscode.didact.requirementCheck&text=cluster-requirements-status$$oc%20get%20project%20camel-k-jdbc&completion=OpenShift%20is%20connected. "Tests to see if `oc get project` returns a result"){.didact}

*Status: unknown*{#cluster-requirements-status}

**Apache Camel K CLI ("kamel")**

Apart from the support provided by the VS Code extension, you also need the Apache Camel K CLI ("kamel") in order to access all Camel K features.

[Check if the Apache Camel K CLI ("kamel") is installed](didact://?commandId=vscode.didact.requirementCheck&text=kamel-requirements-status$$kamel%20version$$Camel%20K%20Client&completion=Apache%20Camel%20K%20CLI%20is%20available%20on%20this%20system. "Tests to see if `kamel version` returns a result"){.didact}

*Status: unknown*{#kamel-requirements-status}

### Optional Requirements

The following requirements are optional. They don't prevent the execution of the demo, but may make it easier to follow.

**VS Code Extension Pack for Apache Camel**

The VS Code Extension Pack for Apache Camel by Red Hat provides a collection of useful tools for Apache Camel K developers, such as code completion and integrated lifecycle management. They are **recommended** for the tutorial, but they are **not** required.

You can install it from the VS Code Extensions marketplace.

[Check if the VS Code Extension Pack for Apache Camel by Red Hat is installed](didact://?commandId=vscode.didact.extensionRequirementCheck&text=extension-requirement-status$$redhat.apache-camel-extension-pack&completion=Camel%20extension%20pack%20is%20available%20on%20this%20system. "Checks the VS Code workspace to make sure the extension pack is installed"){.didact}

*Status: unknown*{#extension-requirement-status}

## 1. Preparing the project

We'll connect to the `camel-k-jdbc` project and check the installation status. To change project, open a terminal tab and type the following command:

```
oc project camel-k-jdbc
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20project%20camel-k-jdbc&completion=Project%20changed. "Switched to the project that will run Camel K JDBC example"){.didact})

We should now check that the operator is installed. To do so, execute the following command on a terminal:

```
oc get csv
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20get%20csv&completion=Checking%20Cluster%20Service%20Versions. "Opens a new terminal and sends the command above"){.didact})

When Camel K is installed, you should find an entry related to `red-hat-camel-k-operator` in phase `Succeeded`.

You can now proceed to the next section.

## 2. Preparing the database instance

We assume you already have a database up and running. If it's not the case, you can easily create a new instance on [Openshift Online](https://www.openshift.com/products/online/) or [create your own Postgres sample database](https://docs.openshift.com/online/pro/using_images/db_images/postgresql.html). You can also deploy any other database instance through a wizard using the _+Add_ button on your **Openshift Console**.

### Prepare the database

For this tutorial, we're using a `Postgres` database service named `postgresql` deployed in the same `camel-k-jdbc` project. In order to configure the `JDBC` you will need to get the following settings: the `JDBC url`, `driver`, `username` and `password`. You may require assistance from your database administrator if you're not managing directly the database instance.

If you are using the Postgres instance mentioned above, you will setup the credentials as part of the deployment process.

> **NOTE**: make sure your Openshift cluster have connectivity with your database if it's deployed outside the cluster.

### Setting cluster secret

You should set a Kubernetes `Secret` in order to avoid exposing sensitive information. You can bundle the configuration expected by the application in a secret. For convenience we have put them into a file named `datasource.properties`, however, they can be provided in the cluster differently.

```
oc create secret generic my-datasource --from-file=datasource.properties
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20create%20secret%20generic%20my-datasource%20--from-file=datasource.properties&completion=Secret%20created. "Opens a new terminal and sends the command above"){.didact})

## 3. Running a JDBC Producer (SQL insert)

At this stage, run a producer integration. This one will insert a row in a `test` table, every 10 seconds. Before executing the `Integration` you will have to change the `JDBC username`, `password` `driver` and `url` that you can find in _JDBCInsert.java_ at lines 38 to 41. Please, notice that these values have to correspond to the ones expected by your instance, so, you can replace the values provided in our examples with yours.

```
kamel run JDBCInsert.java --dev --build-property quarkus.datasource.camel.db-kind=postgresql --config secret:my-datasource -d mvn:io.quarkus:quarkus-jdbc-postgresql:1.13.7.Final
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kamel%20run%20JDBCInsert.java%20--dev%20--build-property%20quarkus.datasource.camel.db-kind=postgresql%20--config%20secret:my-datasource%20-d%20mvn:io.quarkus:quarkus-jdbc-postgresql:1.13.7.Final&completion=Camel%20K%20integration%20run%20in%20dev%20mode. "Opens a new terminal and sends the command above"){.didact})

Please, notice that we must specify certain dependencies that will be needed at runtime. In particular, we will have to provide the `JDBC driver` that is expected by the database. In our example, we're using the `Postgresql` drivers: make sure you pick up the proper driver according to your database instance.

The producer will create a new message and push into the database and log some information.

```
...
[2] 2021-06-18 13:39:22,590 INFO  [info] (Camel (camel-1) thread #0 - timer://sql-insert) Exchange[ExchangePattern: InOnly, BodyType: String, Body: INSERT INTO test (data) VALUES ('message #3')]
[2] 2021-06-18 13:39:32,574 INFO  [info] (Camel (camel-1) thread #0 - timer://sql-insert) Exchange[ExchangePattern: InOnly, BodyType: String, Body: INSERT INTO test (data) VALUES ('message #4')]
...
```

## 4. Running a JDBC Consumer (SQL select)

Now we can run a consumer integration. This one will read 5 rows from a `test` table, every 10 seconds. Before executing the `Integration` you will have to change the `JDBC username`, `password` `driver` and `url` that you can find in _JDBCSelect.java_ at lines 38 to 41 in the same way you did in the previous section.

```
kamel run JDBCSelect.java --dev --build-property quarkus.datasource.camel.db-kind=postgresql --config secret:my-datasource -d mvn:io.quarkus:quarkus-jdbc-postgresql:1.13.7.Final
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$kamel%20run%20JDBCSelect.java%20--dev%20--build-property%20quarkus.datasource.camel.db-kind=postgresql%20--config%20secret:my-datasource%20-d%20mvn:io.quarkus:quarkus-jdbc-postgresql:1.13.7.Final&completion=Camel%20K%20integration%20run%20in%20dev%20mode. "Opens a new terminal and sends the command above"){.didact})

Also here we had to specify certain dependencies that may change if you use a different database. A consumer will start logging the events found in the table:

```
...
[1] 2021-06-18 13:40:05,609 INFO  [info] (Camel (camel-1) thread #0 - timer://foo) Exchange[ExchangePattern: InOnly, BodyType: java.util.ArrayList, Body: [{data=message #1}, {data=message #2}, {data=message #3}, {data=message #4}]]
...
```

> **NOTE**: You may have run the consumer and producer as separate routes in the same integration.

## 5. Uninstall

To cleanup everything, execute the following command:

```
oc delete project camel-k-jdbc
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=camelTerm$$oc%20delete%20project%20camel-k-jdbc&completion=Removed%20the%20project%20from%20the%20cluster. "Cleans up the cluster after running the example"){.didact})
