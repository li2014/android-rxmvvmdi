package com.futurice.rxmvvmdi.viewmodels;

import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/**
 * RxProperty based on the https://github.com/k-kagurazaka/rx-property-android ReactiveProperty port.
 * The main differences between this and the original are that this class:
 * - can be bound and unbound multiple times
 * - can be directly set as an observer in an Rx subscription
 * - supports chaining by exposing the internal Rx value stream
 * - automatically observes on the main thread
 */
public class RxProperty<T> implements rx.Observer<T> {

    private static final String TAG = RxProperty.class.getName();

    private final SerializedSubject<T, T> subject = new SerializedSubject(PublishSubject.create());
    private final rx.Observable<T> output;
    private final ObservableField<T> value;

    public RxProperty() {
        value = new ObservableField<>();
        output = subject.asObservable();
        init();
    }

    public RxProperty(final T startValue) {
        value = new ObservableField<>();
        output = subject.startWith(startValue);
        init();
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T t) {
        onNext(t);
    }

    @NonNull
    public rx.Observable<T> getStream() {
        return output;
    }

    @Override
    public void onCompleted() {
        subject.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        subject.onError(e);
    }

    @Override
    public void onNext(T t) {
        subject.onNext(t);
    }

    private void init() {
        // The property is expected to be bound to UI elements, so we assume that consumers
        // no not care about the full history of value changes. Therefore, we apply backpressure
        // handling by taking the latest value and ignore duplicate values.
        output.onBackpressureLatest()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<T>() {
                    @Override
                    public void call(T t) {
                        value.set(t);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "onError: " + throwable.getMessage());
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * Data-binding related methods. The getGet, getSet, setGet, setSet methods are for allowing
     * the data-binding library to access either the ObservableField or the RxProperty itself
     * depending on whether the property is used for reading or writing.
     */
    @Deprecated
    public ObservableField<T> getGet() {
        return value;
    }

    @Deprecated
    public void setGet() {
    }

    @Deprecated
    public RxProperty<T> getSet() {
        return this;
    }

    @Deprecated
    public void setSet(RxProperty<T> property) {
    }

    @BindingAdapter("bindset")
    public static void bindEditText(EditText editText, RxProperty<String> property) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                property.setValue(s.toString());
            }
        });
    }
}
