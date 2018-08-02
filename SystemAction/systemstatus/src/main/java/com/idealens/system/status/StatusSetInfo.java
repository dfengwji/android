package com.idealens.system.status;

import java.util.ArrayList;

/**
 * @file:com.idealens.system.status.StatusSetInfo.java
 * @authoer:huangzizhen
 * @date:2017/12/7
 * @version:V1.0
 */

public class StatusSetInfo {
    public class ItemInfo{
        public int type;
        public String state;

        public ItemInfo(int type,String param){
            this.type = type;
            this.state = param;
        }
    }

    private ArrayList<ItemInfo> stateItems;
    private ArrayList<ItemInfo> baseItems;
    public StatusSetInfo(){
        baseItems = new ArrayList<>();
        stateItems = new ArrayList<>();
    }

    public void addStateItem(DeviceStateType type, String param){
        stateItems.add(new ItemInfo(type.getCode(),param));
    }

    public void addBaseItem(DeviceBaseType type, String param){
        baseItems.add(new ItemInfo(type.getCode(),param));
    }

    public ArrayList<ItemInfo> getStateItems(){
        return stateItems;
    }

    public ArrayList<ItemInfo> getBaseItems(){
        return baseItems;
    }

    public void addStateItems(ArrayList<ItemInfo> list){
        if(list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                ItemInfo info = list.get(i);
                stateItems.add(new ItemInfo(info.type, info.state));
            }
        }
    }

    public String toBaseString(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0, max = baseItems.size(); i < max; i++){
            ItemInfo info = baseItems.get(i);
            if(i < (max - 1))
                builder.append(info.type +","+info.state+"&");
            else
                builder.append(info.type +","+info.state);
        }
        return builder.toString();
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0, max = stateItems.size(); i < max; i++){
            ItemInfo info = stateItems.get(i);
            if(i < (max - 1))
                builder.append(info.type +","+info.state+"&");
            else
                builder.append(info.type +","+info.state);
        }
        return builder.toString();
    }
}
