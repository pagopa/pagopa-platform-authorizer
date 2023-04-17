package it.gov.pagopa.authorizer.util;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;

public class ResponseSubscriber implements Flow.Subscriber<ByteBuffer> {

    final HttpResponse.BodySubscriber<String> wrappedResponse;

    public ResponseSubscriber(HttpResponse.BodySubscriber<String> wrappedResponse) {
        this.wrappedResponse = wrappedResponse;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        wrappedResponse.onSubscribe(subscription);
    }

    @Override
    public void onNext(ByteBuffer item) {
        wrappedResponse.onNext(List.of(item));
    }

    @Override
    public void onError(Throwable throwable) {
        wrappedResponse.onError(throwable);
    }

    @Override
    public void onComplete() {
        wrappedResponse.onComplete();
    }
}
