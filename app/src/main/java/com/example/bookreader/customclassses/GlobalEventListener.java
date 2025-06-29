package com.example.bookreader.customclassses;
import androidx.core.util.Consumer;
import com.example.bookreader.constants.GlobalEventType;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class GlobalEventListener {
    private final ConcurrentHashMap<GlobalEventType, CopyOnWriteArrayList<Consumer<Object>>> events = new ConcurrentHashMap<>();
    public GlobalEventListener(){
        for (int i = 0; i < GlobalEventType.getLength(); i++) {
            events.put(GlobalEventType.fromId(i),new CopyOnWriteArrayList<>());
        }
    }

    public void subscribe(GlobalEventType eventType,Consumer<Object> handler){
        List<Consumer<Object>> list =   events.get(eventType);
        if(list != null && !list.contains(handler)){
            list.add(handler);
        }
    }

    public void unSubscribe(GlobalEventType eventType,Consumer<Object> handler){
        List<Consumer<Object>> list =   events.get(eventType);
        if(list != null){
            list.remove(handler);
        }
    }

    public void sendEvent(GlobalEventType eventType,Object o){
        List<Consumer<Object>> list =   events.get(eventType);
        if(list != null && !list.isEmpty()){
            for (Consumer<Object> consumer:list){
                consumer.accept(o);
            }
        }
    }

}
