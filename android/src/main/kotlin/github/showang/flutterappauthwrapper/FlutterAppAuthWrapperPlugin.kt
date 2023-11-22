package github.showang.flutterappauthwrapper

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar


class FlutterAppAuthWrapperPlugin(private var context: Context? = null,
                                  private var methodChannel: MethodChannel? = null,
                                  private var eventChannel: EventChannel? = null,
                                  private var activity: Activity? = null) :
    MethodCallHandler,
    EventChannel.StreamHandler,
    FlutterPlugin,
    ActivityAware {

    companion object {

        private const val CHANNEL_METHOD = "flutter_app_auth_wrapper"
        private const val CHANNEL_EVENT = "oauth_completion_events"

        const val METHOD_START_OAUTH = "startOAuth"

        var oauthEventSink: EventChannel.EventSink? = null

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val plugin = FlutterAppAuthWrapperPlugin()
            plugin.initInstance(registrar.messenger(), registrar.context())
        }
    }

    fun initInstance(messenger: BinaryMessenger, context: Context) {
        this.context = context

        methodChannel = MethodChannel(messenger, CHANNEL_METHOD)
        methodChannel?.setMethodCallHandler(this)

        eventChannel = EventChannel(messenger, CHANNEL_EVENT)
        eventChannel?.setStreamHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            METHOD_START_OAUTH -> startOAuth(call, result)
            else -> result.notImplemented()
        }
    }

    private fun startOAuth(call: MethodCall, result: Result) {
        val intent = Intent(context, OAuthActivity::class.java).apply {
            putExtra(OAuthActivity.INPUT_STRING_JSON_AUTH_CONFIG, call.arguments.toString())
        }
        activity?.startActivity(intent)
        result.success(true)
    }

    override fun onListen(arguments: Any?, sink: EventChannel.EventSink?) {
        oauthEventSink = sink
    }

    override fun onCancel(arguments: Any?) {
        oauthEventSink = null
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        initInstance(binding.binaryMessenger, binding.applicationContext)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = null
        methodChannel = null
        eventChannel = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}