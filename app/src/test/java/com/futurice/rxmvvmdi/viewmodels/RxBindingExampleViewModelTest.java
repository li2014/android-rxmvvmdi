package com.futurice.rxmvvmdi.viewmodels;

import com.futurice.rxmvvmdi.RxTest;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.observers.TestSubscriber;

public class RxBindingExampleViewModelTest extends RxTest {

    private RxBindingExampleViewModel viewModel;

    protected void setup() {
        viewModel = new RxBindingExampleViewModel();
    }

    @Test
    public void timeStream_incrementsEveryTenMilliseconds() {
        TestSubscriber subscriber = new TestSubscriber();
        viewModel.getTimeStream().subscribe(subscriber);
        computationScheduler().advanceTimeBy(20, TimeUnit.MILLISECONDS);
        subscriber.assertValues("00:00:00", "00:00:01");
    }
}
