package com.awareframework.accelerometer.awareframeworkaccelerometer

import android.content.Context
import com.awareframework.android.core.db.Engine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import com.awareframework.android.sensor.accelerometer.AccelerometerSensor
import com.awareframework.android.sensor.accelerometer.model.AccelerometerData
import java.util.*
import kotlin.collections.HashMap


class AwareframeworkAccelerometerPlugin(val appContext: Context) : MethodCallHandler, EventChannel.StreamHandler {

    private var listeners: LinkedList<EventChannel.EventSink> = LinkedList();
    private var accelerometerObserver = AccelerometerObserver(listeners);

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            val instance = AwareframeworkAccelerometerPlugin(registrar.context());
            MethodChannel(registrar.messenger(), "awareframework_accelerometer/method").setMethodCallHandler(instance)
            EventChannel(registrar.messenger(), "awareframework_accelerometer/event_on_data_changed").setStreamHandler(instance)
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result): Unit {
        when(call.method){
            "getPlatformVersion"-> return result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "start"-> start(call, result)
            "stop"->stop(result)
        }
    }

    private fun start(call: MethodCall, result: Result) {
        AccelerometerSensor.start(appContext, getConfig(call))
        result.success(true)
    }

    private fun stop(result: Result) {
        AccelerometerSensor.stop(appContext);
        result.success(true)
    }

    override fun onCancel(p0: Any?) {
    }


    override fun onListen(p0: Any, sink: EventChannel.EventSink) {
        listeners.add(sink);
    }

    private fun getConfig(call: MethodCall): AccelerometerSensor.Config{
        var frequency = call.argument<Int>("frequency")
        var period = call.argument<Double>("period")
        var threshold = call.argument<Double>("threshold")
        var debug = call.argument<Boolean>("debug")
        var dbPath = call.argument<String>("dbPath")
        var label = call.argument<String>("label")
        var enabled = call.argument<Boolean>("enabled")
        return AccelerometerSensor.Config().apply {
            if(threshold!=null) this.threshold = threshold
            if(frequency!=null) this.interval = frequency
            if (period != null) this.period = period.toFloat()
            if(debug!=null) this.debug = debug
            if(dbPath!=null) this.dbPath = dbPath
            if(label!=null) this.label = label
            if(enabled !=null) this.enabled = enabled
            this.sensorObserver = accelerometerObserver
            this.dbType = Engine.DatabaseType.NONE
        }
    }
}

class AccelerometerObserver(var listeners: List<EventChannel.EventSink>) : AccelerometerSensor.Observer {
    override fun onDataChanged(data: AccelerometerData) {
        val map = HashMap<String, Any>()
        map.set("x", data.x);
        map.set("y", data.y);
        map.set("z", data.z);
        for (sink in listeners){
            sink.success(map);
        }
    }
}
   