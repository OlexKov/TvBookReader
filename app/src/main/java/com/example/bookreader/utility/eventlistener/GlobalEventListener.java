package com.example.bookreader.utility.eventlistener;
import android.util.Log;

import androidx.core.util.Consumer;
import com.example.bookreader.constants.GlobalEventType;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class GlobalEventListener {
    private final ConcurrentHashMap<GlobalEventType, CopyOnWriteArrayList<Consumer<Object>>> events = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<GlobalEventType, Object> lastEventData = new ConcurrentHashMap<>();
    public GlobalEventListener(){
        for (int i = 0; i < GlobalEventType.getLength(); i++) {
            events.put(GlobalEventType.fromId(i),new CopyOnWriteArrayList<>());
        }
    }

    public void subscribe(GlobalEventType eventType,Consumer<Object> handler){
        Log.d("GlobalEventLog","Subscribe " + eventType.name());
        CopyOnWriteArrayList<Consumer<Object>> list = events.get(eventType);
        if(list != null && !list.contains(handler)){
            list.add(handler);
        }
        // Якщо вже є подія, відразу викликати
        if (lastEventData.containsKey(eventType)) {
            Log.d("GlobalEventLog","LastEventAccept " + eventType.name());
            handler.accept(lastEventData.get(eventType));
        }
    }

    public void unSubscribe(GlobalEventType eventType,Consumer<Object> handler){
        Log.d("GlobalEventLog","UnsubscribeEvent "+eventType.name());
        CopyOnWriteArrayList<Consumer<Object>> list =   events.get(eventType);
        if(list != null){
            list.remove(handler);
        }
    }

    public void sendEvent(GlobalEventType eventType,Object o){
        Log.d("GlobalEventLog","SendEvent " + eventType.name());
        if(o == null) o = new Object();
        lastEventData.put(eventType, o);
        List<Consumer<Object>> list =   events.get(eventType);
        if(list != null && !list.isEmpty()){
            for (Consumer<Object> consumer:list){
                consumer.accept(o);
            }
        }
    }

}
