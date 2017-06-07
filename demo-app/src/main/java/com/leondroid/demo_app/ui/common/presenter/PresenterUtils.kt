package io.geeny.sample.ui.common.presenter

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter


fun defaultErrorDialog(context: Context, throwable: Throwable, tag: String = "ERROR"): AlertDialog {
    val errors = StringWriter()
    throwable.printStackTrace(PrintWriter(errors))
    Log.e(tag, throwable.message, throwable)
    return AlertDialog.Builder(context).setTitle("Some error occurred").setMessage(errors.toString()).create()
}