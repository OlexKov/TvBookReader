package com.example.bookreader.utility.eventlistener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalEventListener {

    private final ConcurrentHashMap<GlobalEventType, CopyOnWriteArrayList<Consumer<?>>> events = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<GlobalEventType, Object> lastEventData = new ConcurrentHashMap<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public GlobalEventListener() {
        for (int i = 0; i < GlobalEventType.getLength(); i++) {
            GlobalEventType type = GlobalEventType.fromId(i);
            if(type != null){
                events.put(type, new CopyOnWriteArrayList<>());
            }
        }
    }

    /** Типобезпечна підписка без lifecycle */
    @MainThread
    public <T> void subscribe(GlobalEventType eventType, Consumer<T> handler, Class<T> clazz) {
        Log.d("GlobalEventLog", "Subscribe " + eventType.name());

        CopyOnWriteArrayList<Consumer<?>> list =  events.get(eventType);
        if (list != null && !list.contains(handler)) {
            list.add(handler);
            // Якщо є остання подія - відправляємо і очищуємо її
            if (lastEventData.containsKey(eventType)) {
                Object data = lastEventData.get(eventType);
                if (clazz.isInstance(data)) {
                    handler.accept(clazz.cast(data));
                    Log.d("GlobalEventLog", "LastEventAccept and clear " + eventType.name());
                    lastEventData.remove(eventType);
                }
            }
        }
    }

    /** Підписка з автоматичною відпискою по lifecycle */
    @MainThread
    public <T> void subscribe(LifecycleOwner owner, GlobalEventType eventType, Consumer<T> handler, Class<T> clazz) {
        subscribe(eventType, handler, clazz);

        owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner lifecycleOwner) {
                unSubscribe(eventType, handler);
                Log.d("GlobalEventLog", "AutoUnsubscribe " + eventType.name());
            }
        });
    }

    /** Відписка */
    @MainThread
    public <T> void unSubscribe(GlobalEventType eventType, Consumer<T> handler) {
        Log.d("GlobalEventLog", "UnsubscribeEvent " + eventType.name());

        CopyOnWriteArrayList<Consumer<?>> list =  events.get(eventType);
        if (list != null) {
            list.remove(handler);
        }
    }

    @SuppressWarnings("unchecked")
    /** Відправка події — можна з будь-якого потоку */
    public <T> void sendEvent(GlobalEventType eventType, T data) {
        Log.d("GlobalEventLog", "SendEvent " + eventType.name());
        if (data == null) data = (T) new Object();

        lastEventData.put(eventType, data);

        List<Consumer<?>> list =  events.get(eventType);
        if (list != null && !list.isEmpty()) {
            T finalData = data;
            mainHandler.post(() -> {
                for (Object consumerObj : list) {
                    Consumer<T> consumer = (Consumer<T>) consumerObj;
                    consumer.accept(finalData);
                }
            });
        }
    }

    /** Очищення всіх подій та підписок */
    public void clearAll() {
        events.clear();
        lastEventData.clear();
        Log.d("GlobalEventLog", "All events cleared");
    }
}