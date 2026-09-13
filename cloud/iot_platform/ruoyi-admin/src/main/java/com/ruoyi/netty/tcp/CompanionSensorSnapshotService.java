package com.ruoyi.netty.tcp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Stores the latest sensor values reported by the board-side lower Python
 * process. The data is intentionally kept in memory because the home dashboard
 * only needs the current live snapshot.
 */
@Service
public class CompanionSensorSnapshotService
{
    private static final long ONLINE_TIMEOUT_MS = 15000L;

    private final Map<String, DeviceSensorSnapshot> snapshots = new ConcurrentHashMap<>();

    public void update(String did, Collection<SensorValue> values)
    {
        if (did == null || did.trim().isEmpty() || values == null || values.isEmpty())
        {
            return;
        }

        DeviceSensorSnapshot snapshot = snapshots.computeIfAbsent(did, DeviceSensorSnapshot::new);
        snapshot.update(values);
    }

    public DeviceSensorSnapshot getSnapshot(String did)
    {
        DeviceSensorSnapshot snapshot = snapshots.get(did);
        if (snapshot == null)
        {
            return DeviceSensorSnapshot.empty(did);
        }
        return snapshot.copy();
    }

    public static final class DeviceSensorSnapshot
    {
        private final String did;

        private final Map<Integer, SensorValue> values = new ConcurrentHashMap<>();

        private volatile long lastSeen;

        private DeviceSensorSnapshot(String did)
        {
            this.did = did;
        }

        public static DeviceSensorSnapshot empty(String did)
        {
            return new DeviceSensorSnapshot(did);
        }

        private void update(Collection<SensorValue> newValues)
        {
            long now = System.currentTimeMillis();
            for (SensorValue value : newValues)
            {
                values.put(value.getType(), value);
            }
            lastSeen = now;
        }

        private DeviceSensorSnapshot copy()
        {
            DeviceSensorSnapshot copied = new DeviceSensorSnapshot(did);
            copied.lastSeen = lastSeen;
            copied.values.putAll(values);
            return copied;
        }

        public String getDid()
        {
            return did;
        }

        public boolean isOnline()
        {
            return lastSeen > 0 && System.currentTimeMillis() - lastSeen <= ONLINE_TIMEOUT_MS;
        }

        public long getLastSeen()
        {
            return lastSeen;
        }

        public List<SensorValue> getSensors()
        {
            List<SensorValue> list = new ArrayList<>(values.values());
            Collections.sort(list, Comparator.comparingInt(SensorValue::getType));
            return list;
        }
    }

    public static final class SensorValue
    {
        private final int type;

        private final String name;

        private final String unit;

        private final double value;

        private final long timestamp;

        public SensorValue(int type, double value, long timestamp)
        {
            this.type = type;
            this.name = sensorName(type);
            this.unit = sensorUnit(type);
            this.value = value;
            this.timestamp = timestamp;
        }

        public int getType()
        {
            return type;
        }

        public String getName()
        {
            return name;
        }

        public String getUnit()
        {
            return unit;
        }

        public double getValue()
        {
            return value;
        }

        public long getTimestamp()
        {
            return timestamp;
        }

        private static String sensorName(int type)
        {
            switch (type)
            {
                case 0x01:
                    return "环境温度";
                case 0x02:
                    return "环境湿度";
                case 0x03:
                    return "光照强度";
                case 0x04:
                    return "空气质量 TVOC";
                case 0x05:
                    return "二氧化碳";
                case 0x06:
                    return "PM2.5";
                case 0x07:
                    return "红外接近";
                case 0x08:
                    return "毫米波人体存在";
                case 0x09:
                    return "毫米波目标距离";
                case 0x0A:
                    return "呼吸频率";
                case 0x0B:
                    return "心跳频率";
                case 0x0C:
                    return "光敏电阻电压";
                default:
                    return "未知传感器";
            }
        }

        private static String sensorUnit(int type)
        {
            switch (type)
            {
                case 0x01:
                    return "°C";
                case 0x02:
                    return "%RH";
                case 0x03:
                    return "lux";
                case 0x04:
                    return "ppb";
                case 0x05:
                    return "ppm";
                case 0x06:
                    return "µg/m³";
                case 0x07:
                case 0x08:
                    return "state";
                case 0x09:
                    return "m";
                case 0x0A:
                case 0x0B:
                    return "次/分钟";
                case 0x0C:
                    return "V";
                default:
                    return "";
            }
        }
    }
}
