# Vert.x tracing example

An example application showing tracing in Vert.x.

Run Jaeger server:

```bash
docker run --net=host jaegertracing/all-in-one:1.12.0
# open browser on http://localhost:16686/search
```

Run the app:
```bash
mvn exec:java -Dexec.mainClass="dev.loffay.App"
curl localhost:8080
```
