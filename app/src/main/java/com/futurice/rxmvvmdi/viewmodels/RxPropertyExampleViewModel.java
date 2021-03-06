package com.futurice.rxmvvmdi.viewmodels;

import android.support.annotation.NonNull;

import com.futurice.rxmvvmdi.services.SystemMonitorService;

import javax.inject.Inject;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class RxPropertyExampleViewModel extends ViewModel {

    public final RxProperty<String> cpuUsage;
    public final RxProperty<String> maxCpuUsage;
    public final RxProperty<String> customInput;
    public final RxProperty<String> upperCaseInput;

    private final SystemMonitorService systemMonitorService;

    @Inject
    public RxPropertyExampleViewModel(@NonNull final SystemMonitorService systemMonitorService) {
        this.systemMonitorService = systemMonitorService;
        cpuUsage = new RxProperty<>();
        maxCpuUsage = new RxProperty<>();
        customInput = new RxProperty<>();
        upperCaseInput = new RxProperty<>();
    }

    @Override
    protected void subscribe(@NonNull CompositeSubscription subscriptions) {
        Observable<Float> cpuUtzStream = systemMonitorService
                .getCpuUtilizationStream()
                .share();
        subscriptions.add(cpuUtzStream
                .map(usage -> usage + "%")
                .subscribe(cpuUsage)
        );
        subscriptions.add(cpuUtzStream
                .scan((max, value) -> Math.max(max, value))
                .map(usage -> usage + "%")
                .subscribe(maxCpuUsage)
        );
        subscriptions.add(customInput
                .getStream()
                .filter(input -> input != null)
                .map(input -> input.toUpperCase())
                .subscribe(upperCaseInput));
    }
}
