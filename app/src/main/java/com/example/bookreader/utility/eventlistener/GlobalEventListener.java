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
    private static final String TAG = "GlobalEventLog";
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
    public <T> boolean subscribe(GlobalEventType eventType, Consumer<T> handler, Class<T> clazz) {
        CopyOnWriteArrayList<Consumer<?>> list =  events.get(eventType);
        if (list != null && !list.contains(handler)) {
            Log.d(TAG, "Subscribe " + eventType.name());
            list.add(handler);
            // Якщо є остання подія - відправляємо і очищуємо її
            if (lastEventData.containsKey(eventType)) {
                Object data = lastEventData.get(eventType);
                if (clazz.isInstance(data)) {
                    handler.accept(clazz.cast(data));
                    Log.d(TAG, "Last event accept and clear " + eventType.name());
                    lastEventData.remove(eventType);
                }
            }
            return true;
        }
        Log.e(TAG, "ERROR " + eventType.name() +" SUBSCRIBE ! ! ! ");
        return false;
    }

    /** Підписка з автоматичною відпискою по lifecycle */
    @MainThread
    public <T> boolean subscribe(LifecycleOwner owner, GlobalEventType eventType, Consumer<T> handler, Class<T> clazz) {
        if(subscribe(eventType, handler, clazz)){
            owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onDestroy(@NonNull LifecycleOwner lifecycleOwner) {
                    if(unSubscribe(eventType, handler)){
                        Log.d(TAG, "Auto unsubscribe " + eventType.name());
                    }
                }
            });
            return true;
        }
        Log.e(TAG, "ERROR " + eventType.name() +" SUBSCRIBE ! ! ! ");
        return false;
    }

    /** Відписка */
    @MainThread
    public <T> boolean unSubscribe(GlobalEventType eventType, Consumer<T> handler) {
        CopyOnWriteArrayList<Consumer<?>> list =  events.get(eventType);
        if(list != null && list.remove(handler)) {
            Log.d(TAG, "Unsubscribe event " + eventType.name());
            return true;
        }
        Log.e(TAG, "ERROR " + eventType.name() +" UNSUBSCRIBE ! ! ! ");
        return false;
    }

    @SuppressWarnings("unchecked")
    /** Відправка події — можна з будь-якого потоку */
    public <T> void sendEvent(GlobalEventType eventType, T data) {
        if (data == null) data = (T) new Object();
        List<Consumer<?>> list =  events.get(eventType);
        if (list != null && !list.isEmpty()) {
            Log.d(TAG, "Send event " + eventType.name());
            T finalData = data;
            mainHandler.post(() -> {
                for (Object consumerObj : list) {
                    Consumer<T> consumer = (Consumer<T>) consumerObj;
                    consumer.accept(finalData);
                }
            });
        }
        else{
            lastEventData.put(eventType, data);
            Log.d(TAG, "Event put to last event array " + eventType.name());
        }
    }

    /** Очищення всіх подій та підписок */
    public void clearAll() {
        events.clear();
        lastEventData.clear();
        Log.d("GlobalEventLog", "All events cleared");
    }
}