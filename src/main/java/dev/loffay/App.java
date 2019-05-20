package dev.loffay;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.opentracing.Tracer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.tracing.TracingOptions;
import io.vertx.tracing.opentracing.OpenTracingOptions;

/**
 * Hello world!
 *
 */
public class App extends AbstractVerticle {

    private static final Tracer tracer = Configuration.fromEnv("vertx")
            .withSampler(new SamplerConfiguration().withParam(1).withType("const")).getTracer();

    public static void main(String[] args) {
        TracingOptions tracingOptions = new OpenTracingOptions(tracer)
            .setEnabled(true);
        Vertx vertx = Vertx.vertx(new VertxOptions().setTracingOptions(tracingOptions));
        vertx.deployVerticle(new App());

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(request -> {
            System.out.printf("http server span: %s\n", tracer.activeSpan());
            /**
             * TODO this is null on first request after deploying
             * TODO Once it is not null it represents event bus event, but the tag is not
             * added because the span has been already reporter to the server.
             */
            if (tracer.activeSpan() != null) {
                tracer.activeSpan().setTag("custom-tag", "before-publish");
            }
            vertx.eventBus().publish("foo", "message from http");
            /**
             * TODO: Span changed to "publish":client from event bus.
             * TODO: The tag is not attached to the span because it might have been already sent to the server
             */
            tracer.activeSpan().setTag("custom-tag2", "after-publish");
            System.out.printf("http server span: %s\n", tracer.activeSpan());
            request.response().end("look for trace " + tracer.activeSpan());
        });
        server.listen(8080);
    }

    @Override
    public void start(Future<Void> startFuture) {
        getVertx().eventBus().consumer("foo").handler(event -> {
            System.out.printf("foo -> %s\n", event.body());
            /**
             * TODO this is always null
             */
            System.out.printf("event bus span: %s\n", tracer.activeSpan());
        });
    }
}

