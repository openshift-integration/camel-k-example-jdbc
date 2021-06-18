# How to deploy a simple Postgres DB to Kubernetes cluster

This is a very simple example to show how to create a Postgres database. **Note**, this is not ready for any production purposes.

## Create a Kubernetes Deployment
```
oc create -f postgres-configmap.yaml
oc create -f postgres-storage.yaml
oc create -f postgres-deployment.yaml
oc create -f postgres-service.yaml
```

