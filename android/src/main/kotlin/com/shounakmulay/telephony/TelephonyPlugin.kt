package com.shounakmulay.telephony

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import com.shounakmulay.telephony.sms.IncomingSmsHandler
import com.shounakmulay.telephony.utils.Constants.CHANNEL_SMS
import com.shounakmulay.telephony.sms.IncomingSmsReceiver
import com.shounakmulay.telephony.sms.SmsController
import com.shounakmulay.telephony.sms.SmsMethodCallHandler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*


class TelephonyPlugin : FlutterPlugin, ActivityAware {

  private lateinit var smsChannel: MethodChannel

  private lateinit var smsMethodCallHandler: SmsMethodCallHandler

  private lateinit var smsController: SmsController

  private lateinit var binaryMessenger: BinaryMessenger

  private lateinit var permissionsController: PermissionsController

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val isInForeground = IncomingSmsHandler.isApplicationForeground(flutterPluginBinding.applicationContext)
    if (!this::binaryMessenger.isInitialized) {
      binaryMessenger = flutterPluginBinding.binaryMessenger
    }

    if (!isInForeground) {
      setupPlugin(flutterPluginBinding.applicationContext, binaryMessenger)
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    tearDownPlugin()
  }

  override fun onDetachedFromActivity() {
    tearDownPlugin()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    setupPlugin(binding.activity.applicationContext, binaryMessenger, binding.activity)
    binding.addRequestPermissionsResultListener(smsMethodCallHandler)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  private fun setupPlugin(context: Context, messenger: BinaryMessenger, activity: Activity? = null) {
    smsController = SmsController(context)
    permissionsController = PermissionsController(context)
    smsMethodCallHandler = SmsMethodCallHandler(context, smsController, permissionsController, activity)

    smsChannel = MethodChannel(messenger, CHANNEL_SMS)
    smsChannel.setMethodCallHandler(smsMethodCallHandler)
    smsMethodCallHandler.setForegroundChannel(smsChannel)
    val isInForeground = IncomingSmsHandler.isApplicationForeground(context)
    if (isInForeground) IncomingSmsReceiver.foregroundSmsChannel = smsChannel
  }

  private fun tearDownPlugin() {
    IncomingSmsReceiver.foregroundSmsChannel = null
    smsChannel.setMethodCallHandler(null)
  }

}
